package com.cleitech.receipt.handlers

import com.cleitech.receipt.headers.DriveFileHeaders
import com.cleitech.receipt.services.DriveService
import com.google.api.services.drive.model.File
import org.springframework.integration.file.FileHeaders
import org.springframework.messaging.Message


class DriveFileWritingHandler(private val driveService: DriveService) {

    fun handleMessage(requestMessage: Message<File>) {
        val file = requestMessage.payload
        val sourceDir: String? = requestMessage.headers[FileHeaders.REMOTE_DIRECTORY] as String
        val destDir: String = requestMessage.headers[DriveFileHeaders.DEST_DIR] as String

        driveService.copyFileToUploadedDir(file.id, sourceDir, destDir)
    }
}