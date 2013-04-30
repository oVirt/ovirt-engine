package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("deprecation")
public class ProviderModel extends Model {

    private static final String CMD_SAVE = "OnSave"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final ListModel sourceListModel;
    private final VdcActionType action;
    private final Provider provider;

    private EntityModel privateName;
    private EntityModel privateDescription;
    private EntityModel privateUrl;

    public EntityModel getName() {
        return privateName;
    }

    private void setName(EntityModel value) {
        privateName = value;
    }

    public EntityModel getDescription() {
        return privateDescription;
    }

    private void setDescription(EntityModel value) {
        privateDescription = value;
    }

    public EntityModel getUrl() {
        return privateUrl;
    }

    private void setUrl(EntityModel value) {
        privateUrl = value;
    }

    public ProviderModel(ListModel sourceListModel, VdcActionType action, Provider provider) {
        this.sourceListModel = sourceListModel;
        this.action = action;
        this.provider = provider;

        setName(new EntityModel(provider.getName()));
        setDescription(new EntityModel(provider.getDescription()));
        setUrl(new EntityModel(provider.getUrl()));

        UICommand tempVar = new UICommand(CMD_SAVE, this);
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand(CMD_CANCEL, this);
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getCommands().add(tempVar2);
    }

    private boolean validate() {
        return true;
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    private void onSave() {
        if (!validate()) {
            return;
        }

        provider.setName((String) privateName.getEntity());
        provider.setDescription((String) privateDescription.getEntity());
        provider.setUrl((String) privateUrl.getEntity());

        Frontend.RunAction(action, new ProviderParameters(provider));
        cancel();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), CMD_SAVE)) {
            onSave();
        } else if (StringHelper.stringsEqual(command.getName(), CMD_CANCEL)) {
            cancel();
        }
    }

}
