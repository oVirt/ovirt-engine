package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class DiskProfileListModel extends SearchableListModel
{
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;
    private Map<Guid, StorageQos> qosMap;

    public DiskProfileListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().diskProfilesTitle());
        setHelpTag(HelpTag.disk_profiles);
        setHashName("disk_profiles"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    public void newProfile() {
        if (getWindow() != null) {
            return;
        }

        NewDiskProfileModel model = new NewDiskProfileModel(this,
                getEntity().getStoragePoolId());
        setWindow(model);

        initProfileStorageDomains(model);
    }

    public void edit() {
        if (getWindow() != null) {
            return;
        }

        EditDiskProfileModel model =
                new EditDiskProfileModel(this, (DiskProfile) getSelectedItem(), getEntity().getStoragePoolId());
        setWindow(model);

        initProfileStorageDomains(model);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveDiskProfileModel model = new RemoveDiskProfileModel(this, getSelectedItems());
        setWindow(model);
    }

    private void initProfileStorageDomains(DiskProfileBaseModel model) {
        model.getStorageDomains().setItems(Arrays.<StorageDomain> asList(getEntity()));
        model.getStorageDomains().setSelectedItem(getEntity());
        model.getStorageDomains().setIsChangable(false);
    }

    public void cancel() {
        setWindow(null);
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
        Guid dcId = getEntity().getStoragePoolId();
        if (dcId == null) { // not attached to data center
            fetchDiskProfiles();
        } else {
        Frontend.getInstance().runQuery(VdcQueryType.GetAllQosByStoragePoolIdAndType,
                new QosQueryParameterBase(dcId, QosType.STORAGE),
                new AsyncQuery(new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<StorageQos> qosList =
                                (ArrayList<StorageQos>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        qosMap = new HashMap<Guid, StorageQos>();
                        if (qosList != null) {
                            for (StorageQos storageQos : qosList) {
                                qosMap.put(storageQos.getId(), storageQos);
                            }
                        }
                        fetchDiskProfiles();
                    }
                }));
        }
    }

    private void fetchDiskProfiles() {
        Frontend.getInstance().runQuery(VdcQueryType.GetDiskProfilesByStorageDomainId,
                new IdQueryParameters(DiskProfileListModel.this.getEntity().getId()),
                new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model1, Object returnValue1) {
                        DiskProfileListModel.this.setItems((List<DiskProfile>) ((VdcQueryReturnValue) returnValue1).getReturnValue());
                    }
                }));
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
        StorageDomain storageDomain = getEntity();

        getNewCommand().setIsExecutionAllowed(storageDomain != null);
        getEditCommand().setIsExecutionAllowed((getSelectedItems() != null && getSelectedItems().size() == 1));
        getRemoveCommand().setIsExecutionAllowed((getSelectedItems() != null && getSelectedItems().size() > 0));
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
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newProfile();
        }
        else if (command == getEditCommand()) {
            edit();
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
    }

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand value) {
        editCommand = value;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    @Override
    public StorageDomain getEntity() {
        return (StorageDomain) ((super.getEntity() instanceof StorageDomain) ? super.getEntity() : null);
    }

    public StorageQos getStorageQos(Guid qosId) {
        return qosMap.get(qosId);
    }

    @Override
    protected String getListName() {
        return "DiskProfileListModel"; //$NON-NLS-1$
    }

}
