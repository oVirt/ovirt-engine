package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewCpuQosModel extends QosModel<CpuQos, CpuQosParametersModel> {

    public NewCpuQosModel(Model sourceModel, StoragePool dataCenter) {
        super(new CpuQos(), new CpuQosParametersModel(), sourceModel, dataCenter);
    }

    @Override
    protected ActionType getAction() {
        return ActionType.AddCpuQos;
    }

    @Override
    protected QosParametersBase<CpuQos> getParameters() {
        QosParametersBase<CpuQos> qosParametersBase = new QosParametersBase<>();
        qosParametersBase.setQos(getQos());
        return qosParametersBase;
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().newCpuQoSTitle();
    }

    @Override
    public HelpTag getHelpTag() {
        return HelpTag.new_cpu_qos;
    }

    @Override
    public String getHashName() {
        return "new_cpu_qos"; //$NON-NLS-1$;
    }

}
