package org.ovirt.engine.ui.userportal.widget.extended.vm;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Decorates a cell with a tooltip which is given from a tooltip provider
 * @param <C> the type that this Cell represents
 */
public class TooltipCell<T> extends CompositeCell<T> {

    private final TooltipProvider<T> provider;
    private final HasCell<T, ?> hasCell;

    @SuppressWarnings("unchecked")
    public TooltipCell(HasCell<T, ?> hasCell, TooltipProvider<T> provider) {
        super(new ArrayList<HasCell<T, ?>>(Arrays.asList(hasCell)));
        this.hasCell = hasCell;
        this.provider = provider;
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<div title=\"" + SafeHtmlUtils.htmlEscape(provider.getTooltip(value)) + "\" >");
        render(context, value, sb, hasCell);
        sb.appendHtmlConstant("</div>");
    }

    public static interface TooltipProvider<T> {
        String getTooltip(T value);
    }

    @Override
    protected Element getContainerElement(Element parent) {
        return parent.getFirstChildElement();
    }
}
