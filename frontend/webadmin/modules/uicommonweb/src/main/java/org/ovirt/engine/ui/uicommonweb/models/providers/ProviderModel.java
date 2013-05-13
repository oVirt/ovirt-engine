package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.Uri;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.UrlValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("deprecation")
public class ProviderModel extends Model {

    private static final String CMD_SAVE = "OnSave"; //$NON-NLS-1$
    private static final String CMD_CANCEL = "Cancel"; //$NON-NLS-1$

    private final ListModel sourceListModel;
    private final VdcActionType action;
    private final Provider provider;

    private EntityModel privateName;
    private ListModel privateType;
    private EntityModel privateDescription;
    private EntityModel privateUrl;

    public EntityModel getName() {
        return privateName;
    }

    private void setName(EntityModel value) {
        privateName = value;
    }

    public ListModel getType() {
        return privateType;
    }

    private void setType(ListModel value) {
        privateType = value;
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
        setType(new ListModel());
        setDescription(new EntityModel(provider.getDescription()));
        setUrl(new EntityModel(provider.getUrl()));

        List<ProviderType> allTypes = Arrays.asList(ProviderType.values());
        getType().setItems(allTypes);
        getType().setSelectedItem(provider.getType());

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
        getName().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        Uri url = new Uri((String) getUrl().getEntity());
        if (url.getScheme().isEmpty()) {
            url.setScheme(Uri.SCHEME_HTTP);
            getUrl().setEntity(url.toString());
        }
        getUrl().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new UrlValidation(new String[] { Uri.SCHEME_HTTP }) });

        return getName().getIsValid() && getType().getIsValid() && getUrl().getIsValid();
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    private void onSave() {
        if (!validate()) {
            return;
        }

        provider.setName((String) privateName.getEntity());
        provider.setType((ProviderType) privateType.getSelectedItem());
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
