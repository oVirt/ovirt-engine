package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LibvirtSecretModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class ProviderSecretListModel extends SearchableListModel<Provider, LibvirtSecret> {

    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;

    @Inject
    public ProviderSecretListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().providerNetworksTitle());

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
    }

    @Override
    protected String getListName() {
        return "ProviderSecretListModel"; //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        Provider provider = getEntity();
        if (provider == null) {
            return;
        }
        super.syncSearch(QueryType.GetAllLibvirtSecretsByProviderId, new IdQueryParameters(provider.getId()));
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newSecret();
        } else if (command == getEditCommand()) {
            editSecret();
        } else if (command == getRemoveCommand()) {
            removeSecret();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemoveSecret();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    private void newSecret() {
        if (getWindow() != null) {
            return;
        }
        final LibvirtSecretModel secretModel = createSecretModel(
                ConstantsManager.getInstance().getConstants().createSecretTitle(),
                HelpTag.create_secret, "create_secret"); //$NON-NLS-1$
        setWindow(secretModel);
        secretModel.getUuid().setEntity(Guid.newGuid().toString());
    }

    private void editSecret() {
        if (getWindow() != null) {
            return;
        }
        LibvirtSecretModel secretModel = createSecretModel(
                ConstantsManager.getInstance().getConstants().editSecretTitle(),
                HelpTag.edit_secret, "edit_secret"); //$NON-NLS-1$
        secretModel.getUuid().setIsChangeable(false);
        secretModel.edit(getSelectedItem());
        setWindow(secretModel);
    }

    private void removeSecret() {
        if (getWindow() != null) {
            return;
        }
        ConfirmationModel model = new ConfirmationModel();
        addDialogCommands(model, this, "OnRemove"); //$NON-NLS-1$
        setConfirmWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeSecretTitle());
        model.setHelpTag(HelpTag.remove_secret);
        model.setHashName("remove_secret"); //$NON-NLS-1$
        ArrayList<String> secretsToRemove = new ArrayList<>();
        for (LibvirtSecret libvirtSecret : getSelectedItems()) {
            secretsToRemove.add(libvirtSecret.getId().toString());
        }
        model.setItems(secretsToRemove);
    }

    private void onRemoveSecret() {
        ConfirmationModel model = (ConfirmationModel) getConfirmWindow();
        if (model.getProgress() != null) {
            return;
        }
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (LibvirtSecret libvirtSecret : getSelectedItems()) {
            LibvirtSecretParameters param = new LibvirtSecretParameters(libvirtSecret);
            parameters.add(param);
        }
        Frontend.getInstance().runMultipleAction(ActionType.RemoveLibvirtSecret, parameters,
                result -> cancel());
    }

    private LibvirtSecretModel createSecretModel(String title, HelpTag helpTag, String hashName) {
        LibvirtSecretModel secretModel = new LibvirtSecretModel();
        secretModel.setTitle(title);
        secretModel.setHelpTag(helpTag);
        secretModel.setHashName(hashName);
        secretModel.getProviderId().setEntity(getEntity().getId().toString());
        addDialogCommands(secretModel, secretModel, "OnSave"); //$NON-NLS-1$
        return secretModel;
    }

    private void addDialogCommands(Model model, ICommandTarget okTarget, String okCommandName) {
        model.getCommands().add(UICommand.createDefaultOkUiCommand(okCommandName, okTarget));
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
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

    private void updateActionAvailability() {
        List<LibvirtSecret> secrets = getSelectedItems() != null ? getSelectedItems() : new ArrayList<>();

        getEditCommand().setIsExecutionAllowed(secrets.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(secrets.size() > 0);
    }

    private void cancel() {
        setWindow(null);
        setConfirmWindow(null);
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    public void setNewCommand(UICommand newCommand) {
        this.newCommand = newCommand;
    }

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    public void setEditCommand(UICommand editCommand) {
        this.editCommand = editCommand;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    public void setRemoveCommand(UICommand removeCommand) {
        this.removeCommand = removeCommand;
    }
}
