package org.eclipse.slm.selfdescriptionservice.datasources.base;

import java.util.function.Supplier;

public class DataSourceValueDefinition<T> {
    private final String key;
    private final Supplier<T> valueSupplierMethod;

    public DataSourceValueDefinition(String key, Supplier<T> valueSupplierMethod) {
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