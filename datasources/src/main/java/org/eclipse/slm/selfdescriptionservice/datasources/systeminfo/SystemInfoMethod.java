package org.eclipse.slm.selfdescriptionservice.datasources.systeminfo;

import freemarker.template.TemplateModelException;
import org.eclipse.slm.selfdescriptionservice.datasources.template.methods.AbstractSafeTemplateMethodModelEx;
import org.eclipse.slm.selfdescriptionservice.datasources.template.methods.JsonPathReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemInfoMethod extends AbstractSafeTemplateMethodModelEx {
    private final static Logger LOG = LoggerFactory.getLogger(SystemInfoMethod.class);

    private final OshiSystemInfoProvider oshiSystemInfoProvider;

    public SystemInfoMethod(OshiSystemInfoProvider oshiSystemInfoProvider) {
        this.oshiSystemInfoProvider = oshiSystemInfoProvider;
    }

    @Override
    public Object safeExec(List list) throws Exception {
        if (list.size() != 1) {
            throw new TemplateModelException("Wrong number of arguments");
        }
        var jsonPath = list.get(0).toString();
        if (this.oshiSystemInfoProvider.getSystemInfoJson() == null) {
            return "SystemInfo datasource still initializing, please try again later";
        }
        var value = JsonPathReader.readSingleValueFromPath(this.oshiSystemInfoProvider.getSystemInfoJson(), jsonPath);
        return value;
    }
}
