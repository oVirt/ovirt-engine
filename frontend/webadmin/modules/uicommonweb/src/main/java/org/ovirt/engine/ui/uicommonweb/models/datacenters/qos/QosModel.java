package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class QosModel<T extends QosBase, P extends QosParametersModel<T>> extends Model {
    private T qos;
    private P qosParametersModel;
    private final Model sourceModel;
    private ListModel<StoragePool> dataCenters;
    private EntityModel<String> name;
    private EntityModel<String> description;

    public QosModel(Model sourceModel, StoragePool dataCenter) {
        this.sourceModel = sourceModel;

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setDataCenters(new ListModel<StoragePool>());
        getDataCenters().setSelectedItem(dataCenter);
        getDataCenters().setIsChangable(false);

        setTitle(getTitle());
        setHelpTag(getHelpTag());
        setHashName(getHashName());

        addCommands();
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getDescription().validateEntity(new IValidation[] { new AsciiOrNoneValidation() });
        setIsValid(getIsValid() && getName().getIsValid() && getDescription().getIsValid());
        return getIsValid();
    }

    protected void addCommands() {
        getCommands().add(new UICommand("OnSave", this).setTitle(ConstantsManager.getInstance().getConstants().ok()) //$NON-NLS-1$
                .setIsDefault(true));
        getCommands().add(new UICommand("Cancel", this).setTitle(ConstantsManager.getInstance().getConstants().cancel()) //$NON-NLS-1$
                .setIsCancel(true));
    }

    public StoragePool getSelectedDc() {
        return getDataCenters().getSelectedItem();
    }

    public T flush() {
        getQos().setName(getName().getEntity());
        getQos().setDescription(getDescription().getEntity());
        getQos().setStoragePoolId(getDataCenters().getSelectedItem().getId());
        getQosParametersModel().flush(getQos());
        return getQos();
    }

    protected abstract void executeSave();

    public abstract void init(T qos);

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
