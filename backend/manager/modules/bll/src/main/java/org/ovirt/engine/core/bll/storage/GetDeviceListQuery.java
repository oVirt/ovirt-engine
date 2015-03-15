package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class GetDeviceListQuery<P extends GetDeviceListQueryParameters> extends QueriesCommandBase<P> {

    public GetDeviceListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<LUNs> returnValue = new ArrayList<LUNs>();
        VDS vds = getDbFacade().getVdsDao().get(getParameters().getVdsId());
        boolean filteringLUNsEnabled = Config.<Boolean> getValue(ConfigValues.FilteringLUNsEnabled,
                vds.getVdsGroupCompatibilityVersion().getValue());

        // Get Device List
        VDSBrokerFrontend vdsBrokerFrontend = getVdsBroker();
        GetDeviceListVDSCommandParameters parameters = new GetDeviceListVDSCommandParameters(
                getParameters().getVdsId(), getParameters().getStorageType());
        List<LUNs> luns = (List<LUNs>) vdsBrokerFrontend.RunVdsCommand(
                VDSCommandType.GetDeviceList, parameters).getReturnValue();

        // Get LUNs from DB
        List<LUNs> lunsFromDb = getDbFacade().getLunDao().getAll();
        HashMap<String, LUNs> lunsFromDbById = new HashMap<String, LUNs>();
        for (LUNs lun : lunsFromDb) {
            lunsFromDbById.put(lun.getLUN_id(), lun);
        }

        for (LUNs lun : luns) {
            // Filtering code should be deprecated once DC level 3.0 is no longer supported
            if (filteringLUNsEnabled) {
                if (StringUtils.isNotEmpty(lun.getvolume_group_id())) {
                    log.debug("LUN with GUID {} already has VG ID {}, so not returning it.",
                            lun.getLUN_id(), lun.getvolume_group_id());
                } else if (lunsFromDbById.containsKey(lun.getLUN_id())) {
                    log.debug("LUN with GUID {} already exists in the DB, so not returning it.",
                            lun.getLUN_id());
                } else {
                    returnValue.add(lun);
                }
            }
            else {
                // if the LUN exists in DB, update its values
                if (lunsFromDbById.containsKey(lun.getLUN_id())) {
                    LUNs lunFromDb = lunsFromDbById.get(lun.getLUN_id());
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

    protected VDSBrokerFrontend getVdsBroker() {
        return Backend.getInstance().getResourceManager();
    }
}
