package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.selfdescriptionservice.datasources.base.AbstractDatasource;
import org.eclipse.slm.selfdescriptionservice.datasources.base.SubmodelMetaData;
import org.eclipse.slm.selfdescriptionservice.datasources.base.DataSourceValueDefinition;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.CpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.OshiCpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.MemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.OshiMemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OsInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OshiOsInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "datasources.systeminfo.enabled", havingValue = "true", matchIfMissing = false)
public class SystemInfoDataSource extends AbstractDatasource {

    private final static Logger LOG = LoggerFactory.getLogger(SystemInfoDataSource.class);

    public static final String DATASOURCE_NAME = "SystemInfo";

    private final CpuInfoProvider cpuInfoProvider;

    private final MemoryInfoProvider memoryInfoProvider;

    private final OsInfoProvider osInfoProvider;

    /**
     * Constructor for AbstractDatasourceService.
     * Registers all supported DataSourceValues in the DataSourceValueRegistry on startup.
     *
     * @param resourceId              The resource ID to associate with this datasource
     */
    protected SystemInfoDataSource(@Value("${resource.id}") String resourceId,
                                   @Value("${datasources.systeminfo.provide-submodels}") boolean provideSubmodels
    ) {
        super(resourceId, SystemInfoDataSource.DATASOURCE_NAME, provideSubmodels);

        this.cpuInfoProvider = new OshiCpuInfoProvider();
        this.memoryInfoProvider = new OshiMemoryInfoProvider();
        this.osInfoProvider = new OshiOsInfoProvider();
    }

    //region AbstractDatasourceService
    @Override
    public List<? extends DataSourceValueDefinition<?>> getValueDefinitions() {
        return List.of(
                new DataSourceValueDefinition<>("cpu.architecture", () -> cpuInfoProvider.getCpuInfo().getArchitecture()),
                new DataSourceValueDefinition<>("cpu.name", () -> cpuInfoProvider.getCpuInfo().getName()),
                new DataSourceValueDefinition<>("cpu.vendor", () -> cpuInfoProvider.getCpuInfo().getVendor()),
                new DataSourceValueDefinition<>("cpu.logical_cores", () -> cpuInfoProvider.getCpuInfo().getLogicalCores()),
                new DataSourceValueDefinition<>("cpu.physical_cores", () -> cpuInfoProvider.getCpuInfo().getPhysicalCores()),
                new DataSourceValueDefinition<>("cpu.max_frequency", () -> cpuInfoProvider.getCpuInfo().getMaxFrequencyHz()),
                new DataSourceValueDefinition<>("mem.free_memory", () -> memoryInfoProvider.getMemoryInfo().getFreeMemory()),
                new DataSourceValueDefinition<>("mem.used_memory", () -> memoryInfoProvider.getMemoryInfo().getUsedMemory()),
                new DataSourceValueDefinition<>("mem.total_memory", () -> memoryInfoProvider.getMemoryInfo().getTotalMemory()),
                new DataSourceValueDefinition<>("os.version", () -> osInfoProvider.getOsInfo().getVersion()),
                new DataSourceValueDefinition<>("os.build_number", () -> osInfoProvider.getOsInfo().getBuildNumber()),
                new DataSourceValueDefinition<>("os.bitness", () -> osInfoProvider.getOsInfo().getBitness()),
                new DataSourceValueDefinition<>("os.boottime", () -> osInfoProvider.getOsInfo().getBootTime()),
                new DataSourceValueDefinition<>("os.family", () -> osInfoProvider.getOsInfo().getFamily()),
                new DataSourceValueDefinition<>("os.uptime_seconds", () -> osInfoProvider.getOsInfo().getUptimeSeconds()),
                new DataSourceValueDefinition<>("os.manufacturer", () -> osInfoProvider.getOsInfo().getManufacturer())
        );
    }

    @Override
    public List<Submodel> getSubmodels() {
        if (!provideSubmodels) {
            return List.of();
        }

        var systemInfoSubmodel = new SystemInfoSubmodel(this.resourceId);
        return List.of(systemInfoSubmodel);
    }

    @Override
    public List<SubmodelMetaData> getMetaDataOfSubmodels() {
        if (!provideSubmodels) {
            return List.of();
        }

        var submodelMetaData = SystemInfoSubmodel.getMetaData(this.resourceId);
        return List.of(submodelMetaData);
    }

    @Override
    public Optional<Submodel> getSubmodelById(String id) {
        if (!provideSubmodels) {
            return Optional.empty();
        }

        var dockerSubmodel = new SystemInfoSubmodel(this.resourceId);
        return Optional.of(dockerSubmodel);
    }
    //endregion AbstractDatasourceService
}
