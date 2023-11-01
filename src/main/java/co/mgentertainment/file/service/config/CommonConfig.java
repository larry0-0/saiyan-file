package co.mgentertainment.file.service.config;

import co.mgentertainment.common.devent.annonation.EnableDistributedEvent;
import co.mgentertainment.common.doc.annonation.EnableCommonDoc;
import co.mgentertainment.common.eventbus.annonation.EnableCommonEventBus;
import co.mgentertainment.common.syslog.annotation.EnableCommonSyslog;
import org.springframework.context.annotation.Configuration;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CommonConfig
 */
@Configuration
@EnableCommonEventBus
@EnableCommonDoc
@EnableCommonSyslog
@EnableDistributedEvent
public class CommonConfig {
}