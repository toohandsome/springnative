package com.yxd.xiaomi2meidi;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;

import com.yxd.xiaomi2meidi.controller.AcController;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;

import org.springframework.context.annotation.ImportAware;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.nativex.hint.*;

import java.io.Serializable;

import static org.springframework.nativex.hint.TypeAccess.PUBLIC_METHODS;

/**
 * @author Administrator
 */


//@AotProxyHints({
//        @AotProxyHint(targetClass = AcController.class, proxyFeatures = ProxyBits.IS_STATIC)
//})
@AotProxyHint(targetClass=AcController.class,proxyFeatures = ProxyBits.IS_STATIC)
@JdkProxyHint(types = {
        org.springframework.beans.factory.SmartInitializingSingleton.class,
        org.springframework.context.ApplicationContextAware.class,
        org.springframework.beans.factory.BeanNameAware.class,
        org.springframework.beans.factory.config.BeanPostProcessor.class,
        org.springframework.context.ApplicationListener.class,
        org.springframework.beans.factory.BeanFactoryAware.class,
        org.springframework.beans.factory.InitializingBean.class
})

@SpringBootApplication
public class DemoApplication {


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
