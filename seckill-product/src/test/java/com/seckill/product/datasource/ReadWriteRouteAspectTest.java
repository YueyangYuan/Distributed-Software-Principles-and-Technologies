package com.seckill.product.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ReadWriteRouteAspectTest {

    @Test
    void switchesToSlaveForReadOnlyMethodAndClearsAfterInvocation() {
        DemoService target = new DemoService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new ReadWriteRouteAspect());

        DemoService proxy = factory.getProxy();
        String route = proxy.readFromReplica();

        assertThat(route).isEqualTo("SLAVE");
        assertThat(DataSourceContextHolder.get()).isEqualTo(DataSourceType.MASTER);
    }

    @Test
    void keepsMasterForWriteMethod() {
        DemoService target = new DemoService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new ReadWriteRouteAspect());

        DemoService proxy = factory.getProxy();
        String route = proxy.writeToPrimary();

        assertThat(route).isEqualTo("MASTER");
        assertThat(DataSourceContextHolder.get()).isEqualTo(DataSourceType.MASTER);
    }

    static class DemoService {
        @ReadOnlyRoute
        String readFromReplica() {
            return DataSourceContextHolder.get().name();
        }

        String writeToPrimary() {
            return DataSourceContextHolder.get().name();
        }
    }
}
