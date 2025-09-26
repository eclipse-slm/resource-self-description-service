package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.memory;

import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.MemoryInfo;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

public class OshiMemoryInfoProvider implements MemoryInfoProvider {
    @Override
    public MemoryInfo getMemoryInfo() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalMemory = memory.getTotal();
        long freeMemory = memory.getAvailable();
        long usedMemory = totalMemory - freeMemory;
        return new MemoryInfo(totalMemory, freeMemory, usedMemory);
    }
}
