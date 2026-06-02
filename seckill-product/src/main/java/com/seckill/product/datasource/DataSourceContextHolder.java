package com.seckill.product.datasource;

public final class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    private DataSourceContextHolder() {
    }

    public static void use(DataSourceType dataSourceType) {
        CONTEXT.set(dataSourceType);
    }

    public static DataSourceType get() {
        DataSourceType dataSourceType = CONTEXT.get();
        return dataSourceType == null ? DataSourceType.MASTER : dataSourceType;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
