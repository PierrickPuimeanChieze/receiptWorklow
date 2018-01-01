package com.cleitech.receipt.handlers

import com.cleitech.receipt.DriveFileHeaders
import com.google.api.services.drive.model.File
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class DropboxHandler {

    fun uploadMessageToDropbox(requestMessage: Message<File>): Message<File> {
        //TODO
        println("Copying file to dropbox in directory :" + requestMessage.headers[DriveFileHeaders.DROPBOX_DIR])
        return requestMessage
    }

}