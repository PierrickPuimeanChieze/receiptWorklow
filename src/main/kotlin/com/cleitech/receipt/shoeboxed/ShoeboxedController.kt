package com.cleitech.receipt.shoeboxed

import com.cleitech.receipt.shoeboxed.domain.Category
import com.cleitech.receipt.shoeboxed.domain.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/shoeboxed")
class ShoeboxedController(val shoeboxedService: ShoeboxedService, @Value("\${shoeboxed.toSentCategory.name}") val toSentCategoryName: String
) {


    @GetMapping("/categories")
    fun getCategories(): Category? = shoeboxedService.retrieveCategory(shoeboxedService.retrieveAccountId(), toSentCategoryName)

    @GetMapping("/documents")
    fun getDocuments(): LinkedList<Document> = shoeboxedService.retrieveDocument(shoeboxedService.retrieveAccountId())
}