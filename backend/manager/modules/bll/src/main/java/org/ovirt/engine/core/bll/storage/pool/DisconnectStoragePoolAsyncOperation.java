package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisconnectStoragePoolAsyncOperation extends ActivateDeactivateSingleAsyncOperation {
    private static final Logger log = LoggerFactory.getLogger(DisconnectStoragePoolAsyncOperation.class);

    public DisconnectStoragePoolAsyncOperation(ArrayList<VDS> vdss, StoragePool storagePool) {
        super(vdss, null, storagePool);
    }

    @Override
    public void execute(int iterationId) {
        try {
            if (getVdss().get(iterationId).getSpmStatus() == VdsSpmStatus.None) {
                log.info("Disconnect storage pool treatment vds '{}', pool '{}'", getVdss().get(iterationId)
                        .getName(), getStoragePool().getName());
                Backend.getInstance()
                        .getResourceManager()
                        .runVdsCommand(
                                VDSCommandType.DisconnectStoragePool,
                                new DisconnectStoragePoolVDSCommandParameters(getVdss().get(iterationId).getId(),
                                        getStoragePool().getId(), getVdss().get(iterationId).getVdsSpmId()));
            }
        } catch (RuntimeException e) {
            log.error("Failed to DisconnectStoragePool storagePool. Host '{}' from storage pool '{}': {}",
                    getVdss().get(iterationId).getName(),
                    getStoragePool().getName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }
}
