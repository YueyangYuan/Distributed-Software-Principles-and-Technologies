package com.seckill.product.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ReadWriteRouteAspect {

    @Around("@annotation(com.seckill.product.datasource.ReadOnlyRoute)")
    public Object switchToSlave(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSourceContextHolder.use(DataSourceType.SLAVE);
        try {
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }
}
