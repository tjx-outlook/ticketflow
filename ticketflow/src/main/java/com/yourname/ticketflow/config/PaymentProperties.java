package com.yourname.ticketflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
    private Alipay alipay = new Alipay();

    @Data
    public static class Alipay {
        private boolean enabled;
        private String appId;
        private String privateKey;
        private String alipayPublicKey;
        private String notifyUrl;
        private String gateway = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    }

}
