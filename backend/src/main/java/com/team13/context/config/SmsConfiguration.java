package com.team13.context.config;

import com.team13.context.auth.AuthVerificationStore;
import com.team13.context.auth.sms.AliyunPnvsSmsVerificationClient;
import com.team13.context.auth.sms.LocalSmsVerificationClient;
import com.team13.context.auth.sms.SmsVerificationClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.auth.sms-provider", havingValue = "aliyun")
    public SmsVerificationClient aliyunPnvsSmsClient(
            AuthProperties authProperties,
            AuthVerificationStore verificationStore) {
        return new AliyunPnvsSmsVerificationClient(authProperties, verificationStore);
    }

    @Bean
    @ConditionalOnMissingBean(SmsVerificationClient.class)
    public SmsVerificationClient localSmsVerificationClient(
            AuthProperties authProperties,
            AuthVerificationStore verificationStore) {
        return new LocalSmsVerificationClient(authProperties, verificationStore);
    }
}
