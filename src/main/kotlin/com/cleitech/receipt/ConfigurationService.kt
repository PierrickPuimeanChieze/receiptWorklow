package com.cleitech.receipt

import org.springframework.stereotype.Component


data class DriveToOCREntry(
        val toScanDirId : String,
        val uploadedDirId : String,
        val ocrCategory : String
)

@Component
class ConfigurationService {
    fun driveToOcrEntries() : List<DriveToOCREntry>
            = listOf(
            DriveToOCREntry(
                    "0B9I1Fu1LKhuoSnloT2hCVng0UUE",
                    "0B9I1Fu1LKhuoXzBlbGFBWlFPMDg",
                    "toSentDummy"
                    )
    )
}