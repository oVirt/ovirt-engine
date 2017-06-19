package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CloneVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class CloneVmModel extends Model {

    private VM vm;

    private EntityModel<String> cloneName;

    private UIConstants uiConstants;

    public CloneVmModel(VM vm, UIConstants uiConstants) {
        this.vm = vm;
        this.uiConstants = uiConstants;

        cloneName = new EntityModel<>();
    }

    @Override
    public void initialize() {
        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery<>(disks -> {
            if (disks.stream().anyMatch(d -> d.getDiskStorageType() == DiskStorageType.LUN)) {
                setMessage(ConstantsManager.getInstance().getConstants().cloneVmLunsWontBeCloned());
            }
        }), vm.getId());
    }

    public EntityModel<String> getCloneName() {
        return cloneName;
    }

    public void setCloneName(EntityModel<String> cloneName) {
        this.cloneName = cloneName;
    }

    public VM getVm() {
        return vm;
    }

    public void onClone(final Model targetModel, final boolean makeCreatorExplicitOwner) {
        if (!validate()) {
            return;
        }

        startProgress();

        AsyncDataProvider.getInstance().isVmNameUnique(new AsyncQuery<>(returnValue -> {
            if (returnValue) {
                postCloneVmNameUnique(targetModel, makeCreatorExplicitOwner);
            } else {
                stopProgress();
                getCloneName()
                        .getInvalidityReasons()
                        .add(uiConstants.nameMustBeUniqueInvalidReason());
                getCloneName().setIsValid(false);
            }
        }), getCloneName().getEntity(), getVm() == null ? null : getVm().getStoragePoolId());

    }

    private void postCloneVmNameUnique(final Model targetModel, boolean makeCreatorExplicitOwner) {
        CloneVmParameters params = new CloneVmParameters(
                getVm(),
                getCloneName().getEntity());

        params.setMakeCreatorExplicitOwner(makeCreatorExplicitOwner);

        Frontend.getInstance().runAction(ActionType.CloneVm, params,
                result -> {
                    stopProgress();
                    targetModel.setWindow(null);
                }, this);
    }

    public boolean validate() {
        getCloneName().validateEntity(
                new IValidation[]{
                        new NotEmptyValidation(),
                        new LengthValidation(AsyncDataProvider.getInstance().getMaxVmNameLength()),
                        new I18NNameValidation()
                });

        return getCloneName().getIsValid();
    }
}
