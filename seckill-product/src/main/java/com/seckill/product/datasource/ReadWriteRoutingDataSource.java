package com.seckill.product.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = DataSourceContextHolder.get();
        log.debug("Using datasource route: {}", dataSourceType);
        return dataSourceType;
    }
}
