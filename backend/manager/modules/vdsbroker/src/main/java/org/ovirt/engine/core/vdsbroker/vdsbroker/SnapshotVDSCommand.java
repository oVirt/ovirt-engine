package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.SnapshotVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Switch the currently active images that the VM is running on, to the new images which are supplied in the parameters.
 * If VM is not running, then error will occur.<br>
 * <br>
 * Command on VDSM side is expected to be synchronous & atomic.<br>
 * For more info see: <a href="http://www.ovirt.org/wiki/Live_Snapshots">http://www.ovirt.org/wiki/Live_Snapshots</a>
 */
public class SnapshotVDSCommand<P extends SnapshotVDSCommandParameters> extends VdsBrokerCommand<P> {

    public SnapshotVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid taskId = Guid.newGuid();
        status = executeSnapshotVerb(taskId);
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(taskId);
        }
    }

    private StatusOnlyReturn executeSnapshotVerb(Guid taskId) {
        String vmId = getParameters().getVmId().toString();
        String memoryVolume = getParameters().isMemoryVolumeExists() ? createMemoryStringFromDisks() : "";
        if (getParameters().isVmFrozen()) {
            return getBroker().snapshot(vmId, createDisksMap(), memoryVolume, getParameters().isVmFrozen(),
                    taskId.toString(), getParameters().getLiveSnapshotTimeout());
        } else if (getParameters().isMemoryVolumeExists()) {
            return getBroker().snapshot(vmId, createDisksMap(), memoryVolume, taskId.toString(),
                    getParameters().getLiveSnapshotTimeout());
        } else {
            return getBroker().snapshot(vmId, createDisksMap(), taskId.toString(),
                    getParameters().getLiveSnapshotTimeout());
        }
    }

    private String createMemoryStringFromDisks() {
        return String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s",
                getParameters().getMemoryDump().getStorageIds().get(0),
                getParameters().getMemoryDump().getStoragePoolId(),
                getParameters().getMemoryDump().getId(),
                getParameters().getMemoryDump().getImageId(),
                getParameters().getMemoryConf().getId(),
                getParameters().getMemoryConf().getImageId());
    }

    private Map<String, String>[] createDisksMap() {
        @SuppressWarnings("unchecked")
        Map<String, String>[] result = new HashMap[getParameters().getImages().size()];

        for (int i = 0; i < result.length; i++) {
            DiskImage image = getParameters().getImages().get(i);
            Map<String, String> imageParams = new HashMap<>();
            imageParams.put("domainID", image.getStorageIds().get(0).toString());
            imageParams.put("imageID", image.getId().toString());
            imageParams.put("baseVolumeID", image.getParentId().toString());
            imageParams.put("volumeID", image.getImageId().toString());
            result[i] = imageParams;
        }

        return result;
    }
}
