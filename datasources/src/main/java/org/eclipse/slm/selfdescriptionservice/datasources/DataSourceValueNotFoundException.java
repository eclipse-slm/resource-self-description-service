package org.eclipse.slm.selfdescriptionservice.datasources;

public class DataSourceValueNotFoundException extends RuntimeException {

    public DataSourceValueNotFoundException(String dataSourceName, String valueKey) {
        super("Value with key '" + valueKey + "' not found in data source '" + dataSourceName + "'");
    }

}
