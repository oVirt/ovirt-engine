package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils.VolumeStatus;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeStatusCell extends AbstractCell<GlusterVolumeEntity> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    protected ImageResource downImage = resources.downImage();
    protected ImageResource upImage = resources.upImage();
    protected ImageResource allBricksDownImage = resources.volumeAllBricksDownWarning();
    protected ImageResource volumeSomeBricksDownImage = resources.volumeBricksDownWarning();

    protected ImageResource getStatusImage(VolumeStatus vStatus) {
     // Find the image corresponding to the status of the volume:
        ImageResource statusImage = null;

        switch (vStatus) {
        case DOWN:
            return downImage;
        case UP :
            return upImage;
        case ALL_BRICKS_DOWN :
            return allBricksDownImage;
        case SOME_BRICKS_DOWN :
            return volumeSomeBricksDownImage;
        }
        return statusImage;
    }

    private String getToolTip(VolumeStatus status) {
        switch (status) {
        case DOWN:
            return constants.down();
        case UP:
            return constants.up();
        case SOME_BRICKS_DOWN:
            return constants.volumeBricksDown();
        case ALL_BRICKS_DOWN:
            return constants.volumeAllBricksDown();
        default:
            return constants.down();
        }
    }

    @Override
    public void render(Context context, GlusterVolumeEntity volume, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no volume is provided:
        if (volume == null) {
            return;
        }
        VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);
        ImageResource statusImage = getStatusImage(status);
        String tooltip = getToolTip(status);
        // Generate the HTML for the image:
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());
        sb.append(applicationTemplates.statusTemplate(statusImageHtml, tooltip, id));
    }
}
