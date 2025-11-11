package pl.mlodyg.spotifer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class CryptoConfig {
    @Bean
    public TextEncryptor textEncryptor(
            @Value("${APP_ENC_PASSWORD:change-me}") String password,
            @Value("${APP_ENC_SALT:0123456789ABCDEF}") String saltHex
    ) {
// For prod use strong key mgmt (KMS/HashiCorp Vault) and prefer AES-GCM where possible
        return Encryptors.delux(password, saltHex);
    }
}
