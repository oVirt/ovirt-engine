package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeBrickStatusCell extends AbstractCell<GlusterVolumeEntity> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public void render(Context context, GlusterVolumeEntity volume, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no volume is provided:
        if (volume == null) {
            return;
        }

        int upBricks = 0;
        int downBricks = 0;
        for (GlusterBrickEntity brick : volume.getBricks()) {
            if (brick.isOnline()) {
                upBricks++;
            } else {
                downBricks++;
            }
        }

        ImageResource upImage = resources.upImage();
        ImageResource downImage = resources.downImage();

        // Generate the HTML for the images
        SafeHtml upImageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(upImage).getHTML());
        SafeHtml downImageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(downImage).getHTML());
        sb.append(templates.volumeBrickStatusTemplate(upImageHtml, upBricks, downImageHtml, downBricks, id));
    }
}
