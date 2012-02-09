package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;

public class GetDiskConfigurationListQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetDiskConfigurationListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Disk Config is a list of enum values of:
        // diskType,volType,volFormat,wipeAfterDelete;...
        java.util.ArrayList<DiskImageBase> result = new java.util.ArrayList<DiskImageBase>();
        String[] imageBasesList = Config.<String> GetValue(ConfigValues.DiskConfigurationList).split("[;]", -1);
        for (String imageBase : imageBasesList) {
            String[] configs = imageBase.split("[,]", -1);
            if (configs.length == 4) {
                try {
                    DiskImageBase tempVar = new DiskImageBase();
                    tempVar.setdisk_type(EnumUtils.valueOf(DiskType.class, configs[0], true));
                    tempVar.setvolume_type(EnumUtils.valueOf(VolumeType.class, configs[1], true));
                    tempVar.setvolume_format(EnumUtils.valueOf(VolumeFormat.class, configs[2], true));
                    tempVar.setwipe_after_delete(Boolean.parseBoolean(configs[3]));
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

    private static LogCompat log = LogFactoryCompat.getLog(GetDiskConfigurationListQuery.class);
}
