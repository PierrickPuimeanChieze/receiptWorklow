package com.cleitech.receipt.handlers

import com.cleitech.receipt.GoogleFile
import com.cleitech.receipt.headers.DriveFileHeaders
import com.cleitech.receipt.headers.OperationHeaders
import com.cleitech.receipt.services.DriveService
import com.cleitech.receipt.services.OcrService
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessagingException
import org.springframework.messaging.support.MessageBuilder

/**
 * Manage the upload of a google file payload to an OCR service. Do not modify the payload
 */
class OcrHandler(private val ocrService: OcrService,
                 private val driveService: DriveService,
                 private val logChannel: MessageChannel) {


    @ServiceActivator
    fun uploadMessageToOCR(requestMessage: Message<GoogleFile>): Message<GoogleFile> {
        val inputStreamForFile = driveService.getInputStreamForFile(requestMessage.payload)
        inputStreamForFile.use {
            try {
                ocrService.uploadDocument(it)
                return MessageBuilder.fromMessage(requestMessage)
                        .setHeader(OperationHeaders.OCR_TREATMENT_SUCCESS, true).build()
            } catch (e: Exception) {
                val message = "Error when sending file ${requestMessage.payload.originalFilename} from directory ${requestMessage.headers[DriveFileHeaders.SOURCE_DIR_LABEL]} to Ocr Service"
                logChannel.send(MessageBuilder.withPayload(MessagingException(message, e)).build())
                return org.springframework.messaging.support.MessageBuilder.fromMessage(requestMessage)
                        .setHeader(com.cleitech.receipt.headers.OperationHeaders.OCR_TREATMENT_SUCCESS, false).build()

            }
        }


    }

}