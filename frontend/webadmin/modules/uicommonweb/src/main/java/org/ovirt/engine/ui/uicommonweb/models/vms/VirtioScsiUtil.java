package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.VirtioMultiQueueType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class VirtioScsiUtil {

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private VirtioScasiEnablingFinished finishedCallback;

    private UnitVmModel model;

    public VirtioScsiUtil(UnitVmModel model) {
        this.model = model;
    }

    public void updateVirtioScsiEnabled(final Guid vmId, int osId) {
        this.updateVirtioScsiEnabled(vmId, osId, new VirtioScasiEnablingFinished() {
            @Override
            public void beforeUpdates() {

            }

            @Override
            public void afterUpdates() {

            }
        });
    }

    public void updateVirtioScsiEnabled(final Guid vmId, int osId, VirtioScasiEnablingFinished finishedCallback) {
        this.finishedCallback = finishedCallback;

        final Cluster cluster = model.getSelectedCluster();
        if (cluster == null) {
            return;
        }

        // VirtIO_SCSI is not chipset-dependent at the moment, so we can pass chipset == null to the call
        AsyncDataProvider.getInstance().getDiskInterfaceList(osId, cluster.getCompatibilityVersion(), null,
                model.asyncQuery(diskInterfaces -> {
                    boolean isOsSupportVirtioScsi = diskInterfaces.contains(DiskInterface.VirtIO_SCSI);

                    callBeforeUpdates();
                    model.getIsVirtioScsiEnabled().setIsChangeable(isOsSupportVirtioScsi);

                    if (!isOsSupportVirtioScsi) {
                        model.getIsVirtioScsiEnabled().setEntity(false);
                        model.getIsVirtioScsiEnabled().setChangeProhibitionReason(constants.cannotEnableVirtioScsiForOs());
                        model.getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.DISABLED);
                        callAfterUpdates();
                    } else {
                        AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(model.asyncQuery(returnValue -> {
                            model.getIsVirtioScsiEnabled().setEntity(returnValue);
                            callAfterUpdates();
                        }), vmId);
                    }
                }));
    }

    public void callBeforeUpdates() {
        if (finishedCallback != null) {
            finishedCallback.beforeUpdates();
        }
    }

    public void callAfterUpdates() {
        if (finishedCallback != null) {
            finishedCallback.afterUpdates();
        }
    }

    public static interface VirtioScasiEnablingFinished {
        void beforeUpdates();

        void afterUpdates();
    }
}
