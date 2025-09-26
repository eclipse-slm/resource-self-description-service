package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

@Component
public class SystemInfoProvider {
    private final static Logger LOG = LoggerFactory.getLogger(SystemInfoProvider.class);

    private DocumentContext cachedSystemInfoJson = null;
    private volatile boolean isUpdating = false;
    private final long refreshIntervalSeconds;

    public SystemInfoProvider(@Value("${datasources.system.refresh-interval-s:300}") long refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
        // Start thread to update cache periodically
        Thread cacheUpdaterThread = new Thread(() -> {
            while (true) {
                try {
                    updateCacheAsync();
                    Thread.sleep(this.refreshIntervalSeconds*1000);
                } catch (InterruptedException e) {
                    LOG.warn("SystemInfoCacheUpdater interrupted");
                    break;
                }
            }
        }, "SystemInfoCacheIntervalUpdater");
        cacheUpdaterThread.setDaemon(true);
        cacheUpdaterThread.start();
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

    public DocumentContext getCachedSystemInfoJson() {
        return cachedSystemInfoJson;
    }
}
