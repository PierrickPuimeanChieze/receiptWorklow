package com.cleitech.receipt.handlers

import com.cleitech.receipt.DriveFileHeaders.OCR_CAT
import com.google.api.services.drive.model.File
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class DatamolinoHandler {

    val restTemplate: RestTemplate = RestTemplate()

    fun uploadMessageToDatamolino(requestMessage: Message<File>): Message<File> {
        //TODO
        println("Uploading file to datamolino with category :" + requestMessage.headers[OCR_CAT])
        return requestMessage
    }

}