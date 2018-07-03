package com.cleitech.receipt.shoeboxed.domain

import java.util.*

/**
 * @author Pierrick Puimean-Chieze on 23-04-16.
 */
class Documents {

    val documents = LinkedList<Document>()

    fun setDocuments(documents: List<Document>) {
        this.documents.clear()
        this.documents.addAll(documents)
    }
}
