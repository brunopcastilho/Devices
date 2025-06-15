package com.bcastilho.example.webserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class WebserverApplication {

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(WebserverApplication.class, args);
        String[] beanNames = context.getBeanNamesForType(RequestMappingHandlerMapping.class);
        for (String beanName : beanNames) {
            RequestMappingHandlerMapping mapping = (RequestMappingHandlerMapping) context.getBean(beanName);
            mapping.getHandlerMethods().forEach((key, value) -> System.out.println(key));
        }

    }

}
