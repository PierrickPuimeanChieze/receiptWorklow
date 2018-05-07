package com.cleitech.receipt.properties;

import com.cleitech.receipt.shoeboxed.domain.ProcessingState;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import java.io.File;

@ConfigurationProperties(prefix = "shoeboxed", ignoreUnknownFields = false)
@Configuration
public class ShoeboxedProperties {

    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    private ProcessingState uploadProcessingState = ProcessingState.NEEDS_SYSTEM_PROCESSING;
    @NotBlank
    private String redirectUrl;
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

    public String getToSentCategory() {
        return toSentCategory;
    }

    public void setToSentCategory(String toSentCategory) {
        this.toSentCategory = toSentCategory;
    }
}
