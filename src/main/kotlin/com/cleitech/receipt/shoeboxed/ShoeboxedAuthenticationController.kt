package com.cleitech.receipt.shoeboxed

import com.cleitech.receipt.properties.ShoeboxedProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Controller
@RequestMapping("/shoeboxed/authenticate")
class ShoeboxedAuthenticationController(@Autowired private val shoeboxedProperties: ShoeboxedProperties,
                                        @Autowired val shoeboxedService: ShoeboxedService) {
    companion object {
        private val TOKEN_URL = "https://id.shoeboxed.com/oauth/token"
        private val RESPONSE_TYPE = "code"
        private val SCOPE = "all"
    }

    private val restTemplate = RestTemplate()
    private val redirectUrl: String = shoeboxedProperties.redirectUrl
    val clientId: String = shoeboxedProperties.clientId

    @GetMapping("")
    fun authenticate(model: Model): String {


        val oauthUrl = UriComponentsBuilder.fromUriString("http://id.shoeboxed.com/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("response_type", RESPONSE_TYPE)
                .queryParam("scope", SCOPE)
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("state", "CRT")
                .build().toUriString()

        model.addAttribute("url", oauthUrl)

        return "shoeboxed/authenticate"
    }

    @GetMapping("/callback")

    fun callback(code: String): String {
        shoeboxedService.retrieveAndWriteAccessToken(code)
        return "shoeboxed/callback"

    }
}