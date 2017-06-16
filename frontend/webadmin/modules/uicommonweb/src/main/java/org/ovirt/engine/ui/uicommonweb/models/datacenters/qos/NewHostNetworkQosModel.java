package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewHostNetworkQosModel extends QosModel<HostNetworkQos, HostNetworkQosParametersModel> {

    public NewHostNetworkQosModel(Model sourceModel, StoragePool dataCenter) {
        super(new HostNetworkQos(), new SharedHostNetworkQosParametersModel(), sourceModel, dataCenter);
    }

    @Override
    protected ActionType getAction() {
        return ActionType.AddHostNetworkQos;
    }

    @Override
    protected QosParametersBase<HostNetworkQos> getParameters() {
        QosParametersBase<HostNetworkQos> parameters = new QosParametersBase<>();
        parameters.setQos(getQos());
        return parameters;
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().newHostNetworkQosTitle();
    }

    @Override
    public HelpTag getHelpTag() {
        return HelpTag.new_host_network_qos;
    }

    @Override
    public String getHashName() {
        return "new_host_network_qos"; //$NON-NLS-1$
    }

}
