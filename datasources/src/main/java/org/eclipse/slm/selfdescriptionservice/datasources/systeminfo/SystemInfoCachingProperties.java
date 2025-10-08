package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "datasources.systeminfo.caching")
public class SystemInfoCachingProperties {
    private CacheConfig overall = new CacheConfig();
    private CacheConfig cpu = new CacheConfig();;
    private CacheConfig memory = new CacheConfig();;
    private CacheConfig os = new CacheConfig();;

    public static class CacheConfig {
        private boolean enabled = true;
        private int refreshIntervalS = 300;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getRefreshIntervalS() { return refreshIntervalS; }
        public void setRefreshIntervalS(int refreshIntervalS) { this.refreshIntervalS = refreshIntervalS; }
    }

    public CacheConfig getOverall() { return overall; }
    public void setOverall(CacheConfig overall) { this.overall = overall; }

    public CacheConfig getCpu() { return cpu; }
    public void setCpu(CacheConfig cpu) { this.cpu = cpu; }

    public CacheConfig getMemory() { return memory; }
    public void setMemory(CacheConfig memory) { this.memory = memory; }

    public CacheConfig getOs() { return os; }
    public void setOs(CacheConfig os) { this.os = os; }
}