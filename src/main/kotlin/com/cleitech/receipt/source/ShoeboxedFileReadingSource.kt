package com.cleitech.receipt.source

import com.cleitech.receipt.headers.DriveFileHeaders
import com.cleitech.receipt.shoeboxed.ShoeboxedService
import com.cleitech.receipt.shoeboxed.domain.Document
import org.springframework.integration.context.IntegrationObjectSupport
import org.springframework.integration.core.MessageSource
import org.springframework.messaging.Message
import java.util.*
import java.util.concurrent.LinkedTransferQueue

class ShoeboxedFileReadingSource(val shoeboxedService: ShoeboxedService) : IntegrationObjectSupport(), MessageSource<ByteArray> {

    //TODO to conf file
    private val CATEGORY: String = "dummySend"
    private val DROPBOX_PATH: String = "IN"
    private val toBeReceived: Queue<Document> = LinkedTransferQueue<Document>()

    override fun receive(): Message<ByteArray>? {
        var message: Message<ByteArray>? = null

        // rescan only if needed
        if (this.toBeReceived.isEmpty()) {
            scanInputDirectory()
        }

        val file: Document? = this.toBeReceived.poll()

        // file == null means the queue was empty
        // we can't rely on isEmpty for concurrency reasons


        if (file != null && accept(file)) {

            val payloadBuilder = messageBuilderFactory.withPayload(file.attachment.url.readBytes())

            payloadBuilder.setHeader(DriveFileHeaders.DROPBOX_PATH, DROPBOX_PATH)
            message = payloadBuilder.build()
            if (logger.isInfoEnabled) {
                logger.info("Created message: [$message]")
            }
        }
        return message
    }

    private fun scanInputDirectory() {
        var retrieveDocument = shoeboxedService.retrieveDocument(CATEGORY)
        for (document in retrieveDocument) {
            this.toBeReceived.add(document)

        }
    }

    private val monitor = Any()

    private val seenSet = HashSet<String>()
    private fun accept(file: Document): Boolean {
        synchronized(this.monitor) {
            if (this.seenSet.contains(file.id)) {
                return false
            }
//            if (this.seen != null) {
//                if (!this.seen.offer(file)) {
//                    val removed = this.seen.poll()
//                    this.seenSet.remove(removed)
//                    this.seen.add(file)
//                }
//            }
            this.seenSet.add(file.id)
            return true
        }
    }
}