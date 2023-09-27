package co.mgentertainment.file;

import cn.xuyanwu.spring.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages = {"co.mgentertainment.file"}, exclude = {SecurityAutoConfiguration.class})
@EnableFileStorage
@EnableRetry(proxyTargetClass = true)
public class Boostrap {

    public static void main(String[] args) {
        SpringApplication.run(Boostrap.class, args);
    }
}