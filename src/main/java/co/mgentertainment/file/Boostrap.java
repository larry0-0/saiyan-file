package co.mgentertainment.file;

import cn.xuyanwu.spring.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"co.mgentertainment.file"})
@EnableFileStorage
public class Boostrap {

    public static void main(String[] args) {
        SpringApplication.run(Boostrap.class, args);
    }
}