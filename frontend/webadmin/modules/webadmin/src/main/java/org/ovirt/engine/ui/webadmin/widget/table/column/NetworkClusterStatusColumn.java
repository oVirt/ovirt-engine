package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.utils.PairQueryable;

import com.google.gwt.resources.client.ImageResource;

public class NetworkClusterStatusColumn extends WebAdminImageResourceColumn<PairQueryable<VDSGroup, network_cluster>>{

    private final NetworkStatusColumn networkStatusColumn = new NetworkStatusColumn();


    @Override
    public ImageResource getValue(PairQueryable<VDSGroup, network_cluster> object) {
        if (object.getSecond() != null){
            return networkStatusColumn.getValue(object.getSecond());
        }
     return null;
    }

}
