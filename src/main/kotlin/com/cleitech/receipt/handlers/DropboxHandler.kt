package com.cleitech.receipt.handlers

import com.cleitech.receipt.GoogleFile
import com.cleitech.receipt.headers.DriveFileHeaders
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component
class DropboxHandler {

    fun uploadGoogleFileToDropbox(requestMessage: Message<GoogleFile>): Message<GoogleFile> {
        //TODO
        println("Copying file to dropbox in directory :" + requestMessage.headers[DriveFileHeaders.DROPBOX_PATH])
        return requestMessage
    }

}