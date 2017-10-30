package com.cleitech.receipt


import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.annotation.PostConstruct


/**
 * Created by ppc on 1/26/2017.
 */
@Component
class DriveService(val driveInitService: DriveInitService)  {

    lateinit var drive : Drive;

    @PostConstruct
    fun initDrive() {
        drive = driveInitService.drive()
    }
    @Throws(IOException::class)
    fun retrieveFileId(dirName: String): String {
        LOG.debug("Retrieving drive id for dir " + dirName)

        val result = drive.files().list()
                .set("q", "trashed = false and name='" +
                        dirName +
                        "'")
                .setPageSize(10)
                .setFields("nextPageToken, files(id)")
                .execute()
        val files = result.files
        if (files == null || files.size == 0) {
            val message = "No dir found named" + dirName
            throw RuntimeException(message)
        } else if (files.size > 1) {
            val message = "More than on dir found named" + dirName
            throw RuntimeException(message)
        }
        val file = files[0]
        LOG.debug("Id is " + file.id)
        return file.id
    }


    @Throws(IOException::class)
    fun downloadTempFile(fileId: String, fileName: String): Path {
        val tempFileName = Files.createTempFile("shoeb_", fileName)
        val outputStream = FileOutputStream(tempFileName.toFile())
        drive.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)
        outputStream.close()
        return tempFileName
    }

    @Throws(IOException::class)
    fun moveFileToUploadedDir(id: String, sourceDir: String, destDir: String) {

        val update = drive.files().update(id, null)
        update.removeParents = sourceDir
        update.addParents = destDir
        update.fields = "id, parents"
        val execute = update.execute()
    }

    @Throws(IOException::class)
    fun retrieveFileToUpload(toUploadDirId: String): Set<File> {
        LOG.debug("Retrieve files from " + toUploadDirId)
        val wholeSet = HashSet<File>()

        var fileResult = drive.files().list()
                .set("q", "'$toUploadDirId' in parents")
                .setPageSize(50)
                .setFields("files(id, originalFilename),nextPageToken")
                .execute()
        var fileToUpload = fileResult.files
        wholeSet.addAll(fileToUpload)
        var nextPageToken: String? = fileResult.nextPageToken
        LOG.debug("next Page Token : " + nextPageToken!!)
        while (nextPageToken != null) {
            LOG.debug("retrieve new result")
            fileResult = drive.files().list()
                    .set("q", "trashed = false and '$toUploadDirId' in parents")
                    .setPageToken(nextPageToken)
                    .setPageSize(50)
                    .setFields("files(id, originalFilename),nextPageToken")
                    .execute()
            fileToUpload = fileResult.files
            nextPageToken = fileResult.nextPageToken
            LOG.debug("next Page Token : " + nextPageToken!!)
            wholeSet.addAll(fileToUpload)
        }
        return wholeSet
    }

    companion object {
        private val LOG = LogFactory.getLog(DriveService::class.java)
    }

    fun getInputStreamForFile(file: File): InputStream =
        drive.files().get(file.id).executeMediaAsInputStream()

}