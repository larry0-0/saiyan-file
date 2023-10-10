package co.mgentertainment.file;

import co.mgentertainment.common.fileupload.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages = {"co.mgentertainment.file"}, exclude = {SecurityAutoConfiguration.class})
@EnableFileStorage
@EnableRetry(proxyTargetClass = true)
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }
}