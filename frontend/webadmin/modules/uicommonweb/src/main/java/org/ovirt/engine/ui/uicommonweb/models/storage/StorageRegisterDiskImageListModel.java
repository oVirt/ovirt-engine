package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.comparators.UnregisteredDiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.queries.IdAndBooleanQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageRegisterDiskImageListModel extends SearchableListModel<StorageDomain, Disk> {
    private UICommand registerCommand;

    public UICommand getRegisterCommand() {
        return registerCommand;
    }

    private void setRegisterCommand(UICommand value) {
        registerCommand = value;
    }

    public StorageRegisterDiskImageListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().disksImportTitle());
        setHashName("disk_image_register"); //$NON-NLS-1$
        setRegisterCommand(new UICommand("ImportDiskImage", this)); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
        if (getEntity() != null) {
            updateActionAvailability();
        }
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    public void search() {
        if (getEntity() == null) {
            setItems(null);
            return;
        }
        super.search();
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            setItems(null);
            return;
        }
        IdQueryParameters parameters = new IdAndBooleanQueryParameters(getEntity().getId(), true);
        parameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetUnregisteredDisksFromDB, parameters,
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        List<UnregisteredDisk> unregisteredDisks = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        Collections.sort(unregisteredDisks, new UnregisteredDiskByDiskAliasComparator());
                        ArrayList<Disk> diskItems = new ArrayList<>();
                        for (UnregisteredDisk unregisteredDisk : unregisteredDisks) {
                            diskItems.add(unregisteredDisk.getDiskImage());
                        }
                        setItems(diskItems);
                    }
                }));
    }

    private void updateActionAvailability() {
        ArrayList<DiskImage> disks = getSelectedItems() != null ?
                Linq.<DiskImage> cast(getSelectedItems()) : new ArrayList<DiskImage>();

        getRegisterCommand().setIsExecutionAllowed(disks.size() > 0
                && getEntity().getStatus() == StorageDomainStatus.Active);
    }

    private void register() {
        if (getWindow() != null) {
            return;
        }

        final RegisterDiskModel registerDiskModel = new RegisterDiskModel();
        registerDiskModel.setTargetAvailable(false);
        setWindow(registerDiskModel);

        // noinspection unchecked
        registerDiskModel.setEntity(this);
        registerDiskModel.init();
        registerDiskModel.setTitle(ConstantsManager.getInstance().getConstants().importDisksTitle());
        registerDiskModel.setHelpTag(HelpTag.import_disks);
        registerDiskModel.setHashName("import_disks"); //$NON-NLS-1$

        registerDiskModel.startProgress();
        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                StoragePool dataCenter = (StoragePool) returnValue;
                registerDiskModel.setQuotaEnforcementType(dataCenter.getQuotaEnforcementType());
                registerDiskModel.setDisks(Linq.disksToDiskModelList(getSelectedItems()));
                registerDiskModel.updateStorageDomain(getEntity());
                registerDiskModel.stopProgress();
            }
        }), getEntity().getStoragePoolId());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRegisterCommand()) {
            register();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageRegisterDiskImageListModel"; //$NON-NLS-1$
    }
}
