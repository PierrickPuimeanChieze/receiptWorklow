package com.cleitech.receipt.properties;

import com.cleitech.receipt.shoeboxed.domain.ProcessingState;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@ConfigurationProperties(prefix = "shoeboxed", ignoreUnknownFields = false)
@Configuration
public class ShoeboxedProperties {

    private String clientId;
    private String clientSecret;
    private ProcessingState uploadProcessingState = ProcessingState.NEEDS_SYSTEM_PROCESSING;
    private String redirectUrl;
    private String username;
    private String password;
    private String toSentCategory;
    private File accessTokenFile;

    public File getAccessTokenFile() {
        return accessTokenFile;
    }

    public void setAccessTokenFile(File accessTokenFile) {
        this.accessTokenFile = accessTokenFile;
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public ProcessingState getUploadProcessingState() {
        return uploadProcessingState;
    }

    public void setUploadProcessingState(ProcessingState uploadProcessingState) {
        this.uploadProcessingState = uploadProcessingState;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToSentCategory() {
        return toSentCategory;
    }

    public void setToSentCategory(String toSentCategory) {
        this.toSentCategory = toSentCategory;
    }
}
