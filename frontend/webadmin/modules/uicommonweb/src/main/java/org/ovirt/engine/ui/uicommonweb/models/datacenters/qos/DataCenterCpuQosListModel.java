package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DataCenterCpuQosListModel extends DataCenterQosListModel<CpuQos, CpuQosParametersModel> {
    @Override
    protected String getQosTitle() {
        return ConstantsManager.getInstance().getConstants().cpuQosTitle();
    }

    @Override
    protected String getQosHashName() {
        return "cpu_qos"; //$NON-NLS-1$
    }

    @Override
    protected HelpTag getQosHelpTag() {
        return HelpTag.cpu_qos;
    }

    @Override
    protected QosType getQosType() {
        return QosType.CPU;
    }

    @Override
    protected QosModel<CpuQos, CpuQosParametersModel> getNewQosModel() {
        return new NewCpuQosModel(this, getEntity());
    }

    @Override
    protected QosModel<CpuQos, CpuQosParametersModel> getEditQosModel(CpuQos qoS) {
        return new EditCpuQosModel(getSelectedItem(), this, getEntity());
    }

    @Override
    protected RemoveQosModel<CpuQos> getRemoveQosModel() {
        return new RemoveCpuQosModel(this);
    }

    @Override
    protected String getListName() {
        return "DataCenterCpuQosModel"; //$NON-NLS-1$
    }

}
