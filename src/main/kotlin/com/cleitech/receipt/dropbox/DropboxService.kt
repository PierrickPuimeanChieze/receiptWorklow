package com.cleitech.receipt.dropbox

import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Created by ppc on 1/30/2017.
 */
class DropboxService(private val uploadPath: String, accessToken: String) {

    private val client: DbxClientV2


    init {
        val config = DbxRequestConfig.newBuilder("receipt-workflow/beta").withUserLocale("en_US").build()
        this.client = DbxClientV2(config, accessToken)
    }

    @Throws(DbxException::class, IOException::class)
    fun uploadFile(fileToUpload: File, fileName: String) {
        // Upload file to Dropbox
        FileInputStream(fileToUpload).use { `in` ->
            client.files().uploadBuilder("$uploadPath/$fileName")
                    .uploadAndFinish(`in`)
        }
    }

    @Throws(DbxException::class)
    fun testCheck() {
        val account = client.users().currentAccount
        println(account.name)
    }
}
