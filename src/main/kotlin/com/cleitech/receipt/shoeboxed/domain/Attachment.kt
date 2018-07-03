package com.cleitech.receipt.shoeboxed.domain

import java.net.URL

/**
 * @author Pierrick Puimean-Chieze on 23-04-16.
 */
class Attachment {

    var name: String? = null
    var url: URL? = null

    override fun toString(): String {
        return "Attachment{" +
                "name='" + name + '\''.toString() +
                ", url=" + url +
                '}'.toString()
    }
}
