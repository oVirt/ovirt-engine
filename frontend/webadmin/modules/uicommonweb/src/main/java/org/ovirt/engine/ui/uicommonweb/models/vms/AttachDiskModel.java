package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class AttachDiskModel extends NewDiskModel {
    protected static final UIConstants CONSTANTS = ConstantsManager.getInstance().getConstants();

    private Map<DiskStorageType, ListModel<EntityModel<DiskModel>>> attachableDisksMap;

    public AttachDiskModel() {
        attachableDisksMap = new HashMap<Disk.DiskStorageType, ListModel<EntityModel<DiskModel>>>();
        attachableDisksMap.put(DiskStorageType.IMAGE, new ListModel<EntityModel<DiskModel>>());
        attachableDisksMap.put(DiskStorageType.LUN, new ListModel<EntityModel<DiskModel>>());
    }

    public Map<DiskStorageType, ListModel<EntityModel<DiskModel>>> getAttachableDisksMap() {
        return attachableDisksMap;
    }

    @Override
    public void initialize() {
        super.initialize();

        getIsPlugged().setIsAvailable(true);

        // Get internal attachable disks
        AsyncDataProvider.getInstance().getAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                Collections.sort(disks, new Linq.DiskByAliasComparer());
                ArrayList<DiskModel> diskModels = Linq.disksToDiskModelList(disks);

                getAttachableDisksMap().get(DiskStorageType.IMAGE).setItems(Linq.toEntityModelList(
                        Linq.filterDisksByType(diskModels, Disk.DiskStorageType.IMAGE)));
            }
        }), getVm().getStoragePoolId(), getVm().getId());

        // Get external attachable disks
        AsyncDataProvider.getInstance().getAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                Collections.sort(disks, new Linq.DiskByAliasComparer());
                ArrayList<DiskModel> diskModels = Linq.disksToDiskModelList(disks);

                getAttachableDisksMap().get(DiskStorageType.LUN).setItems(Linq.toEntityModelList(
                        Linq.filterDisksByType(diskModels, Disk.DiskStorageType.LUN)));
            }
        }), null, getVm().getId());
    }

    @Override
    public boolean validate() {
        if (isNoSelection()) {
            getInvalidityReasons().add(CONSTANTS.noDisksSelected());
            setIsValid(false);
            return false;
        }
        return true;
    }

    @Override
    public void onSave() {
        if (getProgress() != null || !validate()) {
            return;
        }

        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();

        IFrontendActionAsyncCallback onFinishCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                NewDiskModel diskModel = (NewDiskModel) result.getState();
                diskModel.stopProgress();
                diskModel.cancel();
            }
        };

        List<EntityModel<DiskModel>> disksToAttach = getSelectedDisks();
        for (int i = 0; i < disksToAttach.size(); i++) {
            DiskModel disk = disksToAttach.get(i).getEntity();
            // Disk is attached to VM as read only or not, null is applicable only for floating disks
            // but this is not a case here.
            AttachDetachVmDiskParameters parameters = new AttachDetachVmDiskParameters(
                    getVm().getId(), disk.getDisk().getId(), getIsPlugged().getEntity(),
                    Boolean.TRUE.equals(disk.getDisk().getReadOnly()));

            actionTypes.add(VdcActionType.AttachDiskToVm);
            paramerterList.add(parameters);
            callbacks.add(i == disksToAttach.size() - 1 ? onFinishCallback : null);
        }

        startProgress(null);

        Frontend.getInstance().runMultipleActions(actionTypes, paramerterList, callbacks, null, this);
    }

    private boolean isNoSelection() {
        for (ListModel<EntityModel<DiskModel>> listModel : attachableDisksMap.values()) {
            if (listModel.getSelectedItems() != null && !listModel.getSelectedItems().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private List<EntityModel<DiskModel>> getSelectedDisks() {
        List<EntityModel<DiskModel>> selectedDisks = new ArrayList<EntityModel<DiskModel>>();
        for (ListModel<EntityModel<DiskModel>> listModel : attachableDisksMap.values()) {
            if (listModel.getSelectedItems() != null && !listModel.getSelectedItems().isEmpty()) {
                selectedDisks.addAll(listModel.getSelectedItems());
            }
        }
        return selectedDisks;
    }
}
