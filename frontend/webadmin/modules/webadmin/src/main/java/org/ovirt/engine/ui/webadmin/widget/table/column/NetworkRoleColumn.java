package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;

public class NetworkRoleColumn extends WebAdminImageResourceColumn<NetworkView> {

    private final ApplicationConstants applicationConstants = ClientGinjectorProvider.instance().getApplicationConstants();
    @Override
    public ImageResource getValue(NetworkView networkView) {

        if (networkView.getNetwork().isVmNetwork()){
            setTitle(applicationConstants.vmNetwork());
            return getApplicationResources().networkVm();
        }

        return null;
    }

}
