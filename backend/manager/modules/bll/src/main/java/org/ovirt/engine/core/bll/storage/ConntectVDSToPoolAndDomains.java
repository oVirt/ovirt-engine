package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class ConntectVDSToPoolAndDomains extends ActivateDeactivateSingleAsyncOperation {

    private static final Log log = LogFactory.getLog(ConntectVDSToPoolAndDomains.class);

    private Guid masterStorageDomainId;

    private List<StoragePoolIsoMap> storagePoolIsoMap;

    public ConntectVDSToPoolAndDomains(ArrayList<VDS> vdss, StorageDomain domain, StoragePool storagePool) {
        super(vdss, domain, storagePool);
        masterStorageDomainId = DbFacade.getInstance().getStorageDomainDao()
                .getMasterStorageDomainIdForPool(getStoragePool().getId());
        storagePoolIsoMap = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(getStoragePool().getId());
    }

    @Override
    public void execute(int iterationId) {
        VDS vds = getVdss().get(iterationId);
        try {
            boolean isConnectSucceeded =
                    StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
                            .connectStorageToDomainByVdsId(getStorageDomain(), vds.getId());
            if (isConnectSucceeded) {
                ResourceManager.getInstance().runVdsCommand(
                        VDSCommandType.ConnectStoragePool,
                        new ConnectStoragePoolVDSCommandParameters(
                                vds, getStoragePool(), masterStorageDomainId, storagePoolIsoMap));
            } else {
                log.errorFormat("Failed to connect host {0} to domain {1}",
                        vds.getName(),
                        getStorageDomain().getStorageName());
            }
        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect host {0} to storage pool {1}. Exception: {3}",
                    vds.getName(),
                    getStoragePool().getName(),
                    e);
        }
    }

}
