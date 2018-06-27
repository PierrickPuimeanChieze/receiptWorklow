package com.cleitech.receipt

import com.cleitech.receipt.properties.ServiceProperties
import com.cleitech.receipt.properties.ShoeboxedProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.messaging.Message
import java.io.File

typealias GoogleFile = com.google.api.services.drive.model.File
typealias MessageHandler = (Message<File>) -> Unit

@SpringBootApplication
@EnableConfigurationProperties(ServiceProperties::class, ShoeboxedProperties::class)
class SampleIntegrationApplication


fun main(args: Array<String>) {
    SpringApplication.run(SampleIntegrationApplication::class.java, *args)
}