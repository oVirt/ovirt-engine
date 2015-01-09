package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
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

public class BrickStatusCell extends AbstractCell<GlusterBrickEntity> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    @Override
    public void render(Context context, GlusterBrickEntity brick, SafeHtmlBuilder sb) {
        // Nothing to render if no brick is provided:
        if (brick == null) {
            return;
        }

        // Find the image corresponding to the status of the brick:
        GlusterStatus status = brick.getStatus();
        ImageResource statusImage = null;
        String tooltip;

        switch (status) {
        case DOWN:
            statusImage = resources.downImage();
            tooltip = constants.down();
            break;
        case UP:
            statusImage = resources.upImage();
            tooltip = constants.up();
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
