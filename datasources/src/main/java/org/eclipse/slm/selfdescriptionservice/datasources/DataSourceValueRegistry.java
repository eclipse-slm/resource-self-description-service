package org.eclipse.slm.selfdescriptionservice.datasources;

import org.eclipse.slm.selfdescriptionservice.datasources.docker.DataSourceValue;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all {@link DataSourceValue}s provided by datasource services.
 * Allows lookup and retrieval of values by key.
 *
 * See also:
 * {@link org.eclipse.slm.selfdescriptionservice.datasources.AbstractDatasourceService}
 * {@link DataSourceValueNotFoundException}
 */
@Component
public class DataSourceValueRegistry {

    /**
     * Stores all registered {@link DataSourceValue} objects by their key.
     */
    private final Map<String, DataSourceValue<?>> values = new HashMap<>();

    /**
     * Registers a {@link DataSourceValue} in the registry.
     * @param value The {@link DataSourceValue} to register
     */
    public void register(DataSourceValue<?> value) {
        values.put(value.getKey(), value);
    }

    /**
     * Returns the value for the given key, if registered.
     * @param key The key of the requested value
     * @return Value as String
     * @throws DataSourceValueNotFoundException if the key is not found
     * @see DataSourceValueNotFoundException
     * @see DataSourceValue#getValue()
     */
    public String getValue(String key) {
        DataSourceValue<?> value = values.get(key);
        if (value == null) {
            throw new DataSourceValueNotFoundException("DataSourceValueRegistry", key);
        }
        return String.valueOf(value.getValue());
    }

    /**
     * Returns an immutable copy of all registered DataSourceValues mapped by their key.
     * @return Map of key to DataSourceValue
     */
    public Map<String, DataSourceValue<?>> getAll() {
        return Map.copyOf(values);
    }
}
