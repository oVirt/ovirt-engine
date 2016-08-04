package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.Disk;

import com.google.gwt.safehtml.shared.SafeHtml;

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
}
