package com.cleitech.receipt.shoeboxed.domain

import java.util.*

data class Categories(val categories: LinkedList<Category> = LinkedList<Category>())

data class Category(var id: String? = null, var name: String)
