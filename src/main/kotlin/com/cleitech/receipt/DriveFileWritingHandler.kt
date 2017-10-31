package com.cleitech.receipt

import com.google.api.services.drive.model.File
import org.springframework.integration.file.FileHeaders
import org.springframework.messaging.Message


class DriveFileWritingHandler(val driveService: DriveService) {

    fun handleMessage(requestMessage: Message<File>) {
        var file = requestMessage.payload;
        var sourceDir: String? = requestMessage.headers[FileHeaders.REMOTE_DIRECTORY] as String
        var destDir: String = requestMessage.headers[DriveFileHeaders.DEST_DIR] as String

        driveService.copyFileToUploadedDir(file.id, sourceDir, destDir);
    }
}