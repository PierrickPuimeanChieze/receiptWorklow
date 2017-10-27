package com.cleitech.receipt

import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.util.StreamUtils
import java.io.File
import java.io.FileInputStream

@MessageEndpoint
class SampleEndpoint(private val helloWorldService: HelloWorldService) {

    @ServiceActivator
    @Throws(Exception::class)
    fun hello(input: File): String {
        val inStream = FileInputStream(input)
        val name = String(StreamUtils.copyToByteArray(inStream))
        inStream.close()
        return this.helloWorldService.getHelloMessage(name)
    }

    @ServiceActivator
    @Throws(Exception::class)
    fun hello(name: String): String {
        return this.helloWorldService.getHelloMessage(name)
    }
}