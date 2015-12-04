package org.ovirt.engine.core.bll.storage.disk.lun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

/**
 * A query for retrieving the LUNs composing a storage domain.
 */
public class GetLunsByVgIdQuery<P extends GetLunsByVgIdParameters> extends QueriesCommandBase<P> {

    public GetLunsByVgIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
    }

    @Override
    protected void executeQueryCommand() {
        List<LUNs> luns = getLunsForVgId(getVgId());
        List<LUNs> nonDummyLuns = new ArrayList<>(luns.size());
        StorageType storageType = getStorageType(luns);
        Map<String, LUNs> lunsFromDeviceMap = getLunsFromDeviceMap(storageType);

        for (LUNs lun : luns) {
            // Filter dummy luns
            if (lun.getLUNId().startsWith(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX)) {
                continue;
            }
            nonDummyLuns.add(lun);

            // Update LUN's connections
            for (LUNStorageServerConnectionMap map : getLunConnections(lun.getLUNId())) {
                addConnection(lun, getConnection(map.getStorageServerConnection()));
            }

            // Update LUN's 'PathsDictionary' by 'lunsFromDeviceList'
            LUNs lunFromDeviceList = lunsFromDeviceMap.get(lun.getLUNId());
            if (lunFromDeviceList != null) {
                lun.setPathsDictionary(lunFromDeviceList.getPathsDictionary());
                lun.setPathsCapacity(lunFromDeviceList.getPathsCapacity());
                lun.setPvSize(lunFromDeviceList.getPvSize());
            }
        }

        setReturnValue(nonDummyLuns);
    }

    private StorageType getStorageType(List<LUNs> luns) {
        StorageType storageType = null;

        if (!luns.isEmpty()) {
            LUNs lun = luns.get(0);
            List<LUNStorageServerConnectionMap> lunConnections = getLunConnections(lun.getLUNId());

            if (!lunConnections.isEmpty()) {
                StorageServerConnections connection =
                        getConnection(lunConnections.get(0).getStorageServerConnection());
                storageType = connection.getStorageType();
            } else {
                storageType = StorageType.FCP;
            }
        }

        return storageType;
    }

    protected void setReturnValue(List<LUNs> luns) {
        getQueryReturnValue().setReturnValue(luns);
    }

    protected String getVgId() {
        return getParameters().getVgId();
    }

    protected List<LUNs> getLunsForVgId(String vgId) {
        return getDbFacade().getLunDao().getAllForVolumeGroup(getVgId());
    }

    protected Map<String, LUNs> getLunsFromDeviceMap(StorageType storageType) {
        Map<String, LUNs> lunsMap = new HashMap<>();

        if (getParameters().getId() == null) {
            return lunsMap;
        }

        GetDeviceListVDSCommandParameters parameters = new GetDeviceListVDSCommandParameters(
                getParameters().getId(), storageType);
        List<LUNs> lunsList = (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();

        for (LUNs lun : lunsList) {
            lunsMap.put(lun.getLUNId(), lun);
        }

        return lunsMap;
    }

    protected List<LUNStorageServerConnectionMap> getLunConnections(String lunId) {
        return getDbFacade().getStorageServerConnectionLunMapDao().getAll(lunId);
    }

    protected StorageServerConnections getConnection(String cnxId) {
        return getDbFacade().getStorageServerConnectionDao().get(cnxId);
    }

    protected void addConnection(LUNs lun, StorageServerConnections cnx) {
        if (lun.getLunConnections() == null) {
            lun.setLunConnections(new ArrayList<>());
        }
        lun.getLunConnections().add(cnx);
    }
}
