package com.cleitech.receipt

import com.cleitech.receipt.headers.OperationHeaders
import org.springframework.integration.aggregator.MessageGroupProcessor
import org.springframework.integration.file.FileHeaders
import org.springframework.integration.mail.MailHeaders
import org.springframework.integration.store.MessageGroup
import org.springframework.messaging.support.MessageBuilder

class MailAggregator(val subject: String, val to: String, val from: String) : MessageGroupProcessor {
    override fun processMessageGroup(group: MessageGroup): Any {

        print("test")
        val mapValues: Map<Boolean?, List<String>> =
        //First we group the message by the result of the OCR treatment
        //TODO manage file with no OCR status
        //TODO generify grouping
                group.messages.groupBy { it.headers[OperationHeaders.OCR_TREATMENT_SUCCESS] as Boolean? }
                        //Then we convert each list of message in a string
                        .mapValues { value -> value.value.map { it.headers[FileHeaders.FILENAME] as String } }
        val payloadBuilder = MessageBuilder.withPayload(
                createBody(mapValues[true], mapValues[false])
        )
                .setHeader(MailHeaders.SUBJECT, subject)
                .setHeader(MailHeaders.TO, to)
                .setHeader(MailHeaders.FROM, from)
        return payloadBuilder.build()
    }

    //TODO use template
    private fun createBody(treatedFiles: List<String>?, errorFiles: List<String>?): String {
        val builder = StringBuilder()
        builder.append("Les fichiers suivants ont été publiés vers l'outil d'OCR :\n")
        treatedFiles?.forEach { builder.append("$it\n") }
        builder.append("Les fichiers suivants ont soulevés une erreur :\n")
        errorFiles?.forEach { builder.append("$it\n") }
        builder.append("Ces fichiers continueront à être marqué en erreur tant qu'ils ne seront pas retirés manuellement de la source.\n")
        return builder.toString()
    }
}