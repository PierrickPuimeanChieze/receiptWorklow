package com.cleitech.receipt.source

import com.cleitech.receipt.ConfigurationService
import com.cleitech.receipt.headers.DriveFileHeaders
import com.cleitech.receipt.services.DriveService
import com.google.api.services.drive.model.File
import org.springframework.integration.IntegrationMessageHeaderAccessor
import org.springframework.integration.context.IntegrationObjectSupport
import org.springframework.integration.core.MessageSource
import org.springframework.integration.file.FileHeaders
import org.springframework.messaging.Message
import java.util.*
import java.util.concurrent.LinkedTransferQueue


class DriveFileReadingSource(val driveService: DriveService, val configurationService: ConfigurationService) : IntegrationObjectSupport(), MessageSource<File> {
    private val toBeReceived: Queue<File> = LinkedTransferQueue<File>()

    override fun receive(): Message<File>? {
        var message: Message<File>? = null

        // rescan only if needed
        if (this.toBeReceived.isEmpty()) {
            scanInputDirectory()
        }

        val file: File? = this.toBeReceived.poll()

        // file == null means the queue was empty
        // we can't rely on isEmpty for concurrency reasons


        if (file != null && accept(file)) {
            val payloadBuilder = messageBuilderFactory.withPayload(file)
            //Here we map each file property to a header of the payload
            file.appProperties.forEach { key, value ->
                payloadBuilder.setHeader(key, value)
            }
            payloadBuilder.setHeader(FileHeaders.FILENAME, file.originalFilename)
            message = payloadBuilder.build()
            if (logger.isInfoEnabled) {
                logger.info("Created message: [$message]")
            }
        }
        return message
    }

    private fun scanInputDirectory() {
        val driveToOcrEntries = this.configurationService.driveToOcrEntries()
        driveToOcrEntries.forEach { configurationEntry ->
            val files = driveService.retrieveFileToUpload(configurationEntry.toScanDirId).filter { accept(it) }
            files.forEach { file ->
                //Here we map configuration entry field to a file property
                file.appProperties =
                        mapOf(
                                DriveFileHeaders.DEST_DIR to configurationEntry.uploadedDirId,
                                DriveFileHeaders.DROPBOX_PATH to configurationEntry.dropboxDir,
                                DriveFileHeaders.OCR_CAT to configurationEntry.ocrCategory,
                                FileHeaders.REMOTE_DIRECTORY to configurationEntry.toScanDirId,
                                IntegrationMessageHeaderAccessor.CORRELATION_ID to configurationEntry.toScanDirId,
                                DriveFileHeaders.SOURCE_DIR_LABEL to configurationEntry.toScanDirLabel

                        )

            }
            toBeReceived.addAll(files)
        }
    }

    //Filter part. eventually, externalize it
    private val monitor = Any()
    //private val seen: Queue<File>?

    private val seenSet = HashSet<File>()
    private fun accept(file: File): Boolean {
        synchronized(this.monitor) {
            if (this.seenSet.contains(file)) {
                return false
            }
//            if (this.seen != null) {
//                if (!this.seen.offer(file)) {
//                    val removed = this.seen.poll()
//                    this.seenSet.remove(removed)
//                    this.seen.add(file)
//                }
//            }
            this.seenSet.add(file)
            return true
        }
    }

}