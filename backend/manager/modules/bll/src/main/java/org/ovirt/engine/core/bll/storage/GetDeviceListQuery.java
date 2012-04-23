package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDeviceListQuery<P extends GetDeviceListQueryParameters> extends QueriesCommandBase<P> {
    public GetDeviceListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<LUNs> luns = (List<LUNs>) Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.GetDeviceList,
                        new GetDeviceListVDSCommandParameters(getParameters().getVdsId(), getParameters()
                                .getStorageType())).getReturnValue();

        List<LUNs> lunsFromDb = DbFacade.getInstance().getLunDAO().getAll();
        java.util.HashMap<String, LUNs> lunsFromDbById = new java.util.HashMap<String, LUNs>();
        for (LUNs lun : lunsFromDb) {
            lunsFromDbById.put(lun.getLUN_id(), lun);
        }
        java.util.ArrayList<LUNs> returnValue = new java.util.ArrayList<LUNs>();
        for (LUNs lun : luns) {
            if (!StringHelper.isNullOrEmpty(lun.getvolume_group_id())) {
                log.debugFormat("LUN with GUID {0} already has VG ID {1}, so not returning it.",
                        lun.getLUN_id(), lun.getvolume_group_id());
            } else if (lunsFromDbById.containsKey(lun.getLUN_id())) {
                log.debugFormat("LUN with GUID {0} already exists in the DB, so not returning it.",
                        lun.getLUN_id());
            } else {
                returnValue.add(lun);
            }
        }
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
