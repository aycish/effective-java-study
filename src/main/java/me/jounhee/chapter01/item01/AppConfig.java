package me.jounhee.chapter01.item01;

import me.jounhee.chapter01.item01.hello.ChineseHelloService;
import me.jounhee.chapter01.item01.hello.HelloService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public HelloService helloService() {
        return new ChineseHelloService();
    }

}
