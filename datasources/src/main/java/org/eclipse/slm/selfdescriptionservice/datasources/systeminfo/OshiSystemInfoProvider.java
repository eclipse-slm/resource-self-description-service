package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu.CpuInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory.MemoryInfoProvider;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os.OsInfoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

@Component
public class OshiSystemInfoProvider implements CpuInfoProvider, MemoryInfoProvider, OsInfoProvider {
    private final static Logger LOG = LoggerFactory.getLogger(OshiSystemInfoProvider.class);

    private DocumentContext cachedSystemInfoJson = null;
    private volatile boolean isUpdating = false;

    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardwareAbstractionLayer;
    private final OperatingSystem operatingSysteo;

    private final SystemInfoCachingProperties systemInfoCachingProperties;
    private CpuInfo cachedCpuInfo;
    private long cpuInfoLastCacheUpdate = 0;
    private MemoryInfo cachedMemoryInfo;
    private long memoryInfoLastCacheUpdate = 0;
    private OsInfo cachedOsInfo;
    private long osInfoLastCacheUpdate = 0;

    public OshiSystemInfoProvider(SystemInfoCachingProperties systemInfoCachingProperties) {
        this.systemInfoCachingProperties = systemInfoCachingProperties;
        this.systemInfo = new SystemInfo();
        this.hardwareAbstractionLayer = this.systemInfo.getHardware();
        this.operatingSysteo = this.systemInfo.getOperatingSystem();

        if (this.systemInfoCachingProperties.getOverall().isEnabled()) {
            var refreshIntervalSeconds = this.systemInfoCachingProperties.getOverall().getRefreshIntervalS();
            // Start thread to update cache periodically
            Thread cacheUpdaterThread = new Thread(() -> {
                while (true) {
                    try {
                        updateCacheAsync();
                        Thread.sleep(refreshIntervalSeconds * 1000L);
                    } catch (InterruptedException e) {
                        LOG.warn("SystemInfoCacheUpdater interrupted");
                        break;
                    }
                }
            }, "SystemInfoCacheIntervalUpdater");
            cacheUpdaterThread.setDaemon(true);
            cacheUpdaterThread.start();
        }
    }

    private void updateCacheAsync() {
        if (isUpdating) return;
        isUpdating = true;
        new Thread(() -> {
            try {
                var systemInfo = new SystemInfo();
                var objectMapper = new ObjectMapper();
                var systemInfoJsonstring = objectMapper.writeValueAsString(systemInfo);
                cachedSystemInfoJson = JsonPath.parse(systemInfoJsonstring);
                LOG.info("SystemInfo cache updated");
            } catch (Exception e) {
                // Log error but keep old cache
                LOG.error("Error updating SystemInfo cache: {}", e.getMessage());
                LOG.debug("Stacktrace:", e);
            } finally {
                isUpdating = false;
            }
        }, "SystemInfoCacheUpdater").start();
    }

    public DocumentContext getSystemInfoJson() throws JsonProcessingException {
        if (this.systemInfoCachingProperties.getOverall().isEnabled()) {
            return cachedSystemInfoJson;
        }
        else {
            var systemInfo = new SystemInfo();
            var objectMapper = new ObjectMapper();
            var systemInfoJsonstring = objectMapper.writeValueAsString(systemInfo);
            var systemInfoJson = JsonPath.parse(systemInfoJsonstring);
            return systemInfoJson;
        }

    }

    //region CpuInfoProvider
    @Override
    public CpuInfo getCpuInfo() {
        // Check if caching is enabled and if cache is still valid
        if (systemInfoCachingProperties.getCpu().isEnabled()
            && System.currentTimeMillis() - cpuInfoLastCacheUpdate < systemInfoCachingProperties.getCpu().getRefreshIntervalS() * 1000L) {
            return cachedCpuInfo;
        }
        // Get latest CPU info
        CentralProcessor cpu = this.hardwareAbstractionLayer.getProcessor();
        var cpuInfo = new CpuInfo();
        cpuInfo.setArchitecture(System.getProperty("os.arch"));
        cpuInfo.setVendor(cpu.getProcessorIdentifier().getVendor());
        cpuInfo.setName(cpu.getProcessorIdentifier().getName());
        cpuInfo.setPhysicalCores(cpu.getPhysicalProcessorCount());
        cpuInfo.setLogicalCores(cpu.getLogicalProcessorCount());
        cpuInfo.setMaxFrequencyHz(cpu.getMaxFreq());
        // Cache latest CPU info
        this.cachedCpuInfo = cpuInfo;
        this.cpuInfoLastCacheUpdate = System.currentTimeMillis();

        return cpuInfo;
    }
    //endregion CpuInfoProvider

    //region MemoryInfoProvider
    @Override
    public MemoryInfo getMemoryInfo() {
        // Check if caching is enabled and if cache is still valid
        if (systemInfoCachingProperties.getMemory().isEnabled()
                && System.currentTimeMillis() - memoryInfoLastCacheUpdate < systemInfoCachingProperties.getMemory().getRefreshIntervalS() * 1000L) {
            return cachedMemoryInfo;
        }
        // Get latest memory info
        var memory = this.hardwareAbstractionLayer.getMemory();
        long totalMemory = memory.getTotal();
        long freeMemory = memory.getAvailable();
        long usedMemory = totalMemory - freeMemory;
        var memoryInfo = new MemoryInfo(totalMemory, freeMemory, usedMemory);
        // Cache latest memory info
        this.cachedMemoryInfo = memoryInfo;
        this.memoryInfoLastCacheUpdate = System.currentTimeMillis();

        return memoryInfo;
    }
    //endregion MemoryInfoProvider

    //region OsInfoProvider
    @Override
    public OsInfo getOsInfo() {
        // Check if caching is enabled and if cache is still valid
        if (systemInfoCachingProperties.getOs().isEnabled()
                && System.currentTimeMillis() - osInfoLastCacheUpdate < systemInfoCachingProperties.getOs().getRefreshIntervalS() * 1000L) {
            return cachedOsInfo;
        }
        // Get latest OS info
        var osInfo = new OsInfo();
        osInfo.setFamily(this.operatingSysteo.getFamily());
        osInfo.setManufacturer(this.operatingSysteo.getManufacturer());
        osInfo.setBitness(this.operatingSysteo.getBitness());
        osInfo.setUptimeSeconds(this.operatingSysteo.getSystemUptime());
        osInfo.setBootTime(this.operatingSysteo.getSystemBootTime());
        var osVersionInfo = this.operatingSysteo.getVersionInfo();
        osInfo.setVersion(osVersionInfo.getVersion());
        osInfo.setBuildNumber(osVersionInfo.getBuildNumber());

        // Cache latest OS info
        this.cachedOsInfo = osInfo;
        this.osInfoLastCacheUpdate = System.currentTimeMillis();

        return osInfo;
    }
    //endregion OsInfoProvider
}
