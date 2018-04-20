package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.EditTemplateInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewTemplateInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVmTemplateInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class TemplateInterfaceListModel extends SearchableListModel<VmTemplate, VmNetworkInterface> {

    private UICommand privateNewCommand;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    public TemplateInterfaceListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHelpTag(HelpTag.network_interfaces);
        setHashName("network_interfaces"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
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

        super.syncSearch(QueryType.GetTemplateInterfacesByTemplateId,
                new IdQueryParameters(getEntity().getId()));
    }

    private void newEntity() {
        if (getWindow() != null) {
            return;
        }

        VmInterfaceModel model =
                NewTemplateInterfaceModel.createInstance(getEntity(),
                        getEntity().getStoragePoolId(),
                        getEntity().getCompatibilityVersion(),
                        (ArrayList<VmNetworkInterface>) getItems(),
                        this);
        setWindow(model);

    }


    private void edit() {
        if (getWindow() != null) {
            return;
        }

        VmInterfaceModel model =
                EditTemplateInterfaceModel.createInstance(getEntity(),
                        getEntity().getStoragePoolId(),
                        getEntity().getCompatibilityVersion(),
                        (ArrayList<VmNetworkInterface>) getItems(),
                        getSelectedItem(),
                        this);
        setWindow(model);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        RemoveVmTemplateInterfaceModel model = new RemoveVmTemplateInterfaceModel(this, getSelectedItems(), false);
        setWindow(model);
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
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getNewCommand().setIsExecutionAllowed(getEntity() != null);
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1
                && getSelectedItem() != null);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "TemplateInterfaceListModel"; //$NON-NLS-1$
    }
}
