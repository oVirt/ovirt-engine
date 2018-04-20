package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;

public abstract class ProfileBaseModel<P extends ProfileBase, Q extends QosBase, R extends BusinessEntity<Guid>> extends Model {

    private EntityModel<String> name;
    private EntityModel<String> description;
    private final IModel sourceModel;
    private ListModel<R> parentListModel;
    private ListModel<Q> qos;
    private P profile;
    private final Guid defaultQosId;
    private final ActionType actionType;

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

    public ListModel<R> getParentListModel() {
        return parentListModel;
    }

    public void setParentListModel(ListModel<R> parentListModel) {
        this.parentListModel = parentListModel;
    }

    public ListModel<Q> getQos() {
        return qos;
    }

    public void setQos(ListModel<Q> qos) {
        this.qos = qos;
    }

    public P getProfile() {
        return profile;
    }

    public void setProfile(P profile) {
        this.profile = profile;
    }

    public IModel getSourceModel() {
        return sourceModel;
    }

    public Guid getDefaultQosId() {
        return defaultQosId;
    }

    public ProfileBaseModel(IModel sourceModel,
            Guid dcId,
            Guid defaultQosId,
            ActionType actionType) {
        this.sourceModel = sourceModel;
        this.defaultQosId = defaultQosId;
        this.actionType = actionType;

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setParentListModel(new ListModel<R>());
        setQos(new ListModel<Q>());

        initQosList(dcId);
        initCommands();
    }

    protected void initCommands() {
        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(cancelCommand);
    }

    private void onSave() {
        if (getProgress() != null) {
            return;
        }

        if (!validate()) {
            return;
        }

        // Save changes.
        flush();

        startProgress();

        Frontend.getInstance().runAction(actionType,
                getParameters(),
                result -> {
                    stopProgress();
                    cancel();
                },
                this);
    }

    protected abstract ProfileParametersBase<P> getParameters();

    public abstract void flush();

    private void cancel() {
        sourceModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) {//$NON-NLS-1$
            cancel();
        }
    }

    private void initQosList(Guid dataCenterId) {
        if (dataCenterId == null) {
            return;
        }

        Frontend.getInstance().runQuery(QueryType.GetAllQosByStoragePoolIdAndType,
                new QosQueryParameterBase(dataCenterId, getQosType()),
                new AsyncQuery<QueryReturnValue>(returnValue -> postInitQosList(returnValue == null ? new ArrayList<Q>()
                        : (List<Q>) returnValue.getReturnValue())));
    }

    protected abstract QosType getQosType();

    protected abstract void postInitQosList(List<Q> list);

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new SpecialAsciiI18NOrNoneValidation() });
        getDescription().validateEntity(new IValidation[] { new AsciiOrNoneValidation() });
        return getName().getIsValid();
    }

}
