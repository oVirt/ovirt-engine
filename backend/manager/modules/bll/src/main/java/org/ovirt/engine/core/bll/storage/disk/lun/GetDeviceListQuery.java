package org.ovirt.engine.core.bll.storage.disk.lun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.VdsDao;

public class GetDeviceListQuery<P extends GetDeviceListQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private LunDao lunDao;

    public GetDeviceListQuery(P parameters) {
        super(parameters);
    }

    @Inject
    private VdsDao vdsDao;

    @Override
    protected void executeQueryCommand() {
        if (getParameters().isValidateHostStatus() && !isActiveVds()) {
            return;
        }
        List<LUNs> returnValue = new ArrayList<>();
        // Get Device List
        GetDeviceListVDSCommandParameters parameters =
                new GetDeviceListVDSCommandParameters(
                        getParameters().getId(),
                        getParameters().getStorageType(),
                        getParameters().isCheckStatus(),
                        getParameters().getLunIds());
        List<LUNs> luns = (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();

        // Get LUNs from DB
        List<LUNs> lunsFromDb = lunDao.getAll();
        HashMap<String, LUNs> lunsFromDbById = new HashMap<>();
        for (LUNs lun : lunsFromDb) {
            lunsFromDbById.put(lun.getLUNId(), lun);
        }

        for (LUNs lun : luns) {
            if (lunsFromDbById.containsKey(lun.getLUNId())) {
                LUNs lunFromDb = lunsFromDbById.get(lun.getLUNId());
                lun.setDiskId(lunFromDb.getDiskId());
                lun.setDiskAlias(lunFromDb.getDiskAlias());
                lun.setStorageDomainId(lunFromDb.getStorageDomainId());
                lun.setStorageDomainName(lunFromDb.getStorageDomainName());
            }

            returnValue.add(lun);
        }

        getQueryReturnValue().setReturnValue(returnValue);
    }

    private boolean isActiveVds() {
        VDS vds = vdsDao.get(getParameters().getId());
        if (vds == null || vds.getStatus() != VDSStatus.Up) {
            getQueryReturnValue().setExceptionString(EngineMessage.ACTION_TYPE_FAILED_SERVER_STATUS_NOT_UP.toString());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }
        return true;
    }
}
