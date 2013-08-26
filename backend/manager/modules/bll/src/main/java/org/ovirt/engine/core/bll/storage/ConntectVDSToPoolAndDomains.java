package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class ConntectVDSToPoolAndDomains extends ActivateDeactivateSingleAsyncOperation {

    private static Log log = LogFactory.getLog(ConntectVDSToPoolAndDomains.class);

    public ConntectVDSToPoolAndDomains(ArrayList<VDS> vdss, StorageDomain domain, StoragePool storagePool) {
        super(vdss, domain, storagePool);
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
                        new ConnectStoragePoolVDSCommandParameters(vds.getId(), getStoragePool().getId(), vds
                                .getVdsSpmId(), getStorageDomain().getId(), getStoragePool()
                                .getmaster_domain_version()));
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
