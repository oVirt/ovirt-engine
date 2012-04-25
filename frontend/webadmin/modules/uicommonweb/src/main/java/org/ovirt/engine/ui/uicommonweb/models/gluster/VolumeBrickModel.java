package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;

public class VolumeBrickModel extends Model {

    EntityModel replicaCount;
    EntityModel stripeCount;
    ListModel bricks;

    public VolumeBrickModel()
    {
        setReplicaCount(new EntityModel());
        getReplicaCount().setEntity(VolumeListModel.REPLICATE_COUNT_DEFAULT);
        getReplicaCount().setIsChangable(false);

        setStripeCount(new EntityModel());
        getStripeCount().setEntity(VolumeListModel.STRIPE_COUNT_DEFAULT);
        getStripeCount().setIsChangable(false);

        setBricks(new ListModel());
    }

    public EntityModel getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(EntityModel replicaCount) {
        this.replicaCount = replicaCount;
    }

    public EntityModel getStripeCount() {
        return stripeCount;
    }

    public void setStripeCount(EntityModel stripeCount) {
        this.stripeCount = stripeCount;
    }

    public ListModel getBricks() {
        return bricks;
    }

    public void setBricks(ListModel bricks) {
        this.bricks = bricks;
    }

    public boolean validateAddBricks(GlusterVolumeType selectedVolumeType)
    {
        boolean valid = true;
        valid = getBricks().getSelectedItems() != null && getBricks().getSelectedItems().size() > 0;
        return valid;
    }

}
