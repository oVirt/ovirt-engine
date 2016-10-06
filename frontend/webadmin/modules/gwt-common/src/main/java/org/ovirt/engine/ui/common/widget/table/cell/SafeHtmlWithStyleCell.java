package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.HasStyleClass;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class SafeHtmlWithStyleCell extends AbstractCell<SafeHtml> implements HasStyleClass {

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private String styleClass = ""; //$NON-NLS-1$

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass == null ? "" : styleClass; //$NON-NLS-1$
    }

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb, String id) {
        if (value != null) {
            sb.append(templates.divWithStyle(styleClass, id, value));
        }
    }
}
