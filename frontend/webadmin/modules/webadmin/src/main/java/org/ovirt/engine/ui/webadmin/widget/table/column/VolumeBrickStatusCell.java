package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.table.column.CellWithElementId;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeBrickStatusCell extends AbstractCell<GlusterVolumeEntity> implements CellWithElementId<GlusterVolumeEntity> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    private String elementIdPrefix;
    private String columnId;

    @Override
    public void render(Context context, GlusterVolumeEntity volume, SafeHtmlBuilder sb) {
        // Nothing to render if no volume is provided:
        if (volume == null) {
            return;
        }

        int upBricks = 0;
        int downBricks = 0;
        for (GlusterBrickEntity brick : volume.getBricks()) {
            if (brick.isOnline()) {
                upBricks++;
            }
            else {
                downBricks++;
            }
        }

        ImageResource upImage = resources.upImage();
        ImageResource downImage = resources.downImage();

        String id = ElementIdUtils.createTableCellElementId(getElementIdPrefix(), getColumnId(), context);

        // Generate the HTML for the images
        SafeHtml upImageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(upImage).getHTML());
        SafeHtml downImageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(downImage).getHTML());
        sb.append(applicationTemplates.volumeBrickStatusTemplate(upImageHtml, upBricks, downImageHtml, downBricks, id));
    }

    @Override
    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    @Override
    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public String getElementIdPrefix() {
        return elementIdPrefix;
    }

    @Override
    public String getColumnId() {
        return columnId;
    }
}
