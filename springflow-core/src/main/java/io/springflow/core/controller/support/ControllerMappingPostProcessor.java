package io.springflow.core.controller.support;

import io.springflow.core.controller.GenericCrudController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * Post-processor that registers dynamically generated controllers with Spring MVC.
 * <p>
 * This component detects GenericCrudController instances created by SpringFlowControllerFactoryBean
 * and registers their endpoints with the RequestMappingHandlerMapping.
 * </p>
 * <p>
 * Note: This is a simplified implementation for Phase 1 MVP.
 * Full controller registration will be enhanced in Module 9.
 * </p>
 */
@Component
public class ControllerMappingPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ControllerMappingPostProcessor.class);

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public ControllerMappingPostProcessor(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof GenericCrudController) {
            log.debug("Detected GenericCrudController bean: {}", beanName);
            // Note: Actual endpoint registration will be implemented in Module 9
            // For now, we just log that the controller was detected
            // The controller needs @RestController and @RequestMapping to be fully functional
        }
        return bean;
    }
}
