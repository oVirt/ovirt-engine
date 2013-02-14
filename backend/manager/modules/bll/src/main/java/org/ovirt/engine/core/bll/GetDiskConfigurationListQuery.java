package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.utils.EnumUtils;

public class GetDiskConfigurationListQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    /**
     * The disk configuration is represented by a comma delimited string as follows:
     * <code>label,VolumeType(from the enum),VolumeFormat(from the enum),wipeAfterDelete(true/false)<code>.
     * E.g.: <code>System,Sparse,COW,true</code>
     */
    private static final int DISK_CONFIGURATION_ELEMENTS_NUM = 4;

    public GetDiskConfigurationListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Disk Config is a list of enum values of:
        // diskType,volType,volFormat,wipeAfterDelete;...
        List<DiskImageBase> result = new ArrayList<DiskImageBase>();
        String[] imageBasesList = Config.<String> GetValue(ConfigValues.DiskConfigurationList).split("[;]", -1);
        for (String imageBase : imageBasesList) {
            String[] configs = imageBase.split("[,]", -1);
            if (configs.length == DISK_CONFIGURATION_ELEMENTS_NUM) {
                try {
                    DiskImageBase tempVar = new DiskImageBase();
                    tempVar.setVolumeType(EnumUtils.valueOf(VolumeType.class, configs[1], true));
                    tempVar.setvolumeFormat(EnumUtils.valueOf(VolumeFormat.class, configs[2], true));
                    tempVar.setWipeAfterDelete(Boolean.parseBoolean(configs[3]));
                    DiskImageBase dib = tempVar;
                    result.add(dib);
                } catch (RuntimeException exp) {
                    log.errorFormat("Could not parse disk configuration: {0} - ex: {1}", configs, exp.getMessage());
                }
            } else {
                log.errorFormat("Wrong configuration value format: {0} - skipping parsing.", configs);
            }
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
