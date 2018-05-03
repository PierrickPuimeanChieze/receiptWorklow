package com.cleitech.receipt.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "receipt-workflow")
@Configuration
public class ReceiptWorkflowProperties {

    private Mail mail;

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public static class Mail {

        private long aggregationWait;

        public long getAggregationWait() {
            return aggregationWait;
        }

        public void setAggregationWait(long aggregationWait) {
            this.aggregationWait = aggregationWait;
        }
    }
}
