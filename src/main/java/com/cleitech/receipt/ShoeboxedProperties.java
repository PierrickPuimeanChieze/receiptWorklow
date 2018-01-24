package com.cleitech.receipt;

import com.cleitech.receipt.shoeboxed.domain.ProcessingState;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ConfigurationProperties(prefix = "shoeboxed", ignoreUnknownFields = false)
@ManagedResource
public class ShoeboxedProperties {

    private String clientId;
    private String clientSecret;
    private ProcessingState uploadProcessingState = ProcessingState.NEEDS_SYSTEM_PROCESSING;
    private String redirectUrl;
    private String username;
    private String password;
    private String toSentCategory;

    @ManagedAttribute
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @ManagedAttribute
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @ManagedAttribute
    public ProcessingState getUploadProcessingState() {
        return uploadProcessingState;
    }

    public void setUploadProcessingState(ProcessingState uploadProcessingState) {
        this.uploadProcessingState = uploadProcessingState;
    }

    @ManagedAttribute
    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @ManagedAttribute
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @ManagedAttribute
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ManagedAttribute
    public String getToSentCategory() {
        return toSentCategory;
    }

    public void setToSentCategory(String toSentCategory) {
        this.toSentCategory = toSentCategory;
    }
}
