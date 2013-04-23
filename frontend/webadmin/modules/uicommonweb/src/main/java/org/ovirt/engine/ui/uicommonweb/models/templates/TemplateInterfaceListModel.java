package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.EditTemplateInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewTemplateInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVmTemplateInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class TemplateInterfaceListModel extends SearchableListModel
{

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private VDSGroup cluster = null;
    private Boolean isLinkStateChangeable = null;

    // TODO: Check if we really need the following property.
    private VmTemplate getEntityStronglyTyped()
    {
        Object tempVar = getEntity();
        return (VmTemplate) ((tempVar instanceof VmTemplate) ? tempVar : null);
    }

    public TemplateInterfaceListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHashName("network_interfaces"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        getSearchCommand().Execute();
        UpdateActionAvailability();
    }

    @Override
    public void Search()
    {
        if (getEntityStronglyTyped() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch(VdcQueryType.GetTemplateInterfacesByTemplateId,
                new IdQueryParameters(getEntityStronglyTyped().getId()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetTemplateInterfacesByTemplateId,
                new IdQueryParameters(getEntityStronglyTyped().getId())));
        setItems(getAsyncResult().getData());
    }

    private void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        VmInterfaceModel model =
                NewTemplateInterfaceModel.createInstance(getEntityStronglyTyped(),
                        cluster.getcompatibility_version(),
                        (ArrayList<VmNetworkInterface>) getItems(),
                        this);
        setWindow(model);

    }


    private void Edit()
    {
        if (getWindow() != null)
        {
            return;
        }

        VmInterfaceModel model = EditTemplateInterfaceModel.createInstance(getEntityStronglyTyped(),
                cluster.getcompatibility_version(),
                (ArrayList<VmNetworkInterface>) getItems(),
                (VmNetworkInterface) getSelectedItem(), this);
        setWindow(model);
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        RemoveVmTemplateInterfaceModel model = new RemoveVmTemplateInterfaceModel(this, getSelectedItems(), false);
        setWindow(model);
    }

    private void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        getNewCommand().setIsExecutionAllowed(cluster != null);
        getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1
                && getSelectedItem() != null && cluster != null);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    public void setEntity(Object value) {
        cluster = null;
        super.setEntity(value);

        if (getEntity() != null) {
            AsyncDataProvider.GetClusterById(new AsyncQuery(this, new INewAsyncCallback() {

                @Override
                public void onSuccess(Object listModel, Object returnValue) {
                    cluster = (VDSGroup) returnValue;
                    isLinkStateChangeable =
                            (Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.NetworkLinkingSupported,
                                    cluster.getcompatibility_version().getValue());
                    UpdateActionAvailability();
                }
            }),
                    ((VmTemplate) getEntity()).getVdsGroupId());
        }
    }

    @Override
    protected String getListName() {
        return "TemplateInterfaceListModel"; //$NON-NLS-1$
    }
}
