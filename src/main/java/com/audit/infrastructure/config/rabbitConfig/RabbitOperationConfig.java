package com.audit.infrastructure.config.rabbitConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitOperationConfig {

}
