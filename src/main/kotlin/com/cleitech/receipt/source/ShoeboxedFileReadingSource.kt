package com.cleitech.receipt.source

import com.cleitech.receipt.headers.DriveFileHeaders
import com.cleitech.receipt.headers.ShoeboxedHeaders
import com.cleitech.receipt.shoeboxed.ShoeboxedService
import com.cleitech.receipt.shoeboxed.domain.Document
import org.springframework.integration.context.IntegrationObjectSupport
import org.springframework.integration.core.MessageSource
import org.springframework.messaging.Message
import java.util.*
import java.util.concurrent.LinkedTransferQueue

class ShoeboxedFileReadingSource(val toSendCategoryName: String, val toSendCategoryId: String, val dropboxUploadDir: String, val shoeboxedService: ShoeboxedService) : IntegrationObjectSupport(), MessageSource<ByteArray> {

    private val toBeReceived: Queue<Document> = LinkedTransferQueue<Document>()

    companion object {
        private const val PROPERTY_NAME_TYPE: String = "type"
    }

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

            val payloadBuilder = messageBuilderFactory.withPayload(file.attachment!!.url!!.readBytes())


            val dropboxPath = "$dropboxUploadDir/${buildUploadFileName(file)}"
            payloadBuilder.setHeader(DriveFileHeaders.DROPBOX_PATH, dropboxPath)
            payloadBuilder.setHeader(ShoeboxedHeaders.TO_SEND_CATEGORY, toSendCategoryId)
            message = payloadBuilder.build()
            if (logger.isInfoEnabled) {
                logger.info("Created message: [$message]")
            }
        }
        return message
    }

    private fun buildUploadFileName(document: Document): String {
        return String.format("%tF_%s_%s%s.pdf",
                document.issued,
                document.vendor.replace(" ", ""),
                document.total.toString().replace('.', ','),
                "_" + extractTypeInfoFromCategory(document.categories))
    }

    /**
     * Parse catgories, searching category starting with `[.PROPERTY_NAME_TYPE]:`
     *
     * @param categories categories to parse
     * @return type info, or empty value
     */
    private fun extractTypeInfoFromCategory(categories: List<String>): String {
        val propertyMarker = "$PROPERTY_NAME_TYPE:"
        for (category in categories) {
            if (category.startsWith(propertyMarker)) {
                return category.substring(propertyMarker.length)
            }
        }
        return ""
    }

    private fun scanInputDirectory() {
        var retrieveDocument = shoeboxedService.retrieveDocument(shoeboxedService.retrieveAccountId(), toSendCategoryName)
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
            this.seenSet.add(file.id!!)
            return true
        }
    }
}