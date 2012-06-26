package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
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
        StorageType storageType = getStorageType(luns);
        Map<String, LUNs> lunsFromDeviceMap = getLunsFromDeviceMap(storageType);

        for (LUNs lun : luns) {
            // Update LUN's connections
            for (LUN_storage_server_connection_map map : getLunConnections(lun.getLUN_id())) {
                addConnection(lun, getConnection(map.getstorage_server_connection()));
            }

            // Update LUN's 'PathsDictionary' by 'lunsFromDeviceList'
            LUNs lunFromDeviceList = lunsFromDeviceMap.get(lun.getLUN_id());
            lun.setPathsDictionary(lunFromDeviceList.getPathsDictionary());
        }

        setReturnValue(luns);
    }

    private StorageType getStorageType(List<LUNs> luns) {
        StorageType storageType = null;

        if (!luns.isEmpty()) {
            LUNs lun = luns.get(0);
            List<LUN_storage_server_connection_map> lunConnections = getLunConnections(lun.getLUN_id());

            if (!lunConnections.isEmpty()) {
                storage_server_connections connection =
                        getConnection(lunConnections.get(0).getstorage_server_connection());
                storageType = connection.getstorage_type();
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
        return getDbFacade().getLunDAO().getAllForVolumeGroup(getVgId());
    }

    protected Map<String, LUNs> getLunsFromDeviceMap(StorageType storageType) {
        Map<String, LUNs> lunsMap = new HashMap<String, LUNs>();

        if (getParameters().getVdsId() == null) {
            return lunsMap;
        }

        VDSBrokerFrontend vdsBrokerFrontend = getVdsBroker();
        GetDeviceListVDSCommandParameters parameters = new GetDeviceListVDSCommandParameters(
                getParameters().getVdsId(), storageType, false);
        List<LUNs> lunsList = (List<LUNs>) vdsBrokerFrontend.RunVdsCommand(
                VDSCommandType.GetDeviceList, parameters).getReturnValue();

        for (LUNs lun : lunsList) {
            lunsMap.put(lun.getLUN_id(), lun);
        }

        return lunsMap;
    }

    protected List<LUN_storage_server_connection_map> getLunConnections(String lunId) {
        return getDbFacade().getStorageServerConnectionLunMapDAO().getAll(lunId);
    }

    protected storage_server_connections getConnection(String cnxId) {
        return getDbFacade().getStorageServerConnectionDAO().get(cnxId);
    }

    protected void addConnection(LUNs lun, storage_server_connections cnx) {
        if (lun.getLunConnections() == null) {
            lun.setLunConnections(new ArrayList<storage_server_connections>());
        }
        lun.getLunConnections().add(cnx);
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return getBackend().getResourceManager();
    }
}
