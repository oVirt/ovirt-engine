package org.ovirt.engine.core.bll.storage.disk.lun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class GetDeviceListQuery<P extends GetDeviceListQueryParameters> extends QueriesCommandBase<P> {

    public GetDeviceListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<LUNs> returnValue = new ArrayList<>();
        VDS vds = getDbFacade().getVdsDao().get(getParameters().getId());
        boolean filteringLUNsEnabled = Config.<Boolean> getValue(ConfigValues.FilteringLUNsEnabled,
                vds.getVdsGroupCompatibilityVersion().getValue());

        // Get Device List
        GetDeviceListVDSCommandParameters parameters =
                new GetDeviceListVDSCommandParameters(
                        getParameters().getId(),
                        getParameters().getStorageType(),
                        getParameters().isCheckStatus(),
                        getParameters().getLunIds());
        List<LUNs> luns = (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();

        // Get LUNs from DB
        List<LUNs> lunsFromDb = getDbFacade().getLunDao().getAll();
        HashMap<String, LUNs> lunsFromDbById = new HashMap<>();
        for (LUNs lun : lunsFromDb) {
            lunsFromDbById.put(lun.getLUNId(), lun);
        }

        for (LUNs lun : luns) {
            // Filtering code should be deprecated once DC level 3.0 is no longer supported
            if (filteringLUNsEnabled) {
                if (StringUtils.isNotEmpty(lun.getVolumeGroupId())) {
                    log.debug("LUN with GUID {} already has VG ID {}, so not returning it.",
                            lun.getLUNId(), lun.getVolumeGroupId());
                } else if (lunsFromDbById.containsKey(lun.getLUNId())) {
                    log.debug("LUN with GUID {} already exists in the DB, so not returning it.",
                            lun.getLUNId());
                } else {
                    returnValue.add(lun);
                }
            }
            else {
                // if the LUN exists in DB, update its values
                if (lunsFromDbById.containsKey(lun.getLUNId())) {
                    LUNs lunFromDb = lunsFromDbById.get(lun.getLUNId());
                    lun.setDiskId(lunFromDb.getDiskId());
                    lun.setDiskAlias(lunFromDb.getDiskAlias());
                    lun.setStorageDomainId(lunFromDb.getStorageDomainId());
                    lun.setStorageDomainName(lunFromDb.getStorageDomainName());
                }

                returnValue.add(lun);
            }
        }

        getQueryReturnValue().setReturnValue(returnValue);
    }
}
