package com.cleitech.receipt

import com.cleitech.receipt.dropbox.DropboxService
import com.cleitech.receipt.properties.ServiceProperties
import com.cleitech.receipt.properties.ShoeboxedProperties
import com.cleitech.receipt.shoeboxed.ShoeboxedService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import java.io.File

typealias GoogleFile = com.google.api.services.drive.model.File
typealias MessageHandler = (Message<File>) -> Unit

@SpringBootApplication
@EnableConfigurationProperties(ServiceProperties::class, ShoeboxedProperties::class)
class SampleIntegrationApplication {

    @Bean
    fun dropboxService(@Value("\${dropbox.uploadPath:IN}")
                       uploadPath: String,

                       @Value("\${dropbox.accessToken}")
                       accessTokenFile: String
    ): DropboxService {
        val dropboxService = DropboxService(uploadPath, accessTokenFile)
//        dropboxService.initDropboxAccessToken()
        return dropboxService
    }

    @Bean
    fun testRunner(shoeboxedService: ShoeboxedService): CommandLineRunner = CommandLineRunner {
    }
}


fun main(args: Array<String>) {
    SpringApplication.run(SampleIntegrationApplication::class.java, *args)
}

