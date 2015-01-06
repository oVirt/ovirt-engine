package org.ovirt.engine.ui.userportal.widget.extended.vm;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.ui.common.utils.ElementIdUtils;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;

/**
 * TODO remove
 * Decorates a cell with a tooltip which is given from a tooltip provider
 *
 * @param <C>
 *            the type that this Cell represents
 */
@Deprecated
public class TooltipCell<T> extends CompositeCell<T> {

    private final TooltipProvider<T> provider;
    private final HasCell<T, ?> hasCell;

    // DOM element ID settings for the text container element
    private String elementIdPrefix = DOM.createUniqueId();
    private String columnId;

    @SuppressWarnings("unchecked")
    public TooltipCell(HasCell<T, ?> hasCell, TooltipProvider<T> provider) {
        super(new ArrayList<HasCell<T, ?>>(Arrays.asList(hasCell)));
        this.hasCell = hasCell;
        this.provider = provider;
    }

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        String tooltip = provider.getTooltip(value);
        if (tooltip == null) {
            tooltip = ""; //$NON-NLS-1$
        }
        // TODO(vszocs) consider using SafeHtmlTemplates instead of building HTML manually
        sb.appendHtmlConstant("<div id=\"" //$NON-NLS-1$
                + ElementIdUtils.createTableCellElementId(elementIdPrefix, columnId, context)
                + "\" title=\"" //$NON-NLS-1$
                + SafeHtmlUtils.htmlEscape(tooltip)
                + "\">"); //$NON-NLS-1$
        super.render(context, value, sb, hasCell);
        sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
    }

    public static interface TooltipProvider<T> {
        String getTooltip(T value);
    }

    @Override
    protected Element getContainerElement(Element parent) {
        return parent.getFirstChildElement();
    }

}
