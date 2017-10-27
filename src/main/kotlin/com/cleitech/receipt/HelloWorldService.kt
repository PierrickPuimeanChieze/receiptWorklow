package com.cleitech.receipt

import org.springframework.stereotype.Service

@Service
class HelloWorldService(private val configuration: ServiceProperties) {

    fun getHelloMessage(name: String): String {
        return this.configuration.greeting + " " + name
    }

}