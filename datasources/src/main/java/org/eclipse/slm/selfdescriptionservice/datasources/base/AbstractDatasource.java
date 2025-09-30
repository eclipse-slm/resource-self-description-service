package org.eclipse.slm.selfdescriptionservice.datasources.base;

import org.eclipse.slm.selfdescriptionservice.datasources.DatasourceRegistry;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

/**
 * Abstract base class for all datasource services.
 * Provides common logic for value lookup and submodel ID creation.
 * Derived classes must provide their own immutable list of supported DataSourceValues.
 */
public abstract class AbstractDatasource implements Datasource {

    protected final DatasourceRegistry datasourceRegistry;

    /** The resource ID associated with this datasource. */
    protected String resourceId = "";

    /** The name of the datasource (used for identification and submodel IDs) */
    protected String datasourceName = "";

    protected final boolean provideSubmodels;

    protected final boolean valueBySemanticId;

    /**
     * Constructor for AbstractDatasourceService.
     * Registers all supported DataSourceValues in the DataSourceValueRegistry on startup.
     * @param resourceId The resource ID to associate with this datasource
     * @param datasourceName The name of the datasource
     * @param provideSubmodels Whether this datasource provides submodels or not
     */
    protected AbstractDatasource(DatasourceRegistry datasourceRegistry, @Value("resource.id") String resourceId, String datasourceName,
                                 boolean provideSubmodels, boolean valueBySemanticId) {
        this.datasourceRegistry = datasourceRegistry;
        this.resourceId = resourceId;
        this.datasourceName = datasourceName;
        this.provideSubmodels = provideSubmodels;
        this.valueBySemanticId = valueBySemanticId;

        this.datasourceRegistry.registerDatasource(this);
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
    public String getValueByKey(String valueKey) {
        return getValueDefinitions().stream()
                .filter(v -> v.getKey().equals(valueKey))
                .findFirst()
                .map(v -> String.valueOf(v.getValue()))
                .orElseThrow(() -> new DataSourceValueNotFoundException(this.datasourceName, valueKey));
    }

    /**
     * Returns the value for the given semantic ID from the immutable list provided by the derived class.
     * Searches the list of supported DataSourceValues for a matching semantic ID and returns its value.
     * @param semanticId Semantic ID of the requested value
     * @return Value as String
     * @throws DataSourceValueNotFoundException if the semantic ID is not found
     */
    public String getValueBySemanticId(String semanticId) {
        return getValueDefinitions().stream()
                .filter(v -> v.getSemanticId().isPresent() && v.getSemanticId().get().equals(semanticId))
                .findFirst()
                .map(v -> String.valueOf(v.getValue()))
                .orElseThrow(() -> new DataSourceValueNotFoundException(this.datasourceName, semanticId));
    }

    /**
     * Indicates whether value lookup by semantic ID is enabled for this datasource.
     * @return true if value lookup by semantic ID is enabled, false otherwise
     */
    public boolean isValueBySemanticIdEnabled() {
        return this.valueBySemanticId;
    }

    /**
     * Returns the immutable list of supported DataSourceValues for this datasource.
     * Must be overridden by derived classes.
     * @return Immutable list of DataSourceValues
     */
    public abstract List<? extends DataSourceValueDefinition<?>> getValueDefinitions();

    /**
     * Creates a unique submodel ID based on datasource name, given ID, and resource ID.
     * @param id The submodel-specific ID
     * @return The generated submodel ID
     */
    protected String createSubmodelId(String id) {
        return this.datasourceName + "-" + id + "-" + this.resourceId;
    }
}
