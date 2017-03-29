package org.ovirt.engine.ui.common.widget.table.cell;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;

import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Cell that renders ActionButtonDefinition-like icon buttons. Supports tooltips.
 *
 * @param <T>
 *            The data type of the cell (the model)
 */
public abstract class AbstractIconButtonCell<T> extends AbstractButtonCell<T> {

    private HasCssName iconType;

    public AbstractIconButtonCell(HasCssName iconType) {

        super();
        this.iconType = iconType;
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb, String id) {

        Icon icon = new Icon();
        icon.setId(id);
        StyleHelper.addEnumStyleName(icon, iconType);
        icon.addStyleName(isEnabled(value) ? "icon-enabled" : "icon-disabled"); //$NON-NLS-1$ //$NON-NLS-2$

        sb.append(SafeHtmlUtils.fromSafeConstant(icon.toString()));
    }
}
