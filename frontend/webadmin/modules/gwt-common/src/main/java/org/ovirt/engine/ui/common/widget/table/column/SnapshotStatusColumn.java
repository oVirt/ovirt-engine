package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class SnapshotStatusColumn extends AbstractImageResourceColumn<Snapshot> {

    private final static CommonApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(Snapshot snapshot) {
        setEnumTitle(snapshot.getStatus());

        switch (snapshot.getStatus()) {
        case OK:
            return resources.snapshotImage();
        case LOCKED:
            return resources.waitImage();
        case IN_PREVIEW:
            return resources.snapshotImage();
        default:
            return resources.snapshotImage();
        }
    }
}
