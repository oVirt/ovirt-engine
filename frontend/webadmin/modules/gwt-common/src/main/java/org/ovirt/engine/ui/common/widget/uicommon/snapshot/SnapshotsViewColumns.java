package org.ovirt.engine.ui.common.widget.uicommon.snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.Linq;

import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class SnapshotsViewColumns {

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    protected static final FullDateTimeRenderer fullDateTimeRenderer = new FullDateTimeRenderer();

    public static final AbstractTextColumn<Snapshot> dateColumn = new AbstractTextColumn<Snapshot>() {
        @Override
        public String getValue(Snapshot snapshot) {
            if (snapshot.getType() == SnapshotType.ACTIVE) {
                return constants.currentSnapshotLabel();
            }
            return fullDateTimeRenderer.render(snapshot.getCreationDate());
        }
    };

    public static final AbstractTextColumn<Snapshot> statusColumn = new AbstractEnumColumn<Snapshot, SnapshotStatus>() {
        @Override
        protected SnapshotStatus getRawValue(Snapshot snapshot) {
            return snapshot.getStatus();
        }
    };

    public static final AbstractCheckboxColumn<Snapshot> memoryColumn = new AbstractCheckboxColumn<Snapshot>() {
        @Override
        public Boolean getValue(Snapshot object) {
            return object.containsMemory();
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
                List<String> previewedItems = new ArrayList<>(Arrays.asList(constants.vmConfiguration()));
                previewedItems.addAll(Linq.getDiskAliases(snapshot.getDiskImages()));
                descriptionStr = messages.snapshotPreviewing(
                        descriptionStr, String.join(", ", previewedItems)); //$NON-NLS-1$
                description = templates.snapshotDescription(SafeStylesUtils.forTrustedColor("orange"), descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.STATELESS) {
                descriptionStr = descriptionStr + " (" + constants.readonlyLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                description = templates.snapshotDescription(SafeStylesUtils.forFontStyle(FontStyle.ITALIC),
                        descriptionStr);
            }
            else if (snapshot.getType() == SnapshotType.PREVIEW) {
                descriptionStr = constants.snapshotDescriptionActiveVmBeforePreview();
                description = templates.snapshotDescription(SafeStylesUtils.forTrustedColor("gray"), descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.ACTIVE) {
                descriptionStr = constants.snapshotDescriptionActiveVm();
                description = templates.snapshotDescription(SafeStylesUtils.forTrustedColor("gray"), descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.getType() == SnapshotType.REGULAR && !snapshot.getDiskImages().isEmpty()) {
                descriptionStr = messages.snapshotPreviewing(
                        descriptionStr, String.join(", ", Linq.getDiskAliases(snapshot.getDiskImages()))); //$NON-NLS-1$
                description = templates.snapshotDescription(SafeStylesUtils.forTrustedColor("gold"), descriptionStr); //$NON-NLS-1$
            }
            else if (snapshot.isVmConfigurationBroken()) {
                descriptionStr = descriptionStr + " (" + constants.brokenVmConfiguration() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                description = templates.snapshotDescription(SafeStylesUtils.forTrustedColor("red"), descriptionStr); //$NON-NLS-1$
            }

            return description;
        }
    };
}
