package com.cleitech.receipt

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.string
import org.springframework.stereotype.Component
import java.time.Instant


data class DriveToOCREntry(
        val toScanDirId: String,
        val uploadedDirId: String,
        val ocrCategory: String?,
        val dropboxDir:String?
)

data class ShoeboxedTokenInfo (var accessToken: String,
                               val refreshToken: String,
                               val tokenType: String,
                               var expiresIn: Int,
                               val scope: String,
                               var lastRefresh : Instant)


@Component
class ConfigurationService {

    private fun parse(name: String): Any? = Parser().parse(name)

    fun driveToOcrEntries(): List<DriveToOCREntry> {
        val obj = parse("driveToOcr.json") as JsonArray<JsonObject>
        return obj.map {
            DriveToOCREntry(
                    it.string("toScanDirId")!!,
                    it.string("uploadedDirId")!!,
                    it.string("ocrCategory"),
                    it.string("dropboxDir")

            )
        }
    }
}