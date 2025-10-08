package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.slm.selfdescriptionservice.datasources.DatasourceRegistry;
import org.eclipse.slm.selfdescriptionservice.datasources.base.AbstractDatasource;
import org.eclipse.slm.selfdescriptionservice.datasources.base.SubmodelMetaData;
import org.eclipse.slm.selfdescriptionservice.datasources.base.DataSourceValueDefinition;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.CpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.MemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OsInfoProvider;
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
    protected SystemInfoDataSource(DatasourceRegistry datasourceRegistry,
                                   @Value("${resource.id}") String resourceId,
                                   @Value("${datasources.systeminfo.provide-submodels:false}") boolean provideSubmodels,
                                   @Value("${datasources.systeminfo.value-by-semantic-id:false}") boolean valueBySemanticId,
                                   CpuInfoProvider cpuInfoProvider,
                                   MemoryInfoProvider memoryInfoProvider,
                                   OsInfoProvider osInfoProvider
    ) {
        super(datasourceRegistry, resourceId, SystemInfoDataSource.DATASOURCE_NAME, provideSubmodels, valueBySemanticId);

        this.cpuInfoProvider = cpuInfoProvider;
        this.memoryInfoProvider = memoryInfoProvider;
        this.osInfoProvider = osInfoProvider;
    }

    //region AbstractDatasourceService
    @Override
    public List<? extends DataSourceValueDefinition<?>> getValueDefinitions() {
        var cpuInfo = cpuInfoProvider.getCpuInfo();
        var memoryInfo = memoryInfoProvider.getMemoryInfo();
        var osInfo = osInfoProvider.getOsInfo();

        return List.of(
                new DataSourceValueDefinition<>("cpu.architecture", cpuInfo::getArchitecture,
                        "http://eclipse.dev/slm/aas/sme/SysteInfo/CPU/Arch"),
                new DataSourceValueDefinition<>("cpu.name", cpuInfo::getName),
                new DataSourceValueDefinition<>("cpu.vendor", cpuInfo::getVendor),
                new DataSourceValueDefinition<>("cpu.logical_cores", cpuInfo::getLogicalCores),
                new DataSourceValueDefinition<>("cpu.physical_cores", cpuInfo::getPhysicalCores),
                new DataSourceValueDefinition<>("cpu.max_frequency", cpuInfo::getMaxFrequencyHz),
                new DataSourceValueDefinition<>("mem.free_memory", memoryInfo::getFreeMemory),
                new DataSourceValueDefinition<>("mem.used_memory", memoryInfo::getUsedMemory),
                new DataSourceValueDefinition<>("mem.total_memory", memoryInfo::getTotalMemory),
                new DataSourceValueDefinition<>("os.version", osInfo::getVersion,
                        "http://eclipse.dev/slm/aas/sme/SysteInfo/OS/Version"),
                new DataSourceValueDefinition<>("os.build_number", osInfo::getBuildNumber),
                new DataSourceValueDefinition<>("os.bitness", osInfo::getBitness),
                new DataSourceValueDefinition<>("os.boottime", osInfo::getBootTime),
                new DataSourceValueDefinition<>("os.family", osInfo::getFamily),
                new DataSourceValueDefinition<>("os.uptime_seconds", osInfo::getUptimeSeconds),
                new DataSourceValueDefinition<>("os.manufacturer", osInfo::getManufacturer)
        );
    }

    @Override
    public List<Submodel> getSubmodels() {
        if (!provideSubmodels) {
            return List.of();
        }

        var systemInfoSubmodel = new SystemInfoSubmodel(this.resourceId, this.cpuInfoProvider, this.memoryInfoProvider, this.osInfoProvider);
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

        var dockerSubmodel = new SystemInfoSubmodel(this.resourceId, this.cpuInfoProvider, this.memoryInfoProvider, this.osInfoProvider);
        return Optional.of(dockerSubmodel);
    }
    //endregion AbstractDatasourceService
}
