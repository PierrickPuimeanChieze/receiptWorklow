package com.cleitech.receipt

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
import java.io.File
import java.util.function.Consumer


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
    fun inputChannel(): DirectChannel = DirectChannel()

    @Bean
    fun outputChannel(): DirectChannel  = DirectChannel()

    @Bean
    fun fileWriter(): FileWritingMessageHandler {
        val writer = FileWritingMessageHandler(
                File("output"))
        writer.setExpectReply(false)
        return writer
    }

    @Bean
    fun driveFileWritingHandler(driveService: DriveService) : DriveFileWritingHandler
            = DriveFileWritingHandler(driveService)

    @Bean
    fun integrationFlow(endpoint: SampleEndpoint,
                        driveFileReadingSource:DriveFileReadingSource,
                        driveFileWritingHandler: DriveFileWritingHandler): IntegrationFlow {
        val fixedRatePoller = Consumer<SourcePollingChannelAdapterSpec> { spec ->
            spec.poller(Pollers.fixedRate(500))
        }
        return IntegrationFlows.from(driveFileReadingSource, fixedRatePoller)
//                .channel(inputChannel())
//                .transform(streamToString())
//                .handle(endpoint)
                .channel(outputChannel())
                .handle(driveFileWritingHandler)
                .get()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(SampleIntegrationApplication::class.java, *args)
}