package dev.handsup.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(ignoreResourceNotFound = true,
        value = {
                "classpath:application-core-${spring.profiles.active}.yml"
        },
        factory = YamlPropertySourceFactory.class)
public class PropertiesConfig {
}