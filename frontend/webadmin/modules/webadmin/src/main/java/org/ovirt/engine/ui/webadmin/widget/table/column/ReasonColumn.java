package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public abstract class ReasonColumn<T> extends AbstractSafeHtmlColumn<T> {

    private final static ApplicationResources resources = AssetProvider.getResources();
    private final static ApplicationTemplates templates = AssetProvider.getTemplates();

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
