package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.cpu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.CpuInfo;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

public class OshiCpuInfoProvider implements CpuInfoProvider {

    @Override
    public CpuInfo getCpuInfo() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();

        var objectMapper = new ObjectMapper();
        try {
            var halString = objectMapper.writeValueAsString(hal);
            int i = 0;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        CpuInfo info = new CpuInfo();
        info.setArchitecture(System.getProperty("os.arch"));
        info.setVendor(cpu.getProcessorIdentifier().getVendor());
        info.setName(cpu.getProcessorIdentifier().getName());
        info.setPhysicalCores(cpu.getPhysicalProcessorCount());
        info.setLogicalCores(cpu.getLogicalProcessorCount());
        info.setMaxFrequencyHz(cpu.getMaxFreq());
        return info;
    }

}
