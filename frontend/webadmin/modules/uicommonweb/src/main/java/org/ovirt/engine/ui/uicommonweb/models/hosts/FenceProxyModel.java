package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class FenceProxyModel extends EntityModel<FenceProxySourceType> {
    private static final String OK = "Ok"; //$NON-NLS-1$
    private static final String CANCEL = "Cancel"; //$NON-NLS-1$

    final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private ListModel<FenceProxyModel> currentProxies;

    private ListModel<FenceProxyModel> availableProxies;

    private boolean initialized;

    public FenceProxyModel() {
        availableProxies = new ListModel<>();
    }

    private FenceProxyModel(FenceProxyModel model) {
        setEntity(model.getEntity());
    }

    /**
     * Edit the model.
     */
    public void edit(ListModel<FenceProxyModel> currentProxies) {
        if (getWindow() != null) {
            return;
        }
        this.currentProxies = currentProxies;
        FenceProxyModel newModel = new FenceProxyModel();
        newModel.setCurrentProxies(deepCopy(currentProxies));
        setWindow(newModel);
        newModel.setTitle(constants.selectFenceProxy());
        if (!newModel.getAvailableProxies().getItems().isEmpty()) {
            newModel.getCommands().add(UICommand.createDefaultOkUiCommand(OK, this));
        }
        newModel.getCommands().add(UICommand.createDefaultCancelUiCommand(CANCEL, this));
    }

    private ListModel<FenceProxyModel> deepCopy(ListModel<FenceProxyModel> proxyListModel) {
        List<FenceProxyModel> proxyModelCopies = new ArrayList<>();
        for (FenceProxyModel proxyModel : proxyListModel.getItems()) {
            proxyModelCopies.add(new FenceProxyModel(proxyModel));
        }

        ListModel<FenceProxyModel> result = new ListModel<>();
        result.setItems(proxyModelCopies);
        return result;
    }

    private void setCurrentProxies(ListModel<FenceProxyModel> currentProxies) {
        this.currentProxies = currentProxies;

        // Determine the already selected proxy types.
        List<FenceProxySourceType> currentSourceTypes = new ArrayList<>();
        for (FenceProxyModel currentProxyModel : currentProxies.getItems()) {
            if (currentProxyModel.getEntity() != null) {
                currentSourceTypes.add(currentProxyModel.getEntity());
            }
        }
        this.availableProxies.setItems(null);

        // Determine the available proxy types.
        List<FenceProxyModel> availableProxiesList = new ArrayList<>();
        for(FenceProxySourceType type : FenceProxySourceType.values()) {
            if (!currentSourceTypes.contains(type)) {
                FenceProxyModel newModel = new FenceProxyModel();
                newModel.setEntity(type);
                availableProxiesList.add(newModel);
            }
        }
        this.availableProxies.setItems(availableProxiesList);
    }

    public ListModel<FenceProxyModel> getCurrentProxies() {
        return this.currentProxies;
    }

    public ListModel<FenceProxyModel> getAvailableProxies() {
        return this.availableProxies;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        initialized = true;
    }

    @Override
    public void executeCommand(UICommand command) {
        if (OK.equals(command.getName())) {
            onOk();
        } else if (CANCEL.equals(command.getName())) {
            if (getWindow() != null) {
                cancel();
            } else if (getConfirmWindow() != null) {
                cancelConfirmation();
            }
        } else {
            super.executeCommand(command);
        }
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    /**
     * Action to take when user clicked OK in the pop-up.
     */
    private void onOk() {
        FenceProxyModel windowModel = (FenceProxyModel) getWindow();
        FenceProxyModel selectedModel = windowModel.getAvailableProxies().getSelectedItem();
        setEntity(selectedModel.getEntity());
        setWindow(null);
        List<FenceProxyModel> currentModels = currentProxies.getItemsAsList();
        currentProxies.setItems(Collections.emptyList());
        currentProxies.setItems(currentModels);
    }

    /**
     * Action to take when user clicked cancel in the pop-up.
     */
    private void cancel() {
        setWindow(null);
        List<FenceProxyModel> currentModels = currentProxies.getItemsAsList();
        currentModels.remove(this);
        currentProxies.setItems(Collections.emptyList());
        currentProxies.setItems(currentModels);
    }

    public void warnUserOnLimit() {
        if (getWindow() != null) {
            return;
        }
        ConfirmationModel model = new ConfirmationModel();
        setConfirmWindow(model);
        model.setTitle(constants.unableToRemoveTitle());
        model.setMessage(constants.unableToRemove());
        model.getLatch().setIsAvailable(true);
        model.getLatch().setIsChangeable(true);

        model.getCommands().add(UICommand.createCancelUiCommand(CANCEL, this));
    }
}
