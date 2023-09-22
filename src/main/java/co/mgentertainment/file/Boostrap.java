package co.mgentertainment.file;

import cn.xuyanwu.spring.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"co.mgentertainment.file"}, exclude = {SecurityAutoConfiguration.class})
@EnableFileStorage
public class Boostrap {

    public static void main(String[] args) {
        SpringApplication.run(Boostrap.class, args);
    }
}