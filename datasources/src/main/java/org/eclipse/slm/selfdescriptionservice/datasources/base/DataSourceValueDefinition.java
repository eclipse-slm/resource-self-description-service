package org.eclipse.slm.selfdescriptionservice.datasources.base;

import java.util.Optional;
import java.util.function.Supplier;

public class DataSourceValueDefinition<T> {
    private final String key;
    private final Supplier<T> valueSupplierMethod;
    private final Optional<String> semanticId;

    public DataSourceValueDefinition(String key, Supplier<T> valueSupplierMethod) {
        this.key = key;
        this.valueSupplierMethod = valueSupplierMethod;
        this.semanticId = Optional.empty();
    }

    public DataSourceValueDefinition(String key, Supplier<T> valueSupplierMethod, String semanticId) {
        this.key = key;
        this.valueSupplierMethod = valueSupplierMethod;
        this.semanticId = Optional.of(semanticId);
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return valueSupplierMethod.get();
    }

    public Optional<String> getSemanticId() {
        return semanticId;
    }
}