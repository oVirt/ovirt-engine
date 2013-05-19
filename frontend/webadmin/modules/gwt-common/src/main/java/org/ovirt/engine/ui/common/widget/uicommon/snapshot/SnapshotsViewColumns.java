package org.ovirt.engine.ui.common.widget.uicommon.snapshot;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class SnapshotsViewColumns {
    private static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);
    private static final CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);

    public static TextColumnWithTooltip<Snapshot> dateColumn = new TextColumnWithTooltip<Snapshot>() {
        @Override
        public String getValue(Snapshot snapshot) {
            if (snapshot.getType() == SnapshotType.ACTIVE) {
                return constants.currentSnapshotLabel();
            }
            return FullDateTimeRenderer.getLocalizedDateTimeFormat().format(snapshot.getCreationDate());
        }
    };

    public static TextColumnWithTooltip<Snapshot> statusColumn = new EnumColumn<Snapshot, SnapshotStatus>() {
        @Override
        protected SnapshotStatus getRawValue(Snapshot snapshot) {
            return snapshot.getStatus();
        }
    };

    public static CheckboxColumn<Snapshot> memoryColumn = new CheckboxColumn<Snapshot>() {
        @Override
        public Boolean getValue(Snapshot object) {
            return !object.getMemoryVolume().isEmpty();
        }

        @Override
        protected boolean canEdit(Snapshot object) {
            return false;
        }
    };

    public static SafeHtmlColumn<Snapshot> descriptionColumn = new SafeHtmlColumn<Snapshot>() {
        @Override
        public final SafeHtml getValue(Snapshot snapshot) {
            // Get raw description string (ignore < and > characters).
            // Customize description style as needed.
            SafeHtml description = SafeHtmlUtils.fromString(snapshot.getDescription());
            String descriptionStr = description.asString();

            if (snapshot.getStatus() == SnapshotStatus.IN_PREVIEW) {
                descriptionStr = descriptionStr + " (" + constants.previewModelLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                description = templates.snapshotDescription("color:orange", descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.STATELESS) {
                descriptionStr = descriptionStr + " (" + constants.readonlyLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                description = templates.snapshotDescription("font-style:italic", descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.ACTIVE || snapshot.getType() == SnapshotType.PREVIEW) {
                description = templates.snapshotDescription("color:gray", descriptionStr); //$NON-NLS-1$
            }

            return description;
        }
    };
}
