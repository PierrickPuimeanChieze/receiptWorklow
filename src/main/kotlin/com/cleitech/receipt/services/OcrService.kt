package com.cleitech.receipt.services

import java.io.InputStream

interface OcrService {
    fun uploadDocument(documentContent: InputStream)
}
