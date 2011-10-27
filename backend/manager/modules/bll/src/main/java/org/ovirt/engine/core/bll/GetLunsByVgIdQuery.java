package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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

        for (LUNs lun : luns) {
            for (LUN_storage_server_connection_map map : getLunsMap(lun.getLUN_id())) {
                addConnection(lun, getConnection(map.getstorage_server_connection()));
            }
        }

        setReturnValue(luns);
    }

    protected DbFacade getDb() {
        return DbFacade.getInstance();
    }

    protected void setReturnValue(List<LUNs> luns) {
        getQueryReturnValue().setReturnValue(luns);
    }

    protected String getVgId() {
        return getParameters().getVgId();
    }

    protected List<LUNs> getLunsForVgId(String vgId) {
        return getDb().getLunDAO().getAllForVolumeGroup(getVgId());
    }

    protected List<LUN_storage_server_connection_map> getLunsMap(String lunId) {
        return getDb().getStorageServerConnectionLunMapDAO().getAll(lunId);
    }

    protected storage_server_connections getConnection(String cnxId) {
        return getDb().getStorageServerConnectionDAO().get(cnxId);
    }

    protected void addConnection(LUNs lun, storage_server_connections cnx) {
        if (lun.getLunConnections() == null) {
            lun.setLunConnections(new ArrayList<storage_server_connections>());
        }
        lun.getLunConnections().add(cnx);
    }
}
