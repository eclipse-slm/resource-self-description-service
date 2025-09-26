package org.eclipse.slm.selfdescriptionservice.datasources.docker;

import java.util.function.Supplier;

public class DataSourceValue<T> {
    private final String key;
    private final Supplier<T> valueSupplierMethod;

    public DataSourceValue(String key, Supplier<T> valueSupplierMethod) {
        this.key = key;
        this.valueSupplierMethod = valueSupplierMethod;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return valueSupplierMethod.get();
    }
}