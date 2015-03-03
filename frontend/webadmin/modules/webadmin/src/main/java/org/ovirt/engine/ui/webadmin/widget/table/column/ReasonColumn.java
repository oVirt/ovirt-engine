package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public abstract class ReasonColumn<T> extends AbstractSafeHtmlColumn<T> {

    ApplicationResources resources = GWT.create(ApplicationResources.class);
    ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);

    @Override
    public SafeHtml getValue(T value) {
        if (getReason(value) != null && !getReason(value).trim().isEmpty()) {
            return templates.inlineImageWithTitle(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.commentImage())
                    .getHTML()),
                    getReason(value));
        }
        return null;
    }

    protected abstract String getReason(T value);
}
