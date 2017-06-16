package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewStorageQosModel extends QosModel<StorageQos, StorageQosParametersModel> {

    public NewStorageQosModel(Model sourceModel, StoragePool dataCenter) {
        super(new StorageQos(), new StorageQosParametersModel(), sourceModel, dataCenter);
    }

    @Override
    protected ActionType getAction() {
        return ActionType.AddStorageQos;
    }

    @Override
    protected QosParametersBase<StorageQos> getParameters() {
        QosParametersBase<StorageQos> qosParametersBase = new QosParametersBase<>();
        qosParametersBase.setQos(getQos());
        return qosParametersBase;
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().newStorageQoSTitle();
    }

    @Override
    public HelpTag getHelpTag() {
        return HelpTag.new_storage_qos;
    }

    @Override
    public String getHashName() {
        return "new_storage_qos"; //$NON-NLS-1$;
    }

}
