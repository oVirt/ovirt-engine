package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class GlusterVolumeSnapshotStatusCell extends AbstractCell<GlusterVolumeSnapshotEntity> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public void render(Context context, GlusterVolumeSnapshotEntity snapshot, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no snapshot is provided:
        if (snapshot == null) {
            return;
        }

        // Find the image corresponding to the status of the brick:
        GlusterSnapshotStatus status = snapshot.getStatus();
        ImageResource statusImage = null;

        switch (status) {
        case ACTIVATED:
            statusImage = resources.upImage();
            break;
        case DEACTIVATED:
            statusImage = resources.downImage();
            break;
        case UNKNOWN:
            statusImage = resources.questionMarkImage();
            break;
        default:
            statusImage = resources.downImage();
        }

        // Generate the HTML for the image:
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());
        sb.append(templates.statusTemplate(statusImageHtml, id, status.toString()));
    }
}
