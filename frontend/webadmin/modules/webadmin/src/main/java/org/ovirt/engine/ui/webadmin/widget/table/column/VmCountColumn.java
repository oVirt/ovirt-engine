package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VmCountColumn extends AbstractSafeHtmlColumn<VDS> {
    private static ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);
    private static final ApplicationMessages messages = GWT.create(ApplicationMessages.class);
    private static final ApplicationResources resources = GWT.create(ApplicationResources.class);
    private static SafeHtml imageHtml =
            SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.doubleArrowImage())
            .getHTML());

    @Override
    public SafeHtml getValue(VDS object) {
        String vmCountStr = String.valueOf(object.getVmCount());
        Integer vmMigrating = object.getVmMigrating();
        if (vmMigrating == null || vmMigrating == 0) {
            return new SafeHtmlBuilder().appendEscaped(vmCountStr).toSafeHtml();
        }

        String vmMigratingStr = String.valueOf(vmMigrating);
        String title = messages.migratingVmsOutOfTotal(vmCountStr, vmMigratingStr);
        return templates.vmCountWithMigrating(vmCountStr, vmMigratingStr, title, imageHtml);
    }
}
