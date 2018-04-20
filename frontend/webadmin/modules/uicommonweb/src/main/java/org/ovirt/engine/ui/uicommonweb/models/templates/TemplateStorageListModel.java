package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class TemplateStorageListModel extends SearchableListModel<VmTemplate, StorageDomainModel> {

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    List<StorageDomainModel> storageDomainModels;

    public TemplateStorageListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().storageTitle());
        setHelpTag(HelpTag.storage);
        setHashName("storage"); //$NON-NLS-1$

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
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
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch();

        VmTemplate template = getEntity();
        super.syncSearch(QueryType.GetStorageDomainsByVmTemplateId,
                new IdQueryParameters(template.getId()));
    }

    @Override
    public void setItems(final Collection<StorageDomainModel> value) {
        if (storageDomainModels != null) {
            Collections.sort(storageDomainModels, Comparator.comparing(s -> s.getStorageDomain().getStorageName()));
            itemsChanging(value, items);
            items = storageDomainModels;
            itemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
            storageDomainModels = null;
        } else {
            VmTemplate template = getEntity();
            AsyncDataProvider.getInstance().getTemplateDiskList(new AsyncQuery<>(
                            diskImages -> {

                                Collection<? extends Object> storageDomains = value;
                                List<StorageDomainModel> storageDomainModels = new ArrayList<>();

                                for (Object o : storageDomains) {
                                    StorageDomain storageDomain = (StorageDomain) o;
                                    StorageDomainModel storageDomainModel = new StorageDomainModel();
                                    storageDomainModel.setStorageDomain(storageDomain);

                                    ArrayList<DiskImage> disks = new ArrayList<>();
                                    for (DiskImage diskImage : diskImages) {
                                        if (diskImage.getStorageIds().contains(storageDomain.getId())) {
                                            disks.add(diskImage);
                                        }
                                    }

                                    Collections.sort(disks, new DiskByDiskAliasComparator());
                                    storageDomainModel.setDisks(disks);
                                    storageDomainModels.add(storageDomainModel);
                                }

                                TemplateStorageListModel.this.storageDomainModels = storageDomainModels;
                                setItems(value);
                            }),
                    template.getId());
        }
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeTemplateDisksTitle());
        model.setHelpTag(HelpTag.remove_template_disks);
        model.setHashName("remove_template_disks"); //$NON-NLS-1$

        List<? extends Object> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<DiskModel>();
        List<String> items = new ArrayList<>();
        for (Object o : disks) {
            DiskModel diskModel = (DiskModel) o;
            items.add(ConstantsManager.getInstance().getMessages().templateDiskDescription(
                    diskModel.getDisk().getDiskAlias(),
                    diskModel.getStorageDomain().getSelectedItem().getStorageName()));
        }
        model.setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();
        List<ActionParametersBase> parameters = new ArrayList<>();
        List<? extends Object> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<DiskModel>();

        for (Object o: disks) {
            DiskModel diskModel = (DiskModel) o;
            RemoveDiskParameters params =
                    new RemoveDiskParameters(diskModel.getDisk().getId(),
                                             diskModel.getStorageDomain().getSelectedItem().getId());
            parameters.add(params);
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveDisk, parameters,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);

        cancel();
    }

    private void cancel() {
        setWindow(null);
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
        getRemoveCommand().setIsExecutionAllowed(isRemoveCommandAvailable());
    }

    private boolean isRemoveCommandAvailable() {
        List<? extends Object> disks = getSelectedItems() != null ? getSelectedItems() : new ArrayList<DiskModel>();

        if (disks.isEmpty()) {
            return false;
        }

        for (Object o: disks) {
            DiskModel disk = (DiskModel) o;
            if (((DiskImage) disk.getDisk()).getImageStatus() == ImageStatus.LOCKED
                    || ((DiskImage) disk.getDisk()).getStorageIds().size() == 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            remove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateStorageListModel"; //$NON-NLS-1$
    }
}
