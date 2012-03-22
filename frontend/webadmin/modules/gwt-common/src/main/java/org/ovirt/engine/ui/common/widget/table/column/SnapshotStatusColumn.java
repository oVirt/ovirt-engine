package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Snapshot;

import com.google.gwt.resources.client.ImageResource;


public class SnapshotStatusColumn extends ImageResourceColumn<Snapshot> {

    @Override
    public ImageResource getValue(Snapshot snapshot) {
        switch (snapshot.getStatus()) {
        case OK:
            return getCommonResources().snapshotImage();
        case LOCKED:
            return getCommonResources().waitImage();
        case IN_PREVIEW:
            return getCommonResources().snapshotImage();
        default:
            return getCommonResources().snapshotImage();
        }
    }
}
