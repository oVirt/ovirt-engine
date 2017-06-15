package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;


public class SnapshotStatusColumn extends AbstractImageResourceColumn<Snapshot> {

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(Snapshot snapshot) {

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

    @Override
    public SafeHtml getTooltip(Snapshot snapshot) {
        String status = EnumTranslator.getInstance().translate(snapshot.getStatus());
        return SafeHtmlUtils.fromString(status);
    }
}
