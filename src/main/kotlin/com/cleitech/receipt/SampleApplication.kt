package com.cleitech.receipt

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.*
import org.springframework.integration.file.FileReadingMessageSource
import org.springframework.integration.file.FileWritingMessageHandler
import org.springframework.integration.handler.LoggingHandler
import org.springframework.integration.router.AbstractMessageRouter
import org.springframework.integration.router.ExpressionEvaluatingRouter
import org.springframework.integration.router.MethodInvokingRouter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import java.io.File
import java.util.function.Consumer
import java.util.function.Function


@SpringBootApplication
@EnableConfigurationProperties(ServiceProperties::class)
//@Import(GoogleConfiguration::class)
class SampleIntegrationApplication() {


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

    @Bean
    fun driveRouter() = object : AbstractMessageRouter() {
        override fun determineTargetChannels(message: Message<*>): MutableCollection<MessageChannel> {
            if (message.headers.containsKey(DriveFileHeaders.OCR_CAT))
                return mutableListOf(sendToOcr())
            else
                return mutableListOf(writeToDrive())
        }
    }

    @Bean
    fun readFromDrive(): DirectChannel = DirectChannel()

    @Bean
    fun writeToDrive(): DirectChannel = DirectChannel()

    @Bean
    fun writeToOcr(): DirectChannel = DirectChannel()

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



        //This consumer will determined the subflow destination : either ocr or dropbox
        //TODO replace the writeToDriveFlow by a writeToDropboxFlow
        var consumer = Consumer<RouterSpec<Boolean, ExpressionEvaluatingRouter>>{ spec: RouterSpec<Boolean, ExpressionEvaluatingRouter> ->
            run {
                spec
                        .subFlowMapping(true, toOcr)
                        .subFlowMapping(false, writeToDriveFlow)
            }

        }
        return IntegrationFlows.from(driveFileReadingSource, fixedRatePoller)
                .route(
                        //??? Expression router
                        "headers.containsKey('${DriveFileHeaders.OCR_CAT}')"

                        ,
                        consumer)
                .get()
    }

    @Bean
    fun toOcr(): IntegrationFlow = IntegrationFlow { sf ->
        sf.channel(writeToOcr())
                .log(LoggingHandler.Level.INFO, "ocr.test.originalFileName", {m:Message<com.google.api.services.drive.model.File>-> m.payload.originalFilename})
                .log(LoggingHandler.Level.INFO, "ocr.test.category", {m:Message<com.google.api.services.drive.model.File>-> m.headers[DriveFileHeaders.OCR_CAT]})
                .wireTap(writeToDrive())

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