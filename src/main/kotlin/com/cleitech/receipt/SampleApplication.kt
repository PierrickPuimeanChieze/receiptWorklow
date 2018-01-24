package com.cleitech.receipt

import com.cleitech.receipt.handlers.OcrHandler
import com.cleitech.receipt.handlers.DriveFileWritingHandler
import com.cleitech.receipt.handlers.DropboxHandler
import com.cleitech.receipt.services.DriveService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.integration.file.FileReadingMessageSource
import org.springframework.integration.file.FileWritingMessageHandler
import org.springframework.integration.router.AbstractMessageRouter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import java.io.File
import java.util.function.Consumer

typealias MessageHandler = (Message<File>) -> Unit
@SpringBootApplication
@EnableConfigurationProperties([ServiceProperties::class, ShoeboxedProperties::class])
//@Import(GoogleConfiguration::class)
@EnableOAuth2Client
class SampleIntegrationApplication {


    @Bean
    fun fileReader(): FileReadingMessageSource {
        val reader = FileReadingMessageSource()
        reader.setDirectory(File("input"))

        return reader
    }

    @Bean
    fun driveFileReadingSource(driveService: DriveService, confService: ConfigurationService): DriveFileReadingSource
            = DriveFileReadingSource(driveService, confService)

    @Bean
    fun driveFileWritingHandler(driveService: DriveService): DriveFileWritingHandler
            = DriveFileWritingHandler(driveService)

    /**
     * Route a payload, either to Dropbox  OCR channel, depending of the presence of payload header OCR_CAT or DROPBOX_DIR.
     * Absence of both will write to Drive
     * Presence of both will throw an IllegalStateException
     */
    @Bean
    fun driveRouter() = object : AbstractMessageRouter() {
        override fun determineTargetChannels(message: Message<*>): MutableCollection<MessageChannel> {
            val toOcr = message.headers.containsKey(DriveFileHeaders.OCR_CAT)
            val toDropbox = message.headers.containsKey(DriveFileHeaders.DROPBOX_DIR)
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

    @Bean
    fun readFromDrive(): DirectChannel = DirectChannel()

    @Bean
    fun writeToDrive(): DirectChannel = DirectChannel()

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
     * Describe a flow moving file from a directories to another in google drive.
     * The target directory is determined by the payload header DriveFileHeaders.DEST_DIR
     * If an ocr category is associated to the payload (payload header DriveFileHeaders.OCR_CAT), the file will be sent
     * to the OCR service before being moved
     * If a Dropbox directory is associated to the payload (TODO payload header), the file will be sent to dropbox
     * before being moved
     * OCR and dropbox are currently mutually exclusive
     */
    @Bean
    fun fromDriveFlow(driveFileReadingSource: DriveFileReadingSource,
                      toOcr: IntegrationFlow,
                      writeToDriveFlow: IntegrationFlow): IntegrationFlow {
        val fixedRatePoller = Consumer<SourcePollingChannelAdapterSpec> { spec ->
            spec.poller(Pollers.fixedRate(500))
        }

        return IntegrationFlows.from(driveFileReadingSource, fixedRatePoller)
                .route(
                        driveRouter())
                .get()
    }

    @Bean
    fun toOcr(ocrHandler: OcrHandler): IntegrationFlow = IntegrationFlow { sf ->
        sf.channel(sendToOcr())

                .handle(ocrHandler)
                .channel(writeToDrive())

    }

    @Bean
    fun toDropBoxFlow(dropboxHandler: DropboxHandler): IntegrationFlow = IntegrationFlow { sf ->
        sf.channel(sendToDropbox())
                .handle(dropboxHandler)
                .channel(writeToDrive())

    }
    /**
     * This flow read payload sent to writeToDrive channel and write them to Google Drive directory
     */
    @Bean
    fun writeToDriveFlow(driveFileWritingHandler: DriveFileWritingHandler) = IntegrationFlow { sf ->
        sf.channel(writeToDrive())
                .handle(driveFileWritingHandler)
    }
}


fun main(args: Array<String>) {
    SpringApplication.run(SampleIntegrationApplication::class.java, *args)
}