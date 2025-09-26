package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.os;

import org.eclipse.slm.selfdescriptionservice.datasources.systeminfo.OsInfo;

import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

public class OshiOsInfoProvider implements OsInfoProvider {
    @Override
    public OsInfo getOsInfo() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        OperatingSystem.OSVersionInfo versionInfo = os.getVersionInfo();

        OsInfo info = new OsInfo();
        info.setFamily(os.getFamily());
        info.setManufacturer(os.getManufacturer());
        info.setVersion(versionInfo.getVersion());
        info.setBuildNumber(versionInfo.getBuildNumber());
        info.setBitness(os.getBitness());
        info.setUptimeSeconds(os.getSystemUptime());
        info.setBootTime(os.getSystemBootTime());
        return info;
    }
}
