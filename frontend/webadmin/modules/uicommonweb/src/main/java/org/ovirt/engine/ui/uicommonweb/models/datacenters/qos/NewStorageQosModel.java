package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewStorageQosModel extends NewQosModel<StorageQos, StorageQosParametersModel> {

    public NewStorageQosModel(Model sourceModel, StoragePool dataCenter) {
        super(sourceModel, dataCenter);
        init(new StorageQos());
    }

    @Override
    protected VdcActionType getVdcAction() {
        return VdcActionType.AddStorageQos;
    }

    @Override
    protected QosParametersBase<StorageQos> getParameters() {
        QosParametersBase<StorageQos> qosParametersBase = new QosParametersBase<StorageQos>();
        qosParametersBase.setQos(getQos());
        return qosParametersBase;
    }

    @Override
    public void init(StorageQos qos) {
        setQos(qos);
        setQosParametersModel(new StorageQosParametersModel());
        getQosParametersModel().init(qos);
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
