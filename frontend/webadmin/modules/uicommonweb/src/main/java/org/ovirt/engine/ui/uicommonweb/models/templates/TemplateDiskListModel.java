package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaItemModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class TemplateDiskListModel extends SearchableListModel<VmTemplate, DiskImage> {
    private UICommand privateCopyCommand;

    public UICommand getCopyCommand() {
        return privateCopyCommand;
    }

    private void setCopyCommand(UICommand value) {
        privateCopyCommand = value;
    }

    private UICommand privateChangeQuotaCommand;

    public UICommand getChangeQuotaCommand() {
        return privateChangeQuotaCommand;
    }

    private void setChangeQuotaCommand(UICommand value) {
        privateChangeQuotaCommand = value;
    }

    protected boolean ignoreStorageDomains;

    private List<StorageDomain> storageDomains;

    public List<StorageDomain> getStorageDomains() {
        return storageDomains;
    }

    public void setStorageDomains(List<StorageDomain> storageDomains) {
        this.storageDomains = storageDomains;
    }

    public TemplateDiskListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setHelpTag(HelpTag.disks);
        setHashName("disks"); //$NON-NLS-1$

        setCopyCommand(new UICommand("Copy", this)); //$NON-NLS-1$
        setChangeQuotaCommand(new UICommand("changeQuota", this)); //$NON-NLS-1$
        getChangeQuotaCommand().setIsAvailable(false);

        updateActionAvailability();

        setStorageDomains(new ArrayList<StorageDomain>());
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }

        updateActionAvailability();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch(QueryType.GetVmTemplatesDisks,
                new IdQueryParameters(getEntity().getId()));
    }

    @Override
    public void setItems(final Collection value) {
        if (ignoreStorageDomains) {
            setDisks(value);
        } else {
            AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(
                    storageDomains -> {
                        Collections.sort(storageDomains, new NameableComparator());
                        setStorageDomains(storageDomains);
                        setDisks(value);
                    }));
        }

        updateActionAvailability();
    }

    private void setDisks(Collection<DiskImage> value) {
        ArrayList<DiskImage> disks = value != null ? new ArrayList<>(value) : new ArrayList<DiskImage>();

        Collections.sort(disks, new DiskByDiskAliasComparator());
        super.setItems(disks);
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
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            updateActionAvailability();
        }
    }

    private void updateActionAvailability() {
        getCopyCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && isCopyCommandAvailable());
    }

    private boolean isCopyCommandAvailable() {
        List<DiskImage> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<DiskImage>();

        for (DiskImage disk : disks) {
            if (disk.getImageStatus() != ImageStatus.OK || disk.getDiskStorageType() != DiskStorageType.IMAGE) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getCopyCommand()) {
            copy();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if (command == getChangeQuotaCommand()) {
            changeQuota();
        } else if (command.getName().equals("onChangeQuota")) { //$NON-NLS-1$
            onChangeQuota();
        }
    }

    private void copy() {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) getSelectedItems();

        if (disks == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        CopyDiskModel model = new CopyDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().copyDisksTitle());
        model.setHelpTag(HelpTag.copy_disk);
        model.setHashName("copy_disk"); //$NON-NLS-1$
        model.setEntity(this);
        model.init(disks);
        model.startProgress();
    }

    private void cancel() {
        setWindow(null);
    }

    @Override
    protected String getListName() {
        return "TemplateDiskListModel"; //$NON-NLS-1$
    }

    private void changeQuota() {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) getSelectedItems();

        if (disks == null || getWindow() != null) {
            return;
        }

        ChangeQuotaModel model = new ChangeQuotaModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignQuotaForDisk());
        model.setHelpTag(HelpTag.change_quota_disks);
        model.setHashName("change_quota_disks"); //$NON-NLS-1$
        model.startProgress();
        model.init(disks);

        model.getCommands().add(UICommand.createDefaultOkUiCommand("onChangeQuota", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onChangeQuota() {
        ChangeQuotaModel model = (ChangeQuotaModel) getWindow();
        ArrayList<ActionParametersBase> paramerterList = new ArrayList<>();

        for (Object item : model.getItems()) {
            ChangeQuotaItemModel itemModel = (ChangeQuotaItemModel) item;
            DiskImage disk = itemModel.getEntity();
            ActionParametersBase parameters =
                    new ChangeQuotaParameters(itemModel.getQuota().getSelectedItem().getId(),
                            disk.getId(),
                            itemModel.getStorageDomainId(),
                            disk.getStoragePoolId());
            paramerterList.add(parameters);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.ChangeQuotaForDisk, paramerterList,
                result -> cancel(),
                this);
    }

}
