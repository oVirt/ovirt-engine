package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

public class GetStorageDomainsWithAttachedStoragePoolGuidQuery<P extends StorageDomainsAndStoragePoolIdQueryParameters> extends QueriesCommandBase<P> {

    private Guid vdsId;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private StorageHelperDirector storageHelperDirector;

    public GetStorageDomainsWithAttachedStoragePoolGuidQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<StorageDomainStatic> storageDomainsWithAttachedStoragePoolId = new ArrayList<>();
        if ((getVdsForConnectStorage() != null) && isDataCenterValidForAttachedStorageDomains()) {
            storageDomainsWithAttachedStoragePoolId = filterAttachedStorageDomains();
        }
        getQueryReturnValue().setReturnValue(storageDomainsWithAttachedStoragePoolId);
    }

    private Guid getVdsForConnectStorage() {
        vdsId = getParameters().getVdsId();
        if (vdsId == null) {
            // Get a Host which is at UP state to connect to the Storage Domain.
            List<VDS> hosts = vdsDao.getAllForStoragePoolAndStatus(getParameters().getId(), VDSStatus.Up);
            if (!hosts.isEmpty()) {
                vdsId = hosts.get(new Random().nextInt(hosts.size())).getId();
                log.info("vds id '{}' was chosen to fetch the Storage domain info", vdsId);
            } else {
                log.warn("There is no available vds in UP state to fetch the Storage domain info from VDSM");
            }
        }
        return vdsId;
    }

    private boolean isDataCenterValidForAttachedStorageDomains() {
        if (getParameters().isCheckStoragePoolStatus()) {
            StoragePool storagePool = storagePoolDao.get(getParameters().getId());
            if ((storagePool == null) || (storagePool.getStatus() != StoragePoolStatus.Up)) {
                log.info("The Data Center is not in UP status.");
                return false;
            }
        }
        return true;
    }

    protected List<StorageDomainStatic> filterAttachedStorageDomains() {
        List<StorageDomain> connectedStorageDomainsToVds = new ArrayList<>();
        for (StorageDomain storageDomain : getParameters().getStorageDomainList()) {
            if (!connectStorageDomain(storageDomain)) {
                logErrorMessage(storageDomain);
            } else {
                connectedStorageDomainsToVds.add(storageDomain);
            }
        }

        // Some domains may have Hosted Engine VM running while importing them.
        // We want to avoid disconnecting before the import in that case, otherwise they'll crash
        List<Guid> heStorageDomainIds = storageDomainDao.getHostedEngineStorageDomainIds();

        List<StorageDomainStatic> storageDomainsWithAttachedStoragePoolId =
                getAttachedStorageDomains(connectedStorageDomainsToVds);
        for (StorageDomain storageDomain : connectedStorageDomainsToVds) {
            if (heStorageDomainIds.contains(storageDomain.getId())) {
                log.info(
                        "Skipping disconnect Storage Domain {} from VDS '{}' because Hosted Engine VM is running on it.",
                        storageDomain.getName(),
                        getVdsId()
                );
                continue;
            }
            if (!disconnectStorageDomain(storageDomain)) {
                log.warn("Could not disconnect Storage Domain {} from VDS '{}'. ", storageDomain.getName(), getVdsId());
            }
        }
        return storageDomainsWithAttachedStoragePoolId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    protected List<StorageDomainStatic> getAttachedStorageDomains(List<StorageDomain> storageDomains) {
        VDSReturnValue vdsReturnValue = null;
        List<StorageDomainStatic> storageDomainsWithAttachedStoragePoolId = new ArrayList<>();

        // Go over the list of Storage Domains and try to get the Storage Domain info to check if it is attached to
        // another Storage Pool
        for (StorageDomain storageDomain : storageDomains) {
            try {
                vdsReturnValue =
                        runVdsCommand(VDSCommandType.HSMGetStorageDomainInfo,
                                new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(), storageDomain.getId()));
            } catch (RuntimeException e) {
                logErrorMessage(storageDomain);
                continue;
            }
            if (!vdsReturnValue.getSucceeded()) {
                logErrorMessage(storageDomain);
                continue;
            }
            Pair<StorageDomainStatic, Guid> domainFromIrs =
                    (Pair<StorageDomainStatic, Guid>) vdsReturnValue.getReturnValue();
            if (domainFromIrs.getSecond() != null) {
                storageDomainsWithAttachedStoragePoolId.add(domainFromIrs.getFirst());
            }
        }
        return storageDomainsWithAttachedStoragePoolId;
    }

    protected boolean connectStorageDomain(StorageDomain storageDomain) {
        try {
            return storageHelperDirector.getItem(storageDomain.getStorageType())
                    .connectStorageToDomainByVdsId(storageDomain, getVdsId());
        } catch (RuntimeException e) {
            log.error("Exception while connecting a storage domain", e);
            return false;
        }
    }

    protected boolean disconnectStorageDomain(StorageDomain storageDomain) {
        try {
            return storageHelperDirector.getItem(storageDomain.getStorageType())
                    .disconnectStorageFromDomainByVdsId(storageDomain, getVdsId());
        } catch (RuntimeException e) {
            log.error("Exception while disconnecting a storage domain", e);
            return false;
        }
    }

    protected void logErrorMessage(StorageDomain storageDomain) {
        if (storageDomain != null) {
            log.error("Could not get Storage Domain info for Storage Domain (name:'{}', id:'{}') with VDS '{}'. ",
                    storageDomain.getName(),
                    storageDomain.getId(),
                    getVdsId());
        } else {
            log.error("Could not get Storage Domain info with VDS '{}'. ", getVdsId());
        }
    }
}
