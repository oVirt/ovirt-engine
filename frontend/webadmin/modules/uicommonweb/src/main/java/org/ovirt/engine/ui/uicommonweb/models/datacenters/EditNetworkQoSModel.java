package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditNetworkQoSModel extends NetworkQoSModel {

    public EditNetworkQoSModel(NetworkQoS networkQoS, Model sourceModel, StoragePool dataCenter) {
        super(sourceModel, dataCenter);
        setTitle(ConstantsManager.getInstance().getConstants().editNetworkQoSTitle());
        setHelpTag(HelpTag.edit_network_qos);
        setHashName("edit_network_qos"); //$NON-NLS-1$
        getName().setEntity(networkQoS.getName());
        init(networkQoS);
    }

    @Override
    public void executeSave() {
        QosParametersBase<NetworkQoS> parameters = new QosParametersBase<>();
        parameters.setQos(networkQoS);
        Frontend.getInstance().runAction(VdcActionType.UpdateNetworkQoS, parameters, result -> {
            VdcReturnValueBase retVal = result.getReturnValue();
            boolean succeeded = false;
            if (retVal != null && retVal.getSucceeded()) {
                succeeded = true;
            }
            postSaveAction(succeeded);
        });
    }
}
