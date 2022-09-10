package com.yxd.xiaomi2meidi.aop;

import com.yxd.xiaomi2meidi.cache.Gcache;
import com.yxd.xiaomi2meidi.controller.AcController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Administrator
 */
@Aspect
@Component
@Slf4j
public class TokenAspect {

//    @Autowired
//    AcController acController;

    @Pointcut("@annotation(com.yxd.xiaomi2meidi.anotation.TokenCheck)")
    public void pointCut() {
    }


    @Before("pointCut()")
    public void around(JoinPoint joinPoint) {
        if (!StringUtils.hasText(Gcache.config.getAccessToken())) {
//            acController.login();
            log.info("11");
        }
    }
}
