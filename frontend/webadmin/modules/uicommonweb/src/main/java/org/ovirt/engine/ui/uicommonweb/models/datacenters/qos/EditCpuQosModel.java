package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditCpuQosModel extends EditQosModel<CpuQos, CpuQosParametersModel> {

    public EditCpuQosModel(CpuQos qos, Model sourceModel, StoragePool dataCenter) {
        super(qos, sourceModel, dataCenter);
    }

    @Override
    protected VdcActionType getVdcAction() {
        return VdcActionType.UpdateCpuQos;
    }

    @Override
    protected QosParametersBase<CpuQos> getParameters() {
        QosParametersBase<CpuQos> qosParametersBase = new QosParametersBase<CpuQos>();
        qosParametersBase.setQos(getQos());
        qosParametersBase.setQosId(getQos().getId());
        return qosParametersBase;
    }

    @Override
    public void init(CpuQos qos) {
        super.init(qos);

        setQosParametersModel(new CpuQosParametersModel());
        getQosParametersModel().init(qos);
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().editStorageQoSTitle();
    }

    @Override
    public HelpTag getHelpTag() {
        return HelpTag.edit_cpu_qos;
    }

    @Override
    public String getHashName() {
        return "edit_cpu_qos"; //$NON-NLS-1$
    }

}

