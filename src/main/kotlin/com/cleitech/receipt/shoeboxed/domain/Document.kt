package com.cleitech.receipt.shoeboxed.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

/**
 * @author Pierrick Puimean-Chieze on 23-04-16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class Document {


    var attachment: Attachment? = null
    var total: Double? = null
    private val tax: Double? = null
    private val currency: String? = null
    var issued: Date? = null
    var uploaded: Date? = null
    var vendor: String? = null
    var notes: String? = null
    var categories: List<String>? = null
    var type: String? = null

    var id: String? = null

    override fun toString(): String {
        return "Document{" +
                "attachment=" + attachment +
                ", total=" + total +
                ", tax=" + tax +
                ", currency='" + currency + '\''.toString() +
                ", issued=" + issued +
                ", uploaded=" + uploaded +
                ", vendor='" + vendor + '\''.toString() +
                ", notes='" + notes + '\''.toString() +
                ", categories=" + categories +
                ", id='" + id + '\''.toString() +
                '}'.toString()
    }
}
