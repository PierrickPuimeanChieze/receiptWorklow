package com.cleitech.receipt

import com.google.api.services.drive.model.File
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

        var file: File? = this.toBeReceived.poll()

        // file == null means the queue was empty
        // we can't rely on isEmpty for concurrency reasons


        if (file != null && accept(file)) {
            var withPayload = messageBuilderFactory.withPayload(file)
            file.appProperties.forEach { key, value ->
                withPayload.setHeader(key, value)
            }
            withPayload.setHeader(FileHeaders.FILENAME, file.originalFilename)
            message = withPayload.build()
            if (logger.isInfoEnabled()) {
                logger.info("Created message: [$message]")
            }
        }
        return message
    }

    private fun scanInputDirectory() {
        val driveToOcrEntries = this.configurationService.driveToOcrEntries()
        driveToOcrEntries.forEach { entry ->
            val files = driveService.retrieveFileToUpload(entry.toScanDirId).filter { accept(it) }
            files.forEach { file ->
                file.appProperties =
                        mapOf(
                                DriveFileHeaders.DEST_DIR to entry.uploadedDirId,
                                DriveFileHeaders.OCR_CAT to entry.ocrCategory,
                                FileHeaders.REMOTE_DIRECTORY to entry.toScanDirId
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