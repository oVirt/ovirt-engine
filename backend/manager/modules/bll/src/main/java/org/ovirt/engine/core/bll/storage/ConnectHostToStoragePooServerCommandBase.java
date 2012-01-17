package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public abstract class ConnectHostToStoragePooServerCommandBase<T extends StoragePoolParametersBase> extends
        StorageHandlingCommandBase<T> {
    private List<storage_server_connections> _connections;
    private java.util.ArrayList<storage_server_connections> _isoConnections;
    private java.util.ArrayList<storage_server_connections> _exportConnections;
    private StorageType _isoType = StorageType.NFS;
    private StorageType _exportType = StorageType.NFS;
    private boolean _needToConnectIso = false;
    private boolean _needToConnectExport = false;

    public ConnectHostToStoragePooServerCommandBase(T parameters) {
        super(parameters);
    }

    protected boolean getNeedToConnectIso() {
        return _needToConnectIso;
    }

    protected void setNeedToConnectIso(boolean value) {
        _needToConnectIso = value;
    }

    protected boolean getNeedToConnectExport() {
        return _needToConnectExport;
    }

    protected void setNeedToConnectExport(boolean value) {
        _needToConnectExport = value;
    }

    protected List<storage_server_connections> getConnections() {
        return _connections;
    }

    protected java.util.ArrayList<storage_server_connections> getIsoConnections() {
        return _isoConnections;
    }

    protected java.util.ArrayList<storage_server_connections> getExportConnections() {
        return _exportConnections;
    }

    protected StorageType getIsoType() {
        return _isoType;
    }

    protected StorageType getExportType() {
        return _exportType;
    }

    protected void InitConnectionList() {
        java.util.ArrayList<storage_domains> isoDomains = GetStorageDomainsByStoragePoolId(StorageDomainType.ISO);
        java.util.ArrayList<storage_domains> exportDomains =
                GetStorageDomainsByStoragePoolId(StorageDomainType.ImportExport);

        java.util.HashSet<storage_server_connections> connections = new java.util.HashSet<storage_server_connections>(
                DbFacade.getInstance().getStorageServerConnectionDAO().getAllForStoragePool(getStoragePool().getId()));
        if (isoDomains.size() != 0) {
            _isoType = isoDomains.get(0).getstorage_type();
            java.util.HashSet<storage_server_connections> isoConnections =
                    new java.util.HashSet<storage_server_connections>(
                            StorageHelperDirector.getInstance().getItem(getIsoType())
                                    .GetStorageServerConnectionsByDomain(isoDomains.get(0).getStorageStaticData()));
            if (_isoType != getStoragePool().getstorage_pool_type()) {
                for (storage_server_connections connection : isoConnections) {
                    if (connections.contains(connection)) {
                        connections.remove(connection);
                    }
                }
            } else {
                for (storage_server_connections connection : connections) {
                    if (isoConnections.contains(connection)) {
                        isoConnections.remove(connection);
                    }
                }
            }
            _isoConnections = new java.util.ArrayList<storage_server_connections>(isoConnections);
            setNeedToConnectIso(_isoConnections.size() > 0);
        }
        if (exportDomains.size() != 0) {
            _exportType = exportDomains.get(0).getstorage_type();
            java.util.HashSet<storage_server_connections> exportConnections =
                    new java.util.HashSet<storage_server_connections>(
                            StorageHelperDirector.getInstance().getItem(getExportType())
                                    .GetStorageServerConnectionsByDomain(exportDomains.get(0).getStorageStaticData()));
            if (_exportType != getStoragePool().getstorage_pool_type()) {
                for (storage_server_connections connection : exportConnections) {
                    if (connections.contains(connection)) {
                        connections.remove(connection);
                    }
                }
            } else {
                for (storage_server_connections connection : connections) {
                    if (exportConnections.contains(connection)) {
                        exportConnections.remove(connection);
                    }
                }
            }
            _exportConnections = new java.util.ArrayList<storage_server_connections>(exportConnections);
            setNeedToConnectExport(exportConnections.size() > 0);
        }
        _connections = new java.util.ArrayList<storage_server_connections>(connections);
    }

    protected java.util.ArrayList<storage_domains> GetStorageDomainsByStoragePoolId(StorageDomainType type) {
        List<storage_domains> allDomains = DbFacade.getInstance().getStorageDomainDAO().getAllForStoragePool(
                getStoragePool().getId());
        java.util.ArrayList<storage_domains> domains = new java.util.ArrayList<storage_domains>();
        for (storage_domains s : allDomains) {
            StorageDomainStatus status = s.getstatus();
            if (s.getstorage_domain_type() == type
                    && (StorageDomainStatus.Active == status || StorageDomainStatus.Unknown == status)) {
                domains.add(s);
            }
        }
        return domains;
    }
}
