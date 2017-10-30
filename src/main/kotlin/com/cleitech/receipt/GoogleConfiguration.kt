package com.cleitech.receipt

import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.client.util.store.FileDataStoreFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File


@Configuration
//@EnableConfigurationProperties(MailProperties::class)
class GoogleConfiguration(@Value("\${credentials.directory}") val credentialDirectory: File
) {


//
//    @Bean
//    @Throws(GeneralSecurityException::class, IOException::class)
//    fun gmailService(): GmailService {
//        return GmailService(httpTransport(), googleCredentials(), jsonFactory(),
//                applicationName, thymeleafEngine, mailProperties)
//    }





    @Bean
    fun googleCredentialsDataStoreFactory(): DataStoreFactory
            = FileDataStoreFactory(credentialDirectory)





    @Bean
    fun jsonFactory(): JsonFactory = JacksonFactory.getDefaultInstance()

}
