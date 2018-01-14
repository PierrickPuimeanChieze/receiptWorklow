package com.cleitech.receipt.services

import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class DummyOcrService : OcrService {
    override fun uploadFile(requestMessage: InputStream) {
        println("Received a file to upload to OCR")

    }
}