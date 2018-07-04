package com.cleitech.receipt.handlers

import com.cleitech.receipt.GoogleFile
import com.cleitech.receipt.dropbox.DropboxService
import com.cleitech.receipt.headers.DriveFileHeaders
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.net.URL

@Component
class DropboxHandler(val dropboxService: DropboxService) {

    fun uploadGoogleFileToDropbox(requestMessage: Message<GoogleFile>): Message<GoogleFile> {
        //TODO
        println("Copying file to dropbox in directory :" + requestMessage.headers[DriveFileHeaders.DROPBOX_PATH])
        return requestMessage
    }

    fun uploadByteArrayToDropbox(requestMessage: Message<ByteArray>): Message<ByteArray> {
        //TODO
        println("Copying file to dropbox in directory :" + requestMessage.headers[DriveFileHeaders.DROPBOX_PATH])
        return requestMessage
    }

    fun uploadUrlContentToDropbox(requestMessage: Message<URL>): Message<URL> {


        requestMessage.payload.openStream().use { it ->
            dropboxService.uploadStream(requestMessage.headers[DriveFileHeaders.DROPBOX_PATH] as String, it)
        }

        return requestMessage
    }
}

