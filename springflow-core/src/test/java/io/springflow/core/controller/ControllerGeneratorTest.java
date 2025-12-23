package io.springflow.core.controller;

import io.springflow.annotations.AutoApi;
import io.springflow.core.controller.support.SpringFlowControllerFactoryBean;
import io.springflow.core.metadata.EntityMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerGeneratorTest {

    @Test
    void generate_shouldRegisterControllerBeanDefinition() {
        // Given
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ControllerGenerator generator = new ControllerGenerator(registry);

        AutoApi autoApiConfig = createAutoApiMock("/api/products");
        EntityMetadata metadata = new EntityMetadata(
                (Class) TestProduct.class,
                Long.class,
                "Product",
                "product",
                autoApiConfig,
                java.util.Collections.emptyList()
        );

        // Register mock service bean (required by controller)
        registry.registerSingleton("productService", new Object());

        // When
        generator.generate(metadata);

        // Then
        assertThat(registry.containsBeanDefinition("productController")).isTrue();

        BeanDefinition bd = registry.getBeanDefinition("productController");
        assertThat(bd.getBeanClassName()).isEqualTo(SpringFlowControllerFactoryBean.class.getName());
        assertThat(bd.getPropertyValues().get("entityClass")).isEqualTo(TestProduct.class);
    }

    @Test
    void getControllerBeanName_shouldReturnCorrectName() {
        // Given
        EntityMetadata metadata = new EntityMetadata(
                (Class) TestProduct.class,
                Long.class,
                "Product",
                "product",
                null,
                java.util.Collections.emptyList()
        );

        // When
        String beanName = ControllerGenerator.getControllerBeanName(metadata);

        // Then
        assertThat(beanName).isEqualTo("productController");
    }

    @Test
    void generate_withDefaultPath_shouldUseConvention() {
        // Given
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ControllerGenerator generator = new ControllerGenerator(registry);

        EntityMetadata metadata = new EntityMetadata(
                (Class) TestProduct.class,
                Long.class,
                "Product",
                "product",
                null, // No AutoApi config, should use default
                java.util.Collections.emptyList()
        );

        registry.registerSingleton("productService", new Object());

        // When
        generator.generate(metadata);

        // Then
        assertThat(registry.containsBeanDefinition("productController")).isTrue();
        BeanDefinition bd = registry.getBeanDefinition("productController");
        // Note: The actual path is stored in attributes
        assertThat(bd.getAttribute("requestMapping")).isEqualTo("/products");
    }

    // Helper method to create AutoApi mock
    private AutoApi createAutoApiMock(String path) {
        return new AutoApi() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return AutoApi.class;
            }

            @Override
            public String path() {
                return path;
            }

            @Override
            public io.springflow.annotations.Expose expose() {
                return io.springflow.annotations.Expose.ALL;
            }

            @Override
            public io.springflow.annotations.Security security() {
                return new io.springflow.annotations.Security() {
                    @Override
                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                        return io.springflow.annotations.Security.class;
                    }

                    @Override
                    public boolean enabled() {
                        return true;
                    }

                    @Override
                    public io.springflow.annotations.SecurityLevel level() {
                        return io.springflow.annotations.SecurityLevel.PUBLIC;
                    }

                    @Override
                    public io.springflow.annotations.SecurityLevel readLevel() {
                        return io.springflow.annotations.SecurityLevel.UNDEFINED;
                    }

                    @Override
                    public io.springflow.annotations.SecurityLevel writeLevel() {
                        return io.springflow.annotations.SecurityLevel.UNDEFINED;
                    }

                    @Override
                    public String[] roles() {
                        return new String[0];
                    }

                    @Override
                    public String[] authorities() {
                        return new String[0];
                    }
                };
            }

            @Override
            public boolean pagination() {
                return true;
            }

            @Override
            public boolean sorting() {
                return true;
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public String[] tags() {
                return new String[0];
            }
        };
    }

    // Test class
    static class TestProduct {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
