package com.cleitech.receipt.services

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
@Profile("ocr-dummy")
class DummyOcrService : OcrService {
    override fun uploadDocument(documentContent: InputStream) {
        println("Received a file to upload to OCR")

    }
}