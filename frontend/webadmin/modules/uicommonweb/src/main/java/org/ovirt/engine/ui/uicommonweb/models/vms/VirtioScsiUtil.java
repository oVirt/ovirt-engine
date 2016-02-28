package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class VirtioScsiUtil {

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private VirtioScasiEnablingFinished finishedCallback;

    private UnitVmModel model;

    public VirtioScsiUtil(UnitVmModel model) {
        this.model = model;
    }

    public void updateVirtioScsiEnabled(final Guid vmId, int osId, VirtioScasiEnablingFinished finishedCallback) {
        this.finishedCallback = finishedCallback;

        final Cluster cluster = model.getSelectedCluster();
        if (cluster == null) {
            return;
        }

        AsyncDataProvider.getInstance().getDiskInterfaceList(osId, cluster.getCompatibilityVersion(),
                new AsyncQuery(model, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object parentModel, Object returnValue) {
                        ArrayList<DiskInterface> diskInterfaces = (ArrayList<DiskInterface>) returnValue;
                        boolean isOsSupportVirtioScsi = diskInterfaces.contains(DiskInterface.VirtIO_SCSI);

                        callBeforeUpdates();
                        model.getIsVirtioScsiEnabled().setIsChangeable(isOsSupportVirtioScsi);

                        if (!isOsSupportVirtioScsi) {
                            model.getIsVirtioScsiEnabled().setEntity(false);
                            model.getIsVirtioScsiEnabled().setChangeProhibitionReason(constants.cannotEnableVirtioScsiForOs());
                            callAfterUpdates();
                        } else {
                            if (Guid.isNullOrEmpty(vmId)) {
                                model.getIsVirtioScsiEnabled().setEntity(true);
                                callAfterUpdates();
                            } else {
                                AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(new AsyncQuery(model, new INewAsyncCallback() {
                                    @Override
                                    public void onSuccess(Object parentModel, Object returnValue) {
                                        model.getIsVirtioScsiEnabled().setEntity((Boolean) returnValue);
                                        callAfterUpdates();
                                    }
                                }), vmId);
                            }
                        }
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
