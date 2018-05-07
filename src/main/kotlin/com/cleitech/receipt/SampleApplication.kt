package com.cleitech.receipt

import com.cleitech.receipt.handlers.DriveFileWritingHandler
import com.cleitech.receipt.handlers.OcrHandler
import com.cleitech.receipt.headers.DriveFileHeaders
import com.cleitech.receipt.properties.ServiceProperties
import com.cleitech.receipt.properties.ShoeboxedProperties
import com.cleitech.receipt.services.DriveService
import com.cleitech.receipt.services.OcrService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ImportResource
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.file.FileWritingMessageHandler
import org.springframework.integration.router.AbstractMessageRouter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import java.io.File

typealias GoogleFile = com.google.api.services.drive.model.File
typealias MessageHandler = (Message<File>) -> Unit

@SpringBootApplication
@EnableConfigurationProperties(ServiceProperties::class, ShoeboxedProperties::class)
@ImportResource("workflow.xml")
class SampleIntegrationApplication {


    @Bean("driveFileReadingSource")
    fun driveFileReadingSource(driveService: DriveService, confService: ConfigurationService): DriveFileReadingSource = DriveFileReadingSource(driveService, confService)

    @Bean("driveFileWritingHandler")
    fun driveFileWritingHandler(driveService: DriveService): DriveFileWritingHandler = DriveFileWritingHandler(driveService)

    /**
     * Route a payload, either to Dropbox  OCR channel, depending of the presence of payload header OCR_CAT or DROPBOX_PATH.
     * Absence of both will write to Drive
     * Presence of both will throw an IllegalStateException
     */
    @Bean
    fun driveRouter() = object : AbstractMessageRouter() {
        override fun determineTargetChannels(message: Message<*>): MutableCollection<MessageChannel> {
            val toOcr = message.headers.containsKey(DriveFileHeaders.OCR_CAT)
            val toDropbox = message.headers.containsKey(DriveFileHeaders.DROPBOX_PATH)
            if (toOcr && toDropbox)
                throw IllegalStateException(message.toString() + " need to be sent to OCR OR Dropbox. Not box")
            else if (toOcr)
                return mutableListOf(sendToOcr())
            else if (toDropbox)
                return mutableListOf(sendToDropbox())
            else
                return mutableListOf(writeToDrive())
        }
    }

    @Bean("readFromDrive")
    fun readFromDrive(): DirectChannel = DirectChannel()

    @Bean("writeToDrive")
    fun writeToDrive(): DirectChannel = DirectChannel()

    @Bean
    fun mailToSend(): DirectChannel = DirectChannel()

    @Bean
    fun sendToDropbox(): DirectChannel = DirectChannel()

    @Bean
    fun sendToOcr(): DirectChannel = DirectChannel()

    @Bean
    fun fileWriter(): FileWritingMessageHandler {
        val writer = FileWritingMessageHandler(
                File("output"))
        writer.setExpectReply(false)
        return writer
    }

    /**
     * This flow read payload sent to writeToDrive channel and write them to Google Drive directory
     */
    @Bean
    fun writeToDriveFlow(driveFileWritingHandler: DriveFileWritingHandler) = IntegrationFlow { sf ->
        sf.channel(writeToDrive())
                .handle(driveFileWritingHandler)
    }

    /**
     * This handler upload the file to ocr
     */
    @Bean
    fun ocrHandler(ocrService: OcrService,
                   driveService: DriveService, logChannel: MessageChannel) = OcrHandler(ocrService, driveService, logChannel)


    @Bean
    fun ocrMailMessageGroupProcessor() = MailAggregator(
            subject = "Cleitech Solutions - Elements envoyés pour OCR",
            to = "pierrick.puimean@gmail.com",
            from = "pierrick.puimean@gmail.com")

    @Bean
    fun dropboxMailMessageGroupProcessor() = MailAggregator(
            subject = "Cleitech Solutions - Elements déposés sur le dropbox",
            to = "pierrick.puimean@gmail.com",
            from = "pierrick.puimean@gmail.com")

}



fun main(args: Array<String>) {
    SpringApplication.run(SampleIntegrationApplication::class.java, *args)
}