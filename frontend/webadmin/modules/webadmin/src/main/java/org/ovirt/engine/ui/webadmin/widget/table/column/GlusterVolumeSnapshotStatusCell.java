package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class GlusterVolumeSnapshotStatusCell extends AbstractCell<GlusterVolumeSnapshotEntity> {

    private static final ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    private static final ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    private static final ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    @Override
    public void render(Context context, GlusterVolumeSnapshotEntity snapshot, SafeHtmlBuilder sb) {
        // Nothing to render if no snapshot is provided:
        if (snapshot == null) {
            return;
        }

        // Find the image corresponding to the status of the brick:
        GlusterSnapshotStatus status = snapshot.getStatus();
        ImageResource statusImage = null;
        String tooltip;

        switch (status) {
        case ACTIVATED:
            statusImage = resources.upImage();
            tooltip = constants.up();
            break;
        case DEACTIVATED:
            statusImage = resources.downImage();
            tooltip = constants.down();
            break;
        case UNKNOWN:
            statusImage = resources.questionMarkImage();
            tooltip = constants.unknown();
            break;
        default:
            statusImage = resources.downImage();
            tooltip = constants.down();
        }

        // Generate the HTML for the image:
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());
        sb.append(applicationTemplates.statusTemplate(statusImageHtml, tooltip));
    }
}
