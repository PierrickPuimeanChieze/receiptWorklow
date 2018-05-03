package com.cleitech.receipt.handlers

import com.cleitech.receipt.GoogleFile
import com.cleitech.receipt.headers.OperationHeaders
import com.cleitech.receipt.services.DriveService
import com.cleitech.receipt.services.OcrService
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

/**
 * Manage the upload of a google file payload to an OCR service. Do not modify the payload
 */
@Component
class OcrHandler(val ocrService: OcrService,
                 val driveService: DriveService) {


    @ServiceActivator
    fun uploadMessageToOCR(requestMessage: Message<GoogleFile>): Message<GoogleFile> {
        val inputStreamForFile = driveService.getInputStreamForFile(requestMessage.payload)
        ocrService.uploadFile(inputStreamForFile)
        inputStreamForFile.close()
        return MessageBuilder.fromMessage(requestMessage)
                .setHeader(OperationHeaders.OCR_TREATMENT_SUCCESS, true).build()
    }

}