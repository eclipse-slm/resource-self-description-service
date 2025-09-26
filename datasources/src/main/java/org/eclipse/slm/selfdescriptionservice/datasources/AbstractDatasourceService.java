package org.eclipse.slm.selfdescriptionservice.datasources;

import org.eclipse.slm.selfdescriptionservice.datasources.docker.DataSourceValue;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

/**
 * Abstract base class for all datasource services.
 * Provides common logic for value lookup and submodel ID creation.
 * Derived classes must provide their own immutable list of supported DataSourceValues.
 */
public abstract class AbstractDatasourceService implements Datasource {

    /** The resource ID associated with this datasource. */
    protected String resourceId = "";

    /** The name of the datasource (used for identification and submodel IDs) */
    protected String datasourceName = "";

    private final DataSourceValueRegistry dataSourceValueRegistry;

    /**
     * Constructor for AbstractDatasourceService.
     * Registers all supported DataSourceValues in the DataSourceValueRegistry on startup.
     * @param resourceId The resource ID to associate with this datasource
     * @param datasourceName The name of the datasource
     * @param dataSourceValueRegistry The registry for DataSourceValues
     */
    protected AbstractDatasourceService(@Value("resource.id") String resourceId, String datasourceName, DataSourceValueRegistry dataSourceValueRegistry) {
        this.resourceId = resourceId;
        this.datasourceName = datasourceName;
        this.dataSourceValueRegistry = dataSourceValueRegistry;
        // Register all supported DataSourceValues at startup
        for (DataSourceValue<?> value : getDataSourceValues()) {
            dataSourceValueRegistry.register(value);
        }
    }

    /**
     * Returns the name of the datasource.
     * @return The datasource name
     */
    public String getDatasourceName() {
        return this.datasourceName;
    }

    /**
     * Returns the value for the given key from the immutable list provided by the derived class.
     * Searches the list of supported DataSourceValues for a matching key and returns its value.
     * @param valueKey Key of the requested value
     * @return Value as String
     * @throws DataSourceValueNotFoundException if the key is not found
     */
    public String getValue(String valueKey) {
        return getDataSourceValues().stream()
                .filter(v -> v.getKey().equals(valueKey))
                .findFirst()
                .map(v -> String.valueOf(v.getValue()))
                .orElseThrow(() -> new DataSourceValueNotFoundException(this.datasourceName, valueKey));
    }

    /**
     * Returns the immutable list of supported DataSourceValues for this datasource.
     * Must be overridden by derived classes.
     * @return Immutable list of DataSourceValues
     */
    protected abstract List<? extends DataSourceValue<?>> getDataSourceValues();

    /**
     * Creates a unique submodel ID based on datasource name, given ID, and resource ID.
     * @param id The submodel-specific ID
     * @return The generated submodel ID
     */
    protected String createSubmodelId(String id) {
        return this.datasourceName + "-" + id + "-" + this.resourceId;
    }
}
