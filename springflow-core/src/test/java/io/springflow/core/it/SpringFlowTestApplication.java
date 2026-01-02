package io.springflow.core.it;

import io.springflow.core.config.PageableProperties;
import io.springflow.core.filter.FilterResolver;
import io.springflow.core.mapper.DtoMapperFactory;
import io.springflow.core.validation.EntityValidator;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;

import org.springframework.context.annotation.Import;
import io.springflow.core.repository.AutoApiRepositoryRegistrar;
import io.springflow.core.controller.support.RequestMappingRegistrar;
import io.springflow.core.controller.GlobalExceptionHandler;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@SpringBootApplication(scanBasePackages = "io.springflow.core.it")
@Import({
    AutoApiRepositoryRegistrar.class,
    RequestMappingRegistrar.class,
    GlobalExceptionHandler.class
})
@EntityScan(basePackages = "io.springflow.core.it.entity")
@EnableWebSecurity
public class SpringFlowTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringFlowTestApplication.class, args);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PageableProperties pageableProperties() {
        return new PageableProperties();
    }

    @Bean
    public DtoMapperFactory dtoMapperFactory(EntityManager entityManager) {
        return new DtoMapperFactory(entityManager);
    }

    @Bean
    public FilterResolver filterResolver(ConversionService conversionService) {
        return new FilterResolver(conversionService);
    }

    @Bean
    public EntityValidator entityValidator(Validator validator) {
        return new EntityValidator(validator);
    }
}
