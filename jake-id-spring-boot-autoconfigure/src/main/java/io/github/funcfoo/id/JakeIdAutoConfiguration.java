package io.github.funcfoo.id;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(JakeId.class)
@EnableConfigurationProperties
public class JakeIdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JakeIdProperties jakeIdProperties() {
        return new JakeIdProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public JakeId jakeId(JakeIdProperties jakeIdProperties) {
        return jakeIdProperties.toBuilder().build();
    }
}
