package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class BrickStatusCell extends AbstractCell<GlusterBrickEntity> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public void render(Context context, GlusterBrickEntity brick, SafeHtmlBuilder sb, String id) {

        // Nothing to render if no brick is provided:
        if (brick == null) {
            return;
        }

        // Find the image corresponding to the status of the brick:
        GlusterStatus status = brick.getStatus();
        ImageResource statusImage = null;

        switch (status) {
        case DOWN:
            statusImage = resources.downImage();
            break;
        case UP:
            statusImage = resources.upImage();
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
        if (brick.getUnSyncedEntries() != null && brick.getUnSyncedEntries() > 0) {
            SafeHtml alertImageHtml =
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.alertImage()).getHTML());
            sb.append(templates.statusWithAlertTemplate(statusImageHtml, alertImageHtml, id, status.toString()));
        } else {
            sb.append(templates.statusTemplate(statusImageHtml, id, status.toString()));
        }
    }

}
