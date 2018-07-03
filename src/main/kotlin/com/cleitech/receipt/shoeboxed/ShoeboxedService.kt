package com.cleitech.receipt.shoeboxed

import com.cleitech.receipt.ShoeboxedTokenInfo
import com.cleitech.receipt.properties.ShoeboxedProperties
import com.cleitech.receipt.services.OcrException
import com.cleitech.receipt.services.OcrService
import com.cleitech.receipt.shoeboxed.domain.*
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.GsonHttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * @author Pierrick Puimean-Chieze on 27-12-16.
 */
@Service
@Profile("ocr-shoeboxed")
class ShoeboxedService(@Autowired private val shoeboxedProperties: ShoeboxedProperties,
                       @Autowired private val shoeboxedAuthenticateDao: ShoeboxedAuthenticateDao) : OcrService {


    private val restTemplate = RestTemplate()
    private val redirectUrl: String = shoeboxedProperties.redirectUrl
    private val clientId: String = shoeboxedProperties.clientId
    private val clientSecret: String = shoeboxedProperties.clientSecret
    private val processingState: ProcessingState = shoeboxedProperties.uploadProcessingState
    init {
        val formHttpMessageConverter = FormHttpMessageConverter()
        restTemplate.messageConverters.add(formHttpMessageConverter)
        restTemplate.messageConverters.add(GsonHttpMessageConverter())
        val authenticationInterceptor = ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            if (!request.uri.toString().startsWith(TOKEN_URL)) {
                refreshTokenIfNeeded()
                request.headers.accept = listOf(MediaType.APPLICATION_JSON)
                val token = shoeboxedAuthenticateDao.getAccessToken()
                request.headers.add("Authorization", "Bearer $token")
            }
            execution.execute(request, body)
        }

        restTemplate.interceptors.add(authenticationInterceptor)

    }


    /**
     * Allow to retrieve an acess token
     *
     * @return the access token to retrieve
     */
    @Throws(IOException::class)
    fun retrieveAndWriteAccessToken(code: String) {

        val lastRefresh = Instant.now()

        val tokenUrl = UriComponentsBuilder.fromUriString(TOKEN_URL)
                .queryParam("grant_type", "authorization_code")
                .queryParam("code", code)
                .queryParam("redirect_uri", redirectUrl)
                .build().toUriString()


        val headers = buildHeadersFromClientInfo()

        try {
            val exchange = restTemplate.exchange(tokenUrl, HttpMethod.POST, HttpEntity<ShoeboxedTokenInfo>(headers), ShoeboxedTokenInfo::class.java)

            val shoeboxedTokenInfo = exchange.body!!
            shoeboxedTokenInfo.lastRefresh = lastRefresh
            shoeboxedAuthenticateDao.storeTokenInfo(shoeboxedTokenInfo)
        } catch (ex: HttpClientErrorException) {
            println(ex.responseBodyAsString)
            throw ex
        }

    }

    private fun buildHeadersFromClientInfo(): HttpHeaders {
        val headers = HttpHeaders()


        val auth = clientId + ":" + clientSecret
        val encodedAuth = Base64.encodeBase64String(
                auth.toByteArray(Charset.forName("US-ASCII")))
        val authHeader = "Basic " + encodedAuth
        headers.set("Authorization", authHeader)
        return headers
    }

    /**
     * Allow to upload a document
     *
     * @param tempFileName the path to thedocument to upload
     * @return the status of the upload
     */
    override fun uploadDocument(documentContent: InputStream) {

        val uriComponentsBuilder = UriComponentsBuilder.fromUriString("https://api.shoeboxed.com/v2/accounts/{accountId}/documents/?")
        //        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost:9999/test");
        val url = uriComponentsBuilder.buildAndExpand(retrieveAccountId()).toUriString()


        val resourceToUpload = InputStreamResource(documentContent)
        val body = LinkedMultiValueMap<String, Any>()
        body.add("attachment", resourceToUpload)
        body.add("document", "{ \"processingState\": \"" +
                processingState +
                "\", \"type\":\"receipt\"}")
        val entity = HttpEntity<MultiValueMap<String, Any>>(body, buildHeadersFromAccessToken())
        val stringResponseEntity = restTemplate.postForEntity(url, entity, String::class.java)

        if (stringResponseEntity.statusCode !in listOf(HttpStatus.ACCEPTED, HttpStatus.CREATED, HttpStatus.OK)) {
            throw OcrException("Bad status code when uploading file : ${stringResponseEntity}")
        }
    }

    fun getCategories(accountId: String): LinkedList<Category> {
        val uri = UriComponentsBuilder.fromUriString("https://api.shoeboxed.com/v2//accounts/{account}/categories/?")
                .buildAndExpand(accountId).toUri()

        val responseEntity = restTemplate.getForEntity(uri, Categories::class.java)
        return responseEntity.body?.categories ?: LinkedList()

    }

    private fun buildHeadersFromAccessToken(): HttpHeaders {
        refreshTokenIfNeeded()
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.set("Authorization", "Bearer " + shoeboxedAuthenticateDao.getAccessToken())
        return headers
    }

    private fun refreshTokenIfNeeded() {
        if (!shoeboxedAuthenticateDao.isTokenInfoAvailable()) {
            throw IllegalStateException("No Shoeboxed authentication available")
        }

        val now = Instant.now()


        val accessTokenInfo = shoeboxedAuthenticateDao.getTokenInfo()
        val accessTokenLastRefresh = accessTokenInfo.lastRefresh
        var expiresIn = accessTokenInfo.expiresIn
        val secondsSinceLastRefresh = Duration.between(accessTokenLastRefresh, now).seconds
        if (secondsSinceLastRefresh > expiresIn) {
            throw IllegalStateException("Shoeboxed authentication information stale. Please reauthenticate")
        }

        val securityMargin = 0.9F

        if (secondsSinceLastRefresh > expiresIn * securityMargin) {
            val tokenUrl = UriComponentsBuilder.fromUriString(TOKEN_URL)
                    .queryParam("grant_type", "refresh_token")
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", redirectUrl)
                    .queryParam("refresh_token", accessTokenInfo.refreshToken)
                    .build().toUriString()

            val refreshResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, HttpEntity<ShoeboxedTokenInfo>(buildHeadersFromClientInfo()), ShoeboxedTokenInfo::class.java)
            if (refreshResponse.statusCode == HttpStatus.OK) {
                accessTokenInfo.lastRefresh = now
                val partialTokenInfo = refreshResponse.body!!
                accessTokenInfo.accessToken = partialTokenInfo.accessToken
                accessTokenInfo.expiresIn = partialTokenInfo.expiresIn
                shoeboxedAuthenticateDao.storeTokenInfo(accessTokenInfo)
            }

        }
    }


    /**
     * Allow to retrieve the first accounts Id
     *
     * @return the first accounts Id
     */
    fun retrieveAccountId(): String {

        val usersAccountUri = UriComponentsBuilder.fromUriString("https://api.shoeboxed.com:443/v2/user/")

        val exchange = restTemplate.getForEntity(usersAccountUri.build().toUri(),
                User::class.java)

        val body = exchange.body
        return body!!.accounts[0].id!!

    }

    fun retrieveCategory(accountId: String, categoryName: String): Category? = getCategories(accountId).find { category -> category.name.equals(categoryName) }

    /**
     * Allow to retrieve document
     *
     * @param categoryFilter the category of the searched document
     * @return the list of Document
     */
    fun retrieveDocument(accountId: String, categoryFilter: String? = null): LinkedList<Document> {

        val getDocumentsAccountUri = UriComponentsBuilder.fromUriString("https://api.shoeboxed.com:443/v2/accounts/{accountId}/documents/")
                .queryParam("limit", 100)
                .queryParam("type", "receipt")
                .queryParam("trashed", false)
        if (categoryFilter != null) {
            getDocumentsAccountUri.queryParam("category", categoryFilter)
        }

        //TODO extract the use of retrieveAccountId
        //We retrieve the documents metadata

        val url = getDocumentsAccountUri.buildAndExpand(accountId).toUri()
        val documentsResponse = restTemplate.getForEntity(url, Documents::class.java)

        return documentsResponse.body!!.documents

    }

    fun updateMetadata(documentId: String, categories: List<String>) {

        val getDocumentsAccountUri = UriComponentsBuilder.fromUriString("https://api.shoeboxed.com:443/v2/accounts/{accountId}/documents/{documentId}")
        val newMetadata = Document()
        newMetadata.categories = categories

        val entity = HttpEntity(newMetadata, buildHeadersFromAccessToken())
        val url = getDocumentsAccountUri.buildAndExpand(retrieveAccountId(), documentId).toUri()
        restTemplate.exchange(url, HttpMethod.PUT, entity, String::class.java)

    }

    companion object {
        private val TOKEN_URL = "https://id.shoeboxed.com/oauth/token"
        private val RESPONSE_TYPE = "code"
        private val SCOPE = "all"
    }
}
