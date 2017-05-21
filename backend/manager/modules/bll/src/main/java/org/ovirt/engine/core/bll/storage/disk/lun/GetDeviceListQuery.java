package org.ovirt.engine.core.bll.storage.disk.lun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.LunDao;

public class GetDeviceListQuery<P extends GetDeviceListQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private LunDao lunDao;

    public GetDeviceListQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
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
        Map<String, LUNs> lunsFromDbById = lunsFromDb.stream()
                .collect(Collectors.toMap(LUNs::getLUNId, Function.identity()));

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
}
