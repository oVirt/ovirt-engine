package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class GetSystemOptionParameters extends QueryParametersBase {
    private static final long serialVersionUID = 985283417143273355L;

    private ConfigValues optionName;
    private String optionVersion;

    public GetSystemOptionParameters() {
    }

    public GetSystemOptionParameters(ConfigValues optionName) {
        this.optionName = optionName;
    }

    public ConfigValues getOptionName() {
        return optionName;
    }

    public void setOptionName(ConfigValues optionName) {
        this.optionName = optionName;
    }

    public String getOptionVersion() {
        return optionVersion;
    }

    public void setOptionVersion(String optionVersion) {
        this.optionVersion = optionVersion;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
            .append("optionName", getOptionName())
            .append("optionVersion", getOptionVersion());
    }
}
