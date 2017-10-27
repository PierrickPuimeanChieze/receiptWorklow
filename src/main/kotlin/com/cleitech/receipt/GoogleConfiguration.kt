package com.cleitech.receipt

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.gmail.GmailScopes
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring5.SpringTemplateEngine
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.security.GeneralSecurityException


@Configuration
@EnableConfigurationProperties(MailProperties::class)
class GoogleConfiguration(val mailProperties: MailProperties,
                          val thymeleafEngine: SpringTemplateEngine,
                          @Value("\${credentials.directory}") val credentialDirectory: File,
                          @Value("\${spring.application.name") val applicationName: String
) {


//
//    @Bean
//    @Throws(GeneralSecurityException::class, IOException::class)
//    fun gmailService(): GmailService {
//        return GmailService(httpTransport(), googleCredentials(), jsonFactory(),
//                applicationName, thymeleafEngine, mailProperties)
//    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun httpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    @Bean
    fun driveService(): DriveService = DriveService(drive())

    @Bean
    fun drive(): Drive = Drive.Builder(
            httpTransport(), jsonFactory(), googleCredentials())
            .setApplicationName(applicationName)
            .build()


    @Bean
    fun googleCredentialsDataStoreFactory(): DataStoreFactory
            = FileDataStoreFactory(credentialDirectory)

    private fun googleAuthorizeScopes(): List<String>
            = listOf(
            GmailScopes.GMAIL_SEND,
            DriveScopes.DRIVE_METADATA_READONLY,
            DriveScopes.DRIVE
    )

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun googleCredentials(): Credential {

        val clientSecrets = GoogleClientSecrets.load(jsonFactory(), FileReader("./google_client_secret.json"))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                httpTransport(), jsonFactory(), clientSecrets, googleAuthorizeScopes())
                .setDataStoreFactory(googleCredentialsDataStoreFactory())
                .setAccessType("offline")
                .build()
        return AuthorizationCodeInstalledApp(
                flow, LocalServerReceiver()).authorize("user")
    }

    @Bean
    fun jsonFactory(): JsonFactory = JacksonFactory.getDefaultInstance()

}
