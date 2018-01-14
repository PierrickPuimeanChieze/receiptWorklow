package com.cleitech.receipt.handlers

import com.cleitech.receipt.DriveFileHeaders.OCR_CAT
import com.cleitech.receipt.services.DriveService
import com.cleitech.receipt.services.OcrService
import com.google.api.services.drive.model.File
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class OcrHandler(val ocrService: OcrService,
                 val driveService: DriveService) {


    fun uploadMessageToDatamolino(requestMessage: Message<File>): Message<File> {
        val inputStreamForFile = driveService.getInputStreamForFile(requestMessage.payload)
        ocrService.uploadFile(inputStreamForFile)
        inputStreamForFile.close()
        return requestMessage
    }

}