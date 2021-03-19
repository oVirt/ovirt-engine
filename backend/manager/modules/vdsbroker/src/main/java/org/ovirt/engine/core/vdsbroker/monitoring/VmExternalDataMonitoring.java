package org.ovirt.engine.core.vdsbroker.monitoring;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.qualifiers.VmDeleted;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetVmExternalDataVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmExternalDataReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Support for updates of external data while a VM is running.
 */
@Singleton
public class VmExternalDataMonitoring {

    private static final Logger log = LoggerFactory.getLogger(VmExternalDataMonitoring.class);
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private VmDao vmDao;

    private class ExternalDataHashes {

        private Map<VmExternalDataKind, String> dataHashes;

        public String getDataHash(VmExternalDataKind dataKind) {
            return dataHashes.get(dataKind);
        }

        public void setDataHash(VmExternalDataKind dataKind, String dataHash) {
            dataHashes.put(dataKind, dataHash);
        }

        public ExternalDataHashes(String tpmHash, String nvramHash) {
            dataHashes = new EnumMap<>(VmExternalDataKind.class);
            dataHashes.put(VmExternalDataKind.TPM, tpmHash);
            dataHashes.put(VmExternalDataKind.NVRAM, nvramHash);
        }
    }

    private ConcurrentMap<Guid, ExternalDataHashes> vmHashes = new ConcurrentHashMap<>();

    /**
     * Update VM external data if needed. The data is updated only if the new hash
     * is different from the hash of the currently stored data or if the data hash
     * is not known (e.g. when there is no hash for a new VM). If the data update
     * fails then an error is logged and the update will be attempted once this
     * method is called again (and there is still hash mismatch). Different kinds of
     * external data (such as TPM or NVRAM) are handled independently within the
     * method.
     *
     * @param vmId          Id of the VM
     * @param vdsId         Id of the VDS the VM currently runs on
     * @param tpmDataHash   Hash of the possibly updated TPM data
     * @param nvramDataHash Hash of the possibly updated NVRAM data
     */
    public void updateVm(Guid vmId, Guid vdsId, String tpmDataHash, String nvramDataHash) {
        if (tpmDataHash != null || nvramDataHash != null) {
            ExternalDataHashes externalDataHashes = vmHashes.computeIfAbsent(vmId,
                    k -> new ExternalDataHashes(vmDao.getTpmData(k).getSecond(), vmDao.getNvramData(k).getSecond()));
            saveExternalData(vmId, vdsId, externalDataHashes, VmExternalDataKind.TPM, tpmDataHash,
                    (data, hash) -> vmDao.updateTpmData(vmId, data, hash));
            saveExternalData(vmId, vdsId, externalDataHashes, VmExternalDataKind.NVRAM, nvramDataHash,
                    (data, hash) -> vmDao.updateNvramData(vmId, data, hash));
        }
    }

    private void saveExternalData(Guid vmId, Guid vdsId, ExternalDataHashes externalDataHashes,
            VmExternalDataKind dataKind, String newDataHash, BiConsumer<String, String> storeFunction) {
        if (newDataHash != null) {
            synchronized (externalDataHashes) {
                if (newDataHash.equals(externalDataHashes.getDataHash(dataKind))) {
                    return;
                }
            }
            log.debug("Updating {} data due to hash change to {}", dataKind, newDataHash);
            VDSReturnValue retVal;
            try {
                retVal = resourceManager.runVdsCommand(VDSCommandType.GetVmExternalData,
                        new GetVmExternalDataVDSCommand.Parameters(vdsId, vmId, dataKind, false));
            } catch (Throwable e) {
                log.error("Exception when retrieving {} data for {}: {}", dataKind, vmId, e.getMessage());
                return;
            }
            if (retVal.getSucceeded()) {
                VmExternalDataReturn externalDataReturn = (VmExternalDataReturn) retVal.getReturnValue();
                String data = externalDataReturn.data;
                if (data != null && !data.equals("")) {
                    synchronized (externalDataHashes) {
                        try {
                            storeFunction.accept(data, externalDataReturn.hash);
                        } catch (Throwable e) {
                            log.error("Failed to store {} data for {}: {}", dataKind, vmId, e.getMessage());
                            return;
                        }
                        externalDataHashes.setDataHash(dataKind, externalDataReturn.hash);
                    }
                    log.debug("{} data with hash {} updated for {}", dataKind, externalDataReturn.hash, vmId);
                } else {
                    log.debug("{} data not provided for {}", dataKind, vmId);
                }
            } else {
                log.error("Failed to retrieve {} data for {}: {}", dataKind, vmId, retVal.getVdsError());
            }
        }
    }

    /**
     * Remove cached hashes of external data of the given VM.
     *
     * @param vmId Id of the VM to remove the data for from the cache
     */
    private void onVmDelete(@Observes @VmDeleted Guid vmId) {
        vmHashes.remove(vmId);
    }
}
