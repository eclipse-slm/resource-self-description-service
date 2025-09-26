package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.selfdescriptionservice.datasources.AbstractDatasourceService;
import org.eclipse.slm.selfdescriptionservice.datasources.aas.SubmodelMetaData;
import org.eclipse.slm.selfdescriptionservice.datasources.docker.DataSourceValue;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.CpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.OshiCpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.MemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.OshiMemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OsInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OshiOsInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.template.datasourcevalues.DataSourceValueRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "datasources.system.enabled", havingValue = "true", matchIfMissing = false)
public class SystemInfoDataSourceService extends AbstractDatasourceService {

    private final static Logger LOG = LoggerFactory.getLogger(SystemInfoDataSourceService.class);

    public static final String DATASOURCE_NAME = "SystemInfo";

    private final CpuInfoProvider cpuInfoProvider;

    private final MemoryInfoProvider memoryInfoProvider;

    private final OsInfoProvider osInfoProvider;

    /**
     * Constructor for AbstractDatasourceService.
     * Registers all supported DataSourceValues in the DataSourceValueRegistry on startup.
     *
     * @param resourceId              The resource ID to associate with this datasource
     * @param dataSourceValueRegistry The registry for DataSourceValues
     */
    protected SystemInfoDataSourceService(@Value("${resource.id}") String resourceId, DataSourceValueRegistry dataSourceValueRegistry) {
        super(resourceId, SystemInfoDataSourceService.DATASOURCE_NAME, dataSourceValueRegistry);

        this.cpuInfoProvider = new OshiCpuInfoProvider();
        this.memoryInfoProvider = new OshiMemoryInfoProvider();
        this.osInfoProvider = new OshiOsInfoProvider();
    }

    @Override
    protected List<? extends DataSourceValue<?>> getDataSourceValues() {
        return List.of(
                new DataSourceValue<>("cpu.architecture", () -> cpuInfoProvider.getCpuInfo().getArchitecture()),
                new DataSourceValue<>("cpu.name", () -> cpuInfoProvider.getCpuInfo().getName()),
                new DataSourceValue<>("cpu.vendor", () -> cpuInfoProvider.getCpuInfo().getVendor()),
                new DataSourceValue<>("cpu.logical_cores", () -> cpuInfoProvider.getCpuInfo().getLogicalCores()),
                new DataSourceValue<>("cpu.physical_cores", () -> cpuInfoProvider.getCpuInfo().getPhysicalCores()),
                new DataSourceValue<>("cpu.max_frequency", () -> cpuInfoProvider.getCpuInfo().getMaxFrequencyHz()),
                new DataSourceValue<>("mem.free_memory", () -> memoryInfoProvider.getMemoryInfo().getFreeMemory()),
                new DataSourceValue<>("mem.used_memory", () -> memoryInfoProvider.getMemoryInfo().getUsedMemory()),
                new DataSourceValue<>("mem.total_memory", () -> memoryInfoProvider.getMemoryInfo().getTotalMemory()),
                new DataSourceValue<>("os.version", () -> osInfoProvider.getOsInfo().getVersion()),
                new DataSourceValue<>("os.build_number", () -> osInfoProvider.getOsInfo().getBuildNumber()),
                new DataSourceValue<>("os.bitness", () -> osInfoProvider.getOsInfo().getBitness()),
                new DataSourceValue<>("os.boottime", () -> osInfoProvider.getOsInfo().getBootTime()),
                new DataSourceValue<>("os.family", () -> osInfoProvider.getOsInfo().getFamily()),
                new DataSourceValue<>("os.uptime_seconds", () -> osInfoProvider.getOsInfo().getUptimeSeconds()),
                new DataSourceValue<>("os.manufacturer", () -> osInfoProvider.getOsInfo().getManufacturer())
        );
    }

    @Override
    public List<Submodel> getSubmodels() {
        return List.of();
    }

    @Override
    public List<SubmodelMetaData> getMetaDataOfSubmodels() {
        return List.of();
    }

    @Override
    public Optional<Submodel> getSubmodelById(String id) throws IOException {
        return Optional.empty();
    }
}
