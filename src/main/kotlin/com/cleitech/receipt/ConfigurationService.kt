package com.cleitech.receipt


import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.time.Instant


data class DriveToOCREntry(
        val toScanDirId: String,
        val uploadedDirId: String,
        val ocrCategory: String?,
        val dropboxDir: String?
)

data class ShoeboxedTokenInfo(@JsonProperty("access_token") var accessToken: String,
                              @JsonProperty("refresh_token") val refreshToken: String,
                              @JsonProperty("token_type") val tokenType: String,
                              @JsonProperty("expires_in") var expiresIn: Int,
                              val scope: String,
                              @JsonProperty("last_refresh") var lastRefresh: Instant = Instant.now())


@Component
class ConfigurationService(@Autowired val om: ObjectMapper) {

    private final val entries: Array<DriveToOCREntry> = om.readValue(File("driveToOcr.json"))

    fun driveToOcrEntries(): Array<DriveToOCREntry> = entries
}