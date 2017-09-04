package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import java.util.List;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public abstract class QosModel<T extends QosBase, P extends QosParametersModel<T>> extends Model {
    private T qos;
    private P qosParametersModel;
    private final Model sourceModel;
    private ListModel<StoragePool> dataCenters;
    private EntityModel<String> name;
    private EntityModel<String> description;

    protected QosModel(T qos, P qosParametersModel, Model sourceModel, StoragePool dataCenter) {
        this.sourceModel = sourceModel;

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setDataCenters(new ListModel<StoragePool>());
        getDataCenters().setSelectedItem(dataCenter);
        getDataCenters().setIsChangeable(false);

        setTitle(getTitle());
        setHelpTag(getHelpTag());
        setHashName(getHashName());

        addCommands();

        init(qos, qosParametersModel);
    }

    private void init(T qos, P qosParametersModel) {
        setQos(qos);
        getQos().setStoragePoolId(getDataCenters().getSelectedItem().getId());
        getName().setEntity(qos.getName());
        getDescription().setEntity(qos.getDescription());

        setQosParametersModel(qosParametersModel);
        getQosParametersModel().init(qos);
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getDescription().validateEntity(new IValidation[] { new AsciiOrNoneValidation() });
        getQosParametersModel().validate();
        setIsValid(getName().getIsValid() && getDescription().getIsValid() && getQosParametersModel().getIsValid());
        return getIsValid();
    }

    protected void addCommands() {
        getCommands().add(UICommand.createDefaultOkUiCommand("OnSave", this)); //$NON-NLS-1$
        getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    public StoragePool getSelectedDc() {
        return getDataCenters().getSelectedItem();
    }

    public T flush() {
        getQos().setName(getName().getEntity());
        getQos().setDescription(getDescription().getEntity());
        List<StoragePool> selectedDataCenters = getDataCenters().getSelectionModel().getSelectedObjects();
        if (!selectedDataCenters.isEmpty()) {
            getQos().setStoragePoolId(selectedDataCenters.get(0).getId());
        }
        getQosParametersModel().flush(getQos());
        return getQos();
    }

    protected void executeSave() {
        final QosParametersBase<T> parameters = getParameters();
        parameters.setQos(getQos());
        Frontend.getInstance().runAction(getAction(), parameters, result -> {
            ActionReturnValue retVal = result.getReturnValue();
            boolean succeeded = false;
            if (retVal != null && retVal.getSucceeded()) {
                succeeded = true;
                getQos().setId((Guid) retVal.getActionReturnValue());
            }
            postSaveAction(succeeded);
        });
    }

    protected abstract ActionType getAction();

    protected abstract QosParametersBase<T> getParameters();

    @Override
    public abstract String getTitle();

    public abstract HelpTag getHelpTag();

    @Override
    public abstract String getHashName();

    protected void cancel() {
        sourceModel.setWindow(null);
        sourceModel.setConfirmWindow(null);
    }

    public void onSave() {
        if (!validate()) {
            return;
        }

        // Save changes.
        flush();

        // Execute all the required commands (detach, attach, update) to save the updates
        executeSave();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    protected void postSaveAction(boolean succeeded) {
        if (succeeded) {
            cancel();
        }
        stopProgress();
    }

    public T getQos() {
        return qos;
    }

    public void setQos(T qos) {
        this.qos = qos;
    }

    public ListModel<StoragePool> getDataCenters() {
        return dataCenters;
    }

    public void setDataCenters(ListModel<StoragePool> dataCenters) {
        this.dataCenters = dataCenters;
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

    public P getQosParametersModel() {
        return qosParametersModel;
    }

    public void setQosParametersModel(P qosParametersModel) {
        this.qosParametersModel = qosParametersModel;
    }
}
