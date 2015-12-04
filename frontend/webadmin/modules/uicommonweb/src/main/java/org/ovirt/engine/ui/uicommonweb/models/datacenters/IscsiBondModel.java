package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.AddIscsiBondParameters;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class IscsiBondModel extends Model {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private EntityModel<String> name;
    private EntityModel<String> description;
    private ListModel<Network> networks;
    private ListModel<StorageServerConnections> storageTargets;

    private IscsiBond iscsiBond;
    private StoragePool storagePool;
    private UICommand cancelCommand;

    public IscsiBondModel() {
        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setNetworks(new ListModel<Network>());
        setStorageTargets(new ListModel<StorageServerConnections>());
        createSaveButon();
    }

    @Override
    public void initialize() {
        if (getIscsiBond() != null) {
            getName().setEntity(getIscsiBond().getName());
            getDescription().setEntity(getIscsiBond().getDescription());
        }

        initializeNetworkList();
        initializeStorageTargetsList();
    }

    private void initializeNetworkList() {
        AsyncDataProvider.getInstance().getAllDataCenterNetworks(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                List<Network> selected = new ArrayList<>();
                List<Network> networks = (List<Network>) returnValue;
                Set<Guid> iscsiBonded = isBondExist() ?
                        new HashSet<>(getIscsiBond().getNetworkIds()) : Collections.<Guid>emptySet();

                for (Network network : networks) {
                    if (iscsiBonded.contains(network.getId())) {
                        selected.add(network);
                    }
                }

                IscsiBondModel model = (IscsiBondModel) target;
                model.getNetworks().setItems(networks);
                model.getNetworks().setSelectedItems(selected);
            }
        }), getStoragePool().getId());
    }

    private void initializeStorageTargetsList() {
        AsyncDataProvider.getInstance().getStorageConnectionsByDataCenterIdAndStorageType(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ArrayList<StorageServerConnections> selected = new ArrayList<>();
                ArrayList<StorageServerConnections> conns = (ArrayList<StorageServerConnections>) returnValue;
                Set<String> iscsiBonded = isBondExist() ?
                        new HashSet<>(getIscsiBond().getStorageConnectionIds()) : Collections.<String>emptySet();

                for (StorageServerConnections conn : conns) {
                    if (iscsiBonded.contains(conn.getId())) {
                        selected.add(conn);
                    }
                }

                IscsiBondModel model = (IscsiBondModel) target;
                model.getStorageTargets().setItems(conns);
                model.getStorageTargets().setSelectedItems(selected);
            }
        }), getStoragePool().getId(), StorageType.ISCSI);
    }

    @Override
    public void executeCommand(UICommand command) {
        if ("OnSave".equalsIgnoreCase(command.getName())) { //$NON-NLS-1$
            onSave();
        }
    }

    private void createSaveButon() {
        UICommand onSaveCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        onSaveCommand.setTitle(constants.ok());
        onSaveCommand.setIsDefault(true);
        getCommands().add(onSaveCommand);
    }

    private void onSave() {
        if (!validate()) {
            return;
        }

        if (isBondExist()) {
            updateBond();
        } else {
            addBond();
        }
    }

    private boolean isBondExist() {
        return getIscsiBond() != null;
    }

    private void addBond() {
        AddIscsiBondParameters params = new AddIscsiBondParameters(createIscsiBond());

        startProgress();
        Frontend.getInstance().runAction(VdcActionType.AddIscsiBond, params, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                IscsiBondModel model = (IscsiBondModel) result.getState();
                model.stopProgress();
                model.cancel();
            }
        }, this);
    }

    private void updateBond() {
        IscsiBond newIscsiBond = createIscsiBond();
        newIscsiBond.setId(getIscsiBond().getId());

        EditIscsiBondParameters params = new EditIscsiBondParameters(newIscsiBond);

        startProgress();
        Frontend.getInstance().runAction(VdcActionType.EditIscsiBond, params, new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                IscsiBondModel model = (IscsiBondModel) result.getState();
                model.stopProgress();
                model.cancel();
            }
        }, this);
    }

    private List<Guid> getSelectedNetworks() {
        List<Guid> selectedNetworks = new LinkedList<>();

        for (Network model : getNetworks().getSelectedItems()) {
            selectedNetworks.add(model.getId());
        }

        return selectedNetworks;
    }

    private List<String> getSelectedConnections() {
        List<String> selectedConnections = new ArrayList<>();

        for (StorageServerConnections conn : getStorageTargets().getSelectedItems()) {
            selectedConnections.add(conn.getId());
        }

        return selectedConnections;
    }

    private boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new I18NNameValidation(), new LengthValidation(50) });
        getDescription().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation(), new LengthValidation(4000) });

        if (getNetworks().getSelectedItems() == null || getNetworks().getSelectedItems().isEmpty()) {
            getInvalidityReasons().add(constants.noNetworksSelected());
            setIsValid(false);
        } else {
            setIsValid(true);
        }

        return getName().getIsValid() && getDescription().getIsValid() && getIsValid();
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public ListModel<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(ListModel<Network> networks) {
        this.networks = networks;
    }

    public ListModel<StorageServerConnections> getStorageTargets() {
        return storageTargets;
    }

    public void setStorageTargets(ListModel<StorageServerConnections> storageTargets) {
        this.storageTargets = storageTargets;
    }

    public StoragePool getStoragePool() {
        return storagePool;
    }

    public void setStoragePool(StoragePool sp) {
        storagePool = sp;
    }

    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
        getCommands().add(cancelCommand);
    }

    public IscsiBond getIscsiBond() {
        return iscsiBond;
    }

    public void setIscsiBond(IscsiBond iscsiBond) {
        this.iscsiBond = iscsiBond;
    }

    protected void cancel() {
        getCancelCommand().execute();
    }

    private IscsiBond createIscsiBond() {
        IscsiBond newIscsiBond = new IscsiBond();

        newIscsiBond.setStoragePoolId(getStoragePool().getId());
        newIscsiBond.setName(getName().getEntity());
        newIscsiBond.setDescription(getDescription().getEntity());
        newIscsiBond.setNetworkIds(getSelectedNetworks());
        newIscsiBond.setStorageConnectionIds(getSelectedConnections());

        return newIscsiBond;
    }
}
