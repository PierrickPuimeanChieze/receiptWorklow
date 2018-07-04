package com.cleitech.receipt.dropbox

import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import java.io.InputStream

/**
 * Created by ppc on 1/30/2017.
 */
class DropboxService(accessToken: String) {

    private val client: DbxClientV2


    init {
        val config = DbxRequestConfig.newBuilder("receipt-workflow/beta").withUserLocale("en_US").build()
        this.client = DbxClientV2(config, accessToken)
    }

    fun uploadStream(fullUploadPath: String, inputStream: InputStream) =
            client.files().uploadBuilder(fullUploadPath)
                    .uploadAndFinish(inputStream)

    @Throws(DbxException::class)
    fun testCheck() {
        val account = client.users().currentAccount
        println(account.name)
    }
}
