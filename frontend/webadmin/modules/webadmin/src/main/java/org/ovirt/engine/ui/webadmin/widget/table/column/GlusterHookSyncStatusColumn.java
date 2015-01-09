package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;

import com.google.gwt.resources.client.ImageResource;


public class GlusterHookSyncStatusColumn extends AbstractWebAdminImageResourceColumn<GlusterHookEntity> {

    @Override
    public ImageResource getValue(GlusterHookEntity hookEntity) {
        if (hookEntity.hasConflicts()) {
            return getApplicationResources().alertImage();
        }
        return null;
    }
}
