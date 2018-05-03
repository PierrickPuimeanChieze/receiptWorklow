package com.cleitech.receipt.services

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.GoogleUtils
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.FileReader
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.GeneralSecurityException


@Component
class DriveInitService(val jsonFactory: JsonFactory,
                       val dataStoreFactory: DataStoreFactory,
                       @Value("\${spring.application.name") val applicationName: String,
                       @Value("\${proxy.host:#{null}}") val proxyHost : String? ,
                       @Value("\${proxy.port:#{80}}") val proxyport  : Int
) {

    @Throws(IOException::class, GeneralSecurityException::class)
    fun drive(): Drive = Drive.Builder(
            httpTransport(), jsonFactory, googleCredentials())
            .setApplicationName(applicationName)
            .build()

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun httpTransport(): HttpTransport //= GoogleNetHttpTransport.newTrustedTransport()
    {
        val httpBuilder = NetHttpTransport.Builder().
                trustCertificates(GoogleUtils.getCertificateTrustStore())
        if (proxyHost != null) {
            httpBuilder.setProxy(Proxy( Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyport)))
        }
        return httpBuilder.build()
    }

    fun googleCredentials(): Credential {
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, FileReader("./google_client_secret.json"))


        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                httpTransport(), jsonFactory, clientSecrets, googleAuthorizeScopes())
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build()
        val authorizationCodeInstalledApp = AuthorizationCodeInstalledApp(
                flow, LocalServerReceiver())
        val authorize = authorizationCodeInstalledApp.authorize("user")
        return authorize
    }

    fun googleAuthorizeScopes(): List<String>
            = listOf(
//            GmailScopes.GMAIL_SEND,
            DriveScopes.DRIVE_METADATA_READONLY,
            DriveScopes.DRIVE
    )
}