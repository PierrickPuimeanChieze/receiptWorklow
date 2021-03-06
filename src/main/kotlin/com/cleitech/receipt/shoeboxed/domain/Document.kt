package com.cleitech.receipt.shoeboxed.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

/**
 * @author Pierrick Puimean-Chieze on 23-04-16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Document(val id: String? = null,
                    var categories: List<String> = emptyList(),
                    var vendor: String = "",
                    var total: Double? = null) {


    var attachment: Attachment? = null

    private val tax: Double? = null
    private val currency: String? = null
    var issued: Date? = null
    var uploaded: Date? = null
    var notes: String? = null
    var type: String? = null


}
