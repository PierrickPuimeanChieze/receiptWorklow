package com.cleitech.receipt

import org.springframework.integration.annotation.MessagingGateway

@MessagingGateway(defaultRequestChannel = "inputChannel")
interface SampleMessageGateway {

    fun echo(message: String)

}