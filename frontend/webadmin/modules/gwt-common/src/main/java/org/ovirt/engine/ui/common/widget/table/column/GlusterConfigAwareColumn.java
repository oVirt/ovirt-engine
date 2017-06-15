package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.widget.table.cell.GlusterConfigAwareCell;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.user.cellview.client.Column;

public class GlusterConfigAwareColumn extends Column<EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>>, GlusterGeoRepSessionConfiguration> {

    public GlusterConfigAwareColumn() {
        super(new GlusterConfigAwareCell());
    }

    @Override
    public GlusterGeoRepSessionConfiguration getValue(EntityModel<Pair<Boolean, GlusterGeoRepSessionConfiguration>> object) {
        return object.getEntity().getSecond();
    }
}
