package co.saiyan.file.web.interceptor;

import co.saiyan.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Optional;

/**
 * @author larry
 * @createTime 2023/11/3
 * @description EndApplicationWorker
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShutdownRunner implements CommandLineRunner {

    private final FileService fileService;

    @PreDestroy
    public void destory() {
        log.info("在程序关闭后执行");
        fileService.resetFailedUploads(Optional.empty());
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
