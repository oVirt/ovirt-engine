package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlWithStyleCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public abstract class ReasonColumn<T> extends AbstractColumn<T, SafeHtml> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    public ReasonColumn() {
        super(new SafeHtmlWithStyleCell());
    }

    @Override
    public SafeHtml getValue(T value) {
        if (getReason(value) != null && !getReason(value).trim().isEmpty()) {
            return templates.inlineImage(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.commentImage())
                    .getHTML()));
        }
        return null;
    }

    protected abstract String getReason(T value);
}
