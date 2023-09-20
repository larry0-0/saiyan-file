package co.mgentertainment.file.service.config;

import co.mgentertainment.common.doc.annonation.EnableCommonDoc;
import co.mgentertainment.common.eventbus.annonation.EnableCommonEventBus;
import org.springframework.context.annotation.Configuration;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CommonConfig
 */
@Configuration
@EnableCommonEventBus
@EnableCommonDoc
public class CommonConfig {
}