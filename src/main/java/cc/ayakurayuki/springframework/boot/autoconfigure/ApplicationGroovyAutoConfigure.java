package cc.ayakurayuki.springframework.boot.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ayakura Yuki
 */
@Configuration
@ConditionalOnClass(ApplicationGroovyAutoConfigure.class)
@EnableConfigurationProperties
@Slf4j
public class ApplicationGroovyAutoConfigure {
}
