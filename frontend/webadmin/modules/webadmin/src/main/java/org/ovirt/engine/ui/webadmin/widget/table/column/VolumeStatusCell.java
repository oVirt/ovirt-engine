package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
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

public class VolumeStatusCell extends AbstractCell<GlusterVolumeEntity> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    @Override
    public void render(Context context, GlusterVolumeEntity volume, SafeHtmlBuilder sb) {
        // Nothing to render if no volume is provided:
        if (volume == null) {
            return;
        }
        int brickCount = volume.getBricks().size();
        int count = 0;

        // Find the image corresponding to the status of the volume:
        GlusterStatus status = volume.getStatus();
        ImageResource statusImage = null;
        String tooltip;

        switch (status) {
        case DOWN:
            statusImage = resources.downImage();
            tooltip = constants.down();
            break;
        case UP:
            count = countDownBricks(volume);
            if (count == 0) {
                statusImage = resources.upImage();
                tooltip = constants.up();
            } else if (count < brickCount) {
                statusImage = resources.volumeBricksDownWarning();
                tooltip = constants.volumeBricksDown();
            } else {
                statusImage = resources.volumeAllBricksDownWarning();
                tooltip = constants.volumeAllBricksDown();
            }
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

    public int countDownBricks(GlusterVolumeEntity volume) {
        int downCount = 0;
        int upCount = 0;
        for (GlusterBrickEntity brick : volume.getBricks()) {
            if (brick.getStatus() == GlusterStatus.UP) {
                upCount++;
            } else {
                downCount++;
            }
            if (upCount > 0 && downCount > 0) {
                return downCount;
            }
        }
        return downCount;
    }
}
