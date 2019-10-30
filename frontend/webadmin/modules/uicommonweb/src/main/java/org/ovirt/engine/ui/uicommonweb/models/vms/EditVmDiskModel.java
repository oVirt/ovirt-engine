package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class EditVmDiskModel extends EditDiskModel {
    public EditVmDiskModel() {
    }

    @Override
    public void initialize() {
        super.initialize();

        getIsBootable().setIsAvailable(true);
        getDiskInterface().setIsAvailable(true);
        getIsReadOnly().setEntity(getDiskVmElement().isReadOnly());
        getIsBootable().setEntity(getDiskVmElement().isBoot());
        getPassDiscard().setEntity(getDiskVmElement().isPassDiscard());

        if (getDisk().getDiskStorageType() == DiskStorageType.LUN) {
            getIsUsingScsiReservation().setEntity(getDiskVmElement().isUsingScsiReservation());
        }

        updateReadOnlyChangeability();
        updatePassDiscardChangeability();
        updateWipeAfterDeleteChangeability();
    }

    @Override
    protected void initDiskVmElement() {
        setDiskVmElement(getDisk().getDiskVmElementForVm(getVm().getId()));
    }

    @Override
    protected boolean isSizeExtendChangeable(DiskImage diskImage) {
        return getVm() != null && !diskImage.isDiskSnapshot() &&
                ActionUtils.canExecute(Collections.singletonList(getVm()), VM.class, ActionType.ExtendImageSize);
    }

    @Override
    public void store(IFrontendActionAsyncCallback callback) {
        if (getProgress() != null || !validate()) {
            return;
        }

        startProgress();

        UpdateDiskParameters parameters = new UpdateDiskParameters(getDiskVmElement(), getDisk());
        IFrontendActionAsyncCallback onFinished = callback != null ? callback : result -> {
            EditVmDiskModel diskModel = (EditVmDiskModel) result.getState();
            diskModel.stopProgress();
            diskModel.cancel();
        };
        Frontend.getInstance().runAction(ActionType.UpdateDisk, parameters, onFinished, this);
    }
}
