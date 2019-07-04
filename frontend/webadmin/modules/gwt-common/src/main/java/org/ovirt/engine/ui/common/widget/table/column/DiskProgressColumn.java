package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class DiskProgressColumn extends AbstractProgressBarColumn<Disk> {

    public DiskProgressColumn() {
    }

    @Override
    protected Integer getProgressValue(Disk disk) {
        return disk.getProgress();
    }

    @Override
    public SafeHtml getValue(Disk object) {
        return object.getProgress() != null ? super.getValue(object) : null;
    }

    @Override
    protected String getStyle() {
        return "engine-progress-box-migration"; //$NON-NLS-1$
    }

    @Override
    protected String getProgressText(Disk disk) {
        Integer p = getProgressValue(disk);
        return p != null ? p.toString() + "%" : "0%"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getColorByProgress(int progress) {
        return ProgressBarColors.GREEN.asCode();
    }

    @Override
    public SafeHtmlCell getCell() {
        return new SafeHtmlCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb, String id) {
                if (value != null) {
                    String divStyle = "display:table-cell;width:100%;padding:0px 4px;vertical-align:middle;"; //$NON-NLS-1$
                    sb.append(AssetProvider.getTemplates().divWithStringStyle(divStyle, id, value));
                }
            }
        };
    }
}
