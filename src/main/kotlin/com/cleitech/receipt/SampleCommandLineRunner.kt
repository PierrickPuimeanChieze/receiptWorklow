package com.cleitech.receipt

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class SampleCommandLineRunner(private val gateway: SampleMessageGateway) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        for (arg in args) {
            this.gateway.echo(arg)
        }
    }

}