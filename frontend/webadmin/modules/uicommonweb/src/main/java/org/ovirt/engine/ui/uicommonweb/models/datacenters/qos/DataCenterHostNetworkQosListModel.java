package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DataCenterHostNetworkQosListModel extends DataCenterQosListModel<HostNetworkQos, HostNetworkQosParametersModel> {

    @Override
    protected String getQosTitle() {
        return ConstantsManager.getInstance().getConstants().hostNetworkQosTitle();
    }

    @Override
    protected String getQosHashName() {
        return "host_network_qos"; //$NON-NLS-1$
    }

    @Override
    protected HelpTag getQosHelpTag() {
        return HelpTag.host_network_qos;
    }

    @Override
    protected QosType getQosType() {
        return QosType.HOSTNETWORK;
    }

    @Override
    protected QosModel<HostNetworkQos, HostNetworkQosParametersModel> getNewQosModel() {
        return new NewHostNetworkQosModel(this, getEntity());
    }

    @Override
    protected QosModel<HostNetworkQos, HostNetworkQosParametersModel> getEditQosModel(HostNetworkQos qoS) {
        return new EditHostNetworkQosModel(getSelectedItem(), this, getEntity());
    }

    @Override
    protected RemoveQosModel<HostNetworkQos> getRemoveQosModel() {
        return new RemoveHostNetworkQosModel(this);
    }

    @Override
    protected String getListName() {
        return "DataCenterHostNetworkQosModel"; //$NON-NLS-1$
    }

}
