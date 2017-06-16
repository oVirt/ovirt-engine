package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditHostNetworkQosModel extends QosModel<HostNetworkQos, HostNetworkQosParametersModel> {

    public EditHostNetworkQosModel(HostNetworkQos qos, Model sourceModel, StoragePool dataCenter) {
        super(qos, new SharedHostNetworkQosParametersModel(), sourceModel, dataCenter);
    }

    @Override
    protected ActionType getAction() {
        return ActionType.UpdateHostNetworkQos;
    }

    @Override
    protected QosParametersBase<HostNetworkQos> getParameters() {
        QosParametersBase<HostNetworkQos> parameters = new QosParametersBase<>();
        parameters.setQos(getQos());
        return parameters;
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().editHostNetworkQosTitle();
    }

    @Override
    public HelpTag getHelpTag() {
        return HelpTag.edit_host_network_qos;
    }

    @Override
    public String getHashName() {
        return "edit_host_network_qos"; //$NON-NLS-1$
    }

}
