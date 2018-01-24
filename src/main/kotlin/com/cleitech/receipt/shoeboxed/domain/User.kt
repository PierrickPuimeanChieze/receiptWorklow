package com.cleitech.receipt.shoeboxed.domain

import java.util.*

/**
 * @author Pierrick Puimean-Chieze on 23-04-16.
 */
data class User(var accounts: Array<Account>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (!Arrays.equals(accounts, other.accounts)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(accounts)
    }
}
