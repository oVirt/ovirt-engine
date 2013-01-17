package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class ProviderModel extends Model
{
    private EntityModel privateName;
    private EntityModel privateDescription;
    private EntityModel privateUrl;
    private final ProviderListModel sourceListModel;

    public ProviderModel(ProviderListModel sourceListModel)
    {
        this.sourceListModel = sourceListModel;

        setName(new EntityModel());
        setDescription(new EntityModel());
        setUrl(new EntityModel());

        setTitle(ConstantsManager.getInstance().getConstants().addProviderTitle());
        setHashName("add_provider"); //$NON-NLS-1$

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    private void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    public EntityModel getUrl()
    {
        return privateUrl;
    }

    private void setUrl(EntityModel value)
    {
        privateUrl = value;
    }

    public ListModel getSourceListModel() {
        return sourceListModel;
    }

    public boolean validate()
    {
        return true;
    }

    protected void postSaveAction(Guid networkGuid, boolean succeeded) {
        if (succeeded)
        {
            cancel();
        }
        stopProgress();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    public void onSave()
    {
        if (!validate())
        {
            return;
        }

        Provider provider = new Provider();
        provider.setName((String) privateName.getEntity());
        provider.setDescription((String) privateDescription.getEntity());
        provider.setUrl((String) privateUrl.getEntity());
        Frontend.RunAction(VdcActionType.AddProvider, new ProviderParameters(provider),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        sourceListModel.getSearchCommand().execute();
                    }
                });
        cancel();
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
    }
}
