package org.ovirt.engine.core.bll.storage.disk.lun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

/**
 * A query for retrieving the LUNs composing a storage domain.
 */
public class GetLunsByVgIdQuery<P extends GetLunsByVgIdParameters> extends QueriesCommandBase<P> {
    @Inject
    private LunDao lunDao;

    @Inject
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    public GetLunsByVgIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<LUNs> luns = lunDao.getAllForVolumeGroup(getVgId());
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
            for (LUNStorageServerConnectionMap map : storageServerConnectionLunMapDao.getAll(lun.getLUNId())) {
                addConnection(lun, storageServerConnectionDao.get(map.getStorageServerConnection()));
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
            List<LUNStorageServerConnectionMap> lunConnections = storageServerConnectionLunMapDao.getAll(lun.getLUNId());

            if (!lunConnections.isEmpty()) {
                StorageServerConnections connection =
                        storageServerConnectionDao.get(lunConnections.get(0).getStorageServerConnection());
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

    protected void addConnection(LUNs lun, StorageServerConnections cnx) {
        if (lun.getLunConnections() == null) {
            lun.setLunConnections(new ArrayList<>());
        }
        lun.getLunConnections().add(cnx);
    }
}
