package org.ovirt.engine.ui.common.widget.uicommon.snapshot;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnapshotsViewColumns {
    private static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);
    private static final CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);
    private static final CommonApplicationMessages messages = GWT.create(CommonApplicationMessages.class);

    public static final AbstractTextColumnWithTooltip<Snapshot> dateColumn = new AbstractTextColumnWithTooltip<Snapshot>() {
        @Override
        public String getValue(Snapshot snapshot) {
            if (snapshot.getType() == SnapshotType.ACTIVE) {
                return constants.currentSnapshotLabel();
            }
            return FullDateTimeRenderer.getLocalizedDateTimeFormat().format(snapshot.getCreationDate());
        }
    };

    public static final AbstractTextColumnWithTooltip<Snapshot> statusColumn = new AbstractEnumColumn<Snapshot, SnapshotStatus>() {
        @Override
        protected SnapshotStatus getRawValue(Snapshot snapshot) {
            return snapshot.getStatus();
        }
    };

    public static final AbstractCheckboxColumn<Snapshot> memoryColumn = new AbstractCheckboxColumn<Snapshot>() {
        @Override
        public Boolean getValue(Snapshot object) {
            return !object.getMemoryVolume().isEmpty();
        }

        @Override
        protected boolean canEdit(Snapshot object) {
            return false;
        }
    };

    public static final AbstractSafeHtmlColumn<Snapshot> descriptionColumn = new AbstractSafeHtmlColumn<Snapshot>() {
        @Override
        public final SafeHtml getValue(Snapshot snapshot) {
            // Get raw description string (ignore < and > characters).
            // Customize description style as needed.
            SafeHtml description = SafeHtmlUtils.fromString(snapshot.getDescription());
            String descriptionStr = description.asString();

            if (snapshot.getStatus() == SnapshotStatus.IN_PREVIEW) {
                List<String> previewedItems = new ArrayList<String>(Arrays.asList(constants.vmConfiguration()));
                previewedItems.addAll(Linq.getDiskAliases(snapshot.getDiskImages()));
                descriptionStr = messages.snapshotPreviewing(
                        descriptionStr, StringUtils.join(previewedItems, ", ")); //$NON-NLS-1$
                description = templates.snapshotDescription("color:orange", descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.STATELESS) {
                descriptionStr = descriptionStr + " (" + constants.readonlyLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                description = templates.snapshotDescription("font-style:italic", descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.PREVIEW) {
                descriptionStr = constants.snapshotDescriptionActiveVmBeforePreview();
                description = templates.snapshotDescription("color:gray", descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.ACTIVE) {
                descriptionStr = constants.snapshotDescriptionActiveVm();
                description = templates.snapshotDescription("color:gray", descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.REGULAR && !snapshot.getDiskImages().isEmpty()) {
                descriptionStr = messages.snapshotPreviewing(
                        descriptionStr, StringUtils.join(Linq.getDiskAliases(snapshot.getDiskImages()), ", ")); //$NON-NLS-1$
                description = templates.snapshotDescription("color:gold", descriptionStr); //$NON-NLS-1$
            }

            return description;
        }
    };
}
