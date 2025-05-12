package dev.handsup.common.support;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@DisplayName("[스프링 컨테이너에 등록된 Bean 조회하기]")
@SpringBootTest
class ApplicationContextInfoTest extends ApiTestSupport {

    @Autowired
    ApplicationContext ac;

    @Test
    void printApplicationBeansOnly() {
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) ac;

        String[] beanNames = ac.getBeanDefinitionNames();
        Arrays.stream(beanNames)
            .filter(name -> {
                BeanDefinition bd = configurableContext.getBeanFactory().getBeanDefinition(name);
                return bd.getRole() == BeanDefinition.ROLE_APPLICATION;
            })
            .sorted()
            .forEach(name -> {
                Object bean = ac.getBean(name);
                System.out.println("📦 " + name + " => " + bean.getClass().getSimpleName());
            });
    }
}
