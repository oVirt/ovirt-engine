package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
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
    protected void executeQueryCommand() {
        List<LUNs> luns = getLunsForVgId(getVgId());
        List<LUNs> nonDummyLuns = new ArrayList<LUNs>(luns.size());
        StorageType storageType = getStorageType(luns);
        Map<String, LUNs> lunsFromDeviceMap = getLunsFromDeviceMap(storageType);

        for (LUNs lun : luns) {
            // Filter dummy luns
            if (lun.getLUN_id().startsWith(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX)) {
                continue;
            }
            nonDummyLuns.add(lun);

            // Update LUN's connections
            for (LUNStorageServerConnectionMap map : getLunConnections(lun.getLUN_id())) {
                addConnection(lun, getConnection(map.getstorage_server_connection()));
            }

            // Update LUN's 'PathsDictionary' by 'lunsFromDeviceList'
            LUNs lunFromDeviceList = lunsFromDeviceMap.get(lun.getLUN_id());
            if (lunFromDeviceList != null) {
                lun.setPathsDictionary(lunFromDeviceList.getPathsDictionary());
            }
        }

        setReturnValue(nonDummyLuns);
    }

    private StorageType getStorageType(List<LUNs> luns) {
        StorageType storageType = null;

        if (!luns.isEmpty()) {
            LUNs lun = luns.get(0);
            List<LUNStorageServerConnectionMap> lunConnections = getLunConnections(lun.getLUN_id());

            if (!lunConnections.isEmpty()) {
                StorageServerConnections connection =
                        getConnection(lunConnections.get(0).getstorage_server_connection());
                storageType = connection.getstorage_type();
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
        Map<String, LUNs> lunsMap = new HashMap<String, LUNs>();

        if (getParameters().getVdsId() == null) {
            return lunsMap;
        }

        VDSBrokerFrontend vdsBrokerFrontend = getVdsBroker();
        GetDeviceListVDSCommandParameters parameters = new GetDeviceListVDSCommandParameters(
                getParameters().getVdsId(), storageType);
        List<LUNs> lunsList = (List<LUNs>) vdsBrokerFrontend.RunVdsCommand(
                VDSCommandType.GetDeviceList, parameters).getReturnValue();

        for (LUNs lun : lunsList) {
            lunsMap.put(lun.getLUN_id(), lun);
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
            lun.setLunConnections(new ArrayList<StorageServerConnections>());
        }
        lun.getLunConnections().add(cnx);
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return getBackend().getResourceManager();
    }
}
