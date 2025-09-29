package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import com.github.dockerjava.api.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.eclipse.slm.selfdescriptionservice.datasources.base.SubmodelMetaData;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.CpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.OshiCpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.MemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.OshiMemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OsInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OshiOsInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SystemInfoSubmodel extends DefaultSubmodel {

    private final static Logger LOG = LoggerFactory.getLogger(SystemInfoSubmodel.class);

    public static final String ID_SHORT = "SystemInfo";
    public static final String SEMANTIC_ID_VALUE = "http://eclipse.dev/slm/aas/sm/SystemInfo";
    public static final Reference SEMANTIC_ID = new DefaultReference.Builder()
            .type(ReferenceTypes.MODEL_REFERENCE)
            .keys(
                    new DefaultKey.Builder()
                    .type(KeyTypes.SUBMODEL)
                    .value(SEMANTIC_ID_VALUE).build()
            ).build();

    private final CpuInfoProvider cpuInfoProvider;

    private final MemoryInfoProvider memoryInfoProvider;

    private final OsInfoProvider osInfoProvider;

    public SystemInfoSubmodel(String resourceId) {
        super();
        this.id = SystemInfoSubmodel.getId(resourceId);
        this.idShort = ID_SHORT;
        setSemanticId(SEMANTIC_ID);

        this.cpuInfoProvider = new OshiCpuInfoProvider();
        this.memoryInfoProvider = new OshiMemoryInfoProvider();
        this.osInfoProvider = new OshiOsInfoProvider();

        this.addCpuProperties();
        this.addMemoryPorperties();
        this.addOsProperties();
    }

    private static String getId(String resourceId) {
        return ID_SHORT + "-" + resourceId;
    }

    public static SubmodelMetaData getMetaData(String resourceId) {
        return new SubmodelMetaData(
                SystemInfoSubmodel.getId(resourceId),
                SystemInfoSubmodel.ID_SHORT,
                SystemInfoSubmodel.SEMANTIC_ID);
    }

    private void addCpuProperties() {

        var cpuSubmodelElements = new ArrayList<SubmodelElement>();
        var cpuArchProp = new DefaultProperty.Builder()
                .idShort("architecture")
                .valueType(DataTypeDefXsd.STRING)
                .value(cpuInfoProvider.getCpuInfo().getArchitecture())
                .build();
        cpuSubmodelElements.add(cpuArchProp);

        var cpuNameProp = new DefaultProperty.Builder()
                .idShort("name")
                .valueType(DataTypeDefXsd.STRING)
                .value(cpuInfoProvider.getCpuInfo().getName())
                .build();
        cpuSubmodelElements.add(cpuNameProp);

        var cpuVendorProp = new DefaultProperty.Builder()
                .idShort("vendor")
                .valueType(DataTypeDefXsd.STRING)
                .value(cpuInfoProvider.getCpuInfo().getVendor())
                .build();
        cpuSubmodelElements.add(cpuVendorProp);

        var cpuLogicalCoresProp = new DefaultProperty.Builder()
                .idShort("logical_cores")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(cpuInfoProvider.getCpuInfo().getLogicalCores()))
                .build();
        cpuSubmodelElements.add(cpuLogicalCoresProp);

        var cpuPhysicalCoresProp = new DefaultProperty.Builder()
                .idShort("physical_cores")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(cpuInfoProvider.getCpuInfo().getPhysicalCores()))
                .build();
        cpuSubmodelElements.add(cpuPhysicalCoresProp);

        var cpuMaxFreqProp = new DefaultProperty.Builder()
                .idShort("max_frequency")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(cpuInfoProvider.getCpuInfo().getMaxFrequencyHz()))
                .build();
        cpuSubmodelElements.add(cpuMaxFreqProp);

        var cpuSubmodelElementCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("cpu")
                .value(cpuSubmodelElements)
                .build();
        this.submodelElements.add(cpuSubmodelElementCollection);
    }

    private void addMemoryPorperties() {

        var memSubmodelElements = new ArrayList<SubmodelElement>();
        var memFreeProp = new DefaultProperty.Builder()
                .idShort("free_memory")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(memoryInfoProvider.getMemoryInfo().getFreeMemory()))
                .build();
        memSubmodelElements.add(memFreeProp);

        var memUsedProp = new DefaultProperty.Builder()
                .idShort("used_memory")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(memoryInfoProvider.getMemoryInfo().getUsedMemory()))
                .build();
        memSubmodelElements.add(memUsedProp);

        var memTotalProp = new DefaultProperty.Builder()
                .idShort("total_memory")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(memoryInfoProvider.getMemoryInfo().getTotalMemory()))
                .build();
        memSubmodelElements.add(memTotalProp);

        var memSubmodelElementCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("memory")
                .value(memSubmodelElements)
                .build();
        this.submodelElements.add(memSubmodelElementCollection);
    }

    private void addOsProperties() {

        var osSubmodelElements = new ArrayList<SubmodelElement>();
        var osVersionProp = new DefaultProperty.Builder()
                .idShort("version")
                .valueType(DataTypeDefXsd.STRING)
                .value(osInfoProvider.getOsInfo().getVersion())
                .build();
        osSubmodelElements.add(osVersionProp);

        var osBuildNumberProp = new DefaultProperty.Builder()
                .idShort("build_number")
                .valueType(DataTypeDefXsd.STRING)
                .value(osInfoProvider.getOsInfo().getBuildNumber())
                .build();
        osSubmodelElements.add(osBuildNumberProp);

        var osBitnessProp = new DefaultProperty.Builder()
                .idShort("bitness")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(osInfoProvider.getOsInfo().getBitness()))
                .build();
        osSubmodelElements.add(osBitnessProp);

        var osBootTimeProp = new DefaultProperty.Builder()
                .idShort("boottime")
                .valueType(DataTypeDefXsd.STRING)
                .value(String.valueOf(osInfoProvider.getOsInfo().getBootTime()))
                .build();
        osSubmodelElements.add(osBootTimeProp);

        var osFamilyProp = new DefaultProperty.Builder()
                .idShort("family")
                .valueType(DataTypeDefXsd.STRING)
                .value(osInfoProvider.getOsInfo().getFamily())
                .build();
        osSubmodelElements.add(osFamilyProp);

        var osUptimeSecondsProp = new DefaultProperty.Builder()
                .idShort("uptime_seconds")
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(osInfoProvider.getOsInfo().getUptimeSeconds()))
                .build();
        osSubmodelElements.add(osUptimeSecondsProp);

        var osManufacturerProp = new DefaultProperty.Builder()
                .idShort("manufacturer")
                .valueType(DataTypeDefXsd.STRING)
                .value(osInfoProvider.getOsInfo().getManufacturer())
                .build();
        osSubmodelElements.add(osManufacturerProp);

        var osSubmodelElementCollection = new DefaultSubmodelElementCollection.Builder()
                .idShort("os")
                .value(osSubmodelElements)
                .build();
        this.submodelElements.add(osSubmodelElementCollection);
    }

}
