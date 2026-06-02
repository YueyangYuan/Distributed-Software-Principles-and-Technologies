package com.seckill.product.config;

import com.seckill.product.datasource.DataSourceType;
import com.seckill.product.datasource.ReadWriteRoutingDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class ProductDataSourceConfig {

    @Bean
    @ConfigurationProperties("app.datasource.master")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.slave")
    public DataSourceProperties slaveDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource masterDataSource() {
        return masterDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource slaveDataSource() {
        return slaveDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public DataSource routingDataSource(DataSource masterDataSource, DataSource slaveDataSource) {
        ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.setTargetDataSources(Map.of(
                DataSourceType.MASTER, masterDataSource,
                DataSourceType.SLAVE, slaveDataSource
        ));
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }
}
