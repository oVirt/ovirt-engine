package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class VolumeCapacityCell extends AbstractCell<GlusterVolumeEntity> {

    private final static ApplicationTemplates templates = AssetProvider.getTemplates();

    @Override
    public void render(Context context, GlusterVolumeEntity object, SafeHtmlBuilder sb, String id) {
        GlusterVolumeSizeInfo sizeInfo = null;
        int progress;
        if (! (object.getAdvancedDetails() == null || object.getAdvancedDetails().getCapacityInfo() == null)) {
            sizeInfo = object.getAdvancedDetails().getCapacityInfo();
            progress = getProgressValue(sizeInfo);
        } else {
            progress = 0;
        }
        String sizeString = getProgressText(sizeInfo);
        String color = progress < 70 ? "#669966" : progress < 95 ? "#FF9900" : "#FF0000"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SafeHtml safeHtml = templates.progressBar(progress, sizeString, color, "engine-progress-box", id); //$NON-NLS-1$
        sb.append(safeHtml);
    }

    private String getProgressText(GlusterVolumeSizeInfo sizeStats) {
        if(sizeStats == null) {
            return "?";//$NON-NLS-1$
        } else {
            return ((int)((sizeStats.getUsedSize().floatValue() / sizeStats.getTotalSize().floatValue()) * 100)) + "%";//$NON-NLS-1$
        }
    }

    private Integer getProgressValue(GlusterVolumeSizeInfo sizeStats) {
        return (int)(Math.round((sizeStats.getUsedSize().floatValue() / sizeStats.getTotalSize().floatValue()) * 100));
    }
}
