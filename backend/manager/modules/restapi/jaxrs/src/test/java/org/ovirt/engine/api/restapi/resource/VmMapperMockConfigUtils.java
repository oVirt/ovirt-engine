package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.DataType;
import org.ovirt.engine.core.utils.ConfigUtilsBase;

import java.util.Arrays;
import java.util.List;

public class VmMapperMockConfigUtils extends ConfigUtilsBase {
    List<ConfigValues> booleanValues = Arrays.asList(
            ConfigValues.SerialNumberPolicySupported,
            ConfigValues.SpiceFileTransferToggleSupported,
            ConfigValues.SpiceCopyPasteToggleSupported,
            ConfigValues.AutoConvergenceSupported,
            ConfigValues.MigrationCompressionSupported
    );

    @Override
    protected void setValue(String name, String value, String version) {

    }

    @Override
    protected Object getValue(DataType type, String name, String defaultValue) {
        return Boolean.TRUE;
    }

    @Override
    public <T> T getValue(ConfigValues configValue, String version) {
        if (booleanValues.contains(configValue)) {
            return (T) Boolean.TRUE;
        }
        return null;
    }
}
