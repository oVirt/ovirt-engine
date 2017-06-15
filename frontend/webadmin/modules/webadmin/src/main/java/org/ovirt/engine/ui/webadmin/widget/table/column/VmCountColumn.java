package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Displays number of VMs along with number of migrations:
 * <pre>
 *     I. For newer hosts which report incoming/outgoing migrations separately
 *          1. No migrations
 *                           VMs
 *
 *          2. Only incoming migrations
 *               (IN_VMs ->) VMs
 *
 *          3. Only outgoing migrations
 *                           VMs (OUT_VMs ->)
 *
 *          4. Incoming and Outgoing migrations
 *               (IN_VMs ->) VMs (OUT_VMs ->)
 *
 *     II. For older hosts which reports only total number of migrations
 *                           VMs (TOTAL_MIGRATIONS <->)
 * </pre>
 */
public class VmCountColumn extends AbstractSafeHtmlColumn<VDS> {
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    private static SafeHtml rightArrowImageHtml =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.rightArrowImage()).getHTML());
    private static SafeHtml doubleArrowImageHtml =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.doubleArrowImage()).getHTML());

    @Override
    public SafeHtml getValue(VDS object) {
        String vmCountStr = String.valueOf(object.getVmCount());
        int incomingMigrations = object.getIncomingMigrations();
        int outgoingMigrations = object.getOutgoingMigrations();
        int totalMigrations = object.getVmMigrating() != null ? object.getVmMigrating() : 0;

        if (incomingMigrations == 0 && outgoingMigrations == 0
                || totalMigrations == 0) {
            // no migrations, just display number of VMs
            return new SafeHtmlBuilder().appendEscaped(vmCountStr).toSafeHtml();
        }

        // TODO remove whole if in 4.x when all hosts will report in/out migrations separately
        if (!areInOutMigrationsSeparated(incomingMigrations, outgoingMigrations)) {
            String migrationStr = String.valueOf(totalMigrations);
            return templates.vmCountWithMigrations(
                    messages.vmsWithTotalMigrations(
                            vmCountStr,
                            migrationStr),
                    createInOutMigrationsTemplate(0, doubleArrowImageHtml),
                    vmCountStr,
                    createInOutMigrationsTemplate(totalMigrations, doubleArrowImageHtml));
        }

        return templates.vmCountWithMigrations(
                messages.vmsWithInOutMigrations(
                        vmCountStr,
                        String.valueOf(incomingMigrations),
                        String.valueOf(outgoingMigrations)),
                createInOutMigrationsTemplate(incomingMigrations, rightArrowImageHtml),
                vmCountStr,
                createInOutMigrationsTemplate(outgoingMigrations, rightArrowImageHtml));
    }

    protected boolean areInOutMigrationsSeparated(int incomingMigrations, int outgoingMigrations) {
        return incomingMigrations != -1 && outgoingMigrations != -1;
    }

    protected SafeHtml createInOutMigrationsTemplate(int migrations, SafeHtml arrowImage) {
        String prefix;
        String postfix;
        String migrationsStr;
        SafeHtml image;

        if (migrations > 0) {
            prefix = "("; //$NON-NLS-1$
            migrationsStr = String.valueOf(migrations);
            image = arrowImage;
            postfix = ")"; //$NON-NLS-1$
        } else {
            prefix = "\u00a0"; //$NON-NLS-1$
            migrationsStr = "\u00a0"; //$NON-NLS-1$
            image = SafeHtmlUtils.EMPTY_SAFE_HTML;
            postfix = "\u00a0"; //$NON-NLS-1$
        }

        return templates.vmCountInOutMigrations(
                prefix,
                migrationsStr,
                image,
                postfix);
    }
}
