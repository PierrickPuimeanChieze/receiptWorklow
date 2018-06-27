package com.cleitech.receipt

import com.cleitech.receipt.properties.ShoeboxedProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.time.Instant

@Component
class ShoeboxedAuthenticateDao(@Autowired val jacksonObjectMapper: ObjectMapper,
                               @Autowired private val shoeboxedProperties: ShoeboxedProperties) {

    val accessTokenFile: File = shoeboxedProperties.accessTokenFile

    fun storeTokenInfo(tokenInfo: ShoeboxedTokenInfo) = jacksonObjectMapper.writeValue(accessTokenFile, tokenInfo)

    fun getTokenInfo(): ShoeboxedTokenInfo {
        return jacksonObjectMapper.readValue(accessTokenFile)
    }

    fun isTokenInfoAvailable(): Boolean = (accessTokenFile.exists())

    fun getAccessToken(): String = getTokenInfo().accessToken

    fun getRefreshToken(): String = getTokenInfo().refreshToken

    fun getTokenType(): String = getTokenInfo().tokenType

    fun getExpiresIn(): Int = getTokenInfo().expiresIn

    fun getScope(): String = getTokenInfo().scope

    fun getLastRefresh(): Instant = getTokenInfo().lastRefresh
}