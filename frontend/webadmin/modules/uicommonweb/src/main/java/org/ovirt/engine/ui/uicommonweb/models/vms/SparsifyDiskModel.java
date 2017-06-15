package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

@SuppressWarnings("unused")
public class SparsifyDiskModel extends ConfirmationModel {

    public static final String ON_SPARSIFY = "OnSparsifyDisk";
    public static final String CANCEL_SPARSIFY = "CancelSparsifyDisk";

    private List<Disk> disksToSparsify;

    private UICommand cancelCommand;

    public void initialize(VM vm, List<Disk> disksToSparsify, ICommandTarget target) {
        this.disksToSparsify = disksToSparsify;

        UIConstants uiConstants = ConstantsManager.getInstance().getConstants();
        setTitle(uiConstants.sparsifyDisksTitle());
        setMessage(uiConstants.sparsifyConfirmationPopupMessage());
        setHelpTag(HelpTag.sparsify_disk);
        setHashName("sparsify_disk"); //$NON-NLS-1$

        List<DiskModel> items = new ArrayList<>();
        for (Disk disk : disksToSparsify) {
            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);
            diskModel.setVm(vm);

            items.add(diskModel);
        }
        setItems(items);

        UICommand okCommand = UICommand.createDefaultOkUiCommand(ON_SPARSIFY, target);
        getCommands().add(okCommand);
        cancelCommand = UICommand.createCancelUiCommand(CANCEL_SPARSIFY, target);
        getCommands().add(cancelCommand);
    }

    public void onSparsify(final ICommandTarget target) {
        List<ActionParametersBase> parameterList = new ArrayList<>();

        for (Disk disk : disksToSparsify) {
            ActionParametersBase parameters = new StorageJobCommandParameters(((DiskImage) disk).getImageId());
            parameterList.add(parameters);
        }

        startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.SparsifyImage,
                parameterList,
                result -> {
                    stopProgress();
                    target.executeCommand(cancelCommand);
                },
                this);
    }

}
