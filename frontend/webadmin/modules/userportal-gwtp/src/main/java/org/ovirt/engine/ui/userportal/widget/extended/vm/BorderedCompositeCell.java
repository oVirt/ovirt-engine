package org.ovirt.engine.ui.userportal.widget.extended.vm;

import java.util.List;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Enriches the CompositeCell by drawing a border around itself
 *
 * @param <T>
 *            the data type of the inner cells
 */
public class BorderedCompositeCell<T> extends CompositeCell<T> {

    private final List<HasCell<T, ?>> hasCells;

    public BorderedCompositeCell(List<HasCell<T, ?>> hasCells) {
        super(hasCells);
        this.hasCells = hasCells;
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<div style=\"border-radius: 7px; border: 1px solid #c6c6c6; width: 128px; padding: 3px\">"); //$NON-NLS-1$
        for (HasCell<T, ?> hasCell : hasCells) {
            render(context, value, sb, hasCell);
        }
        sb.appendHtmlConstant("</div>"); //$NON-NLS-1$
    }

    @Override
    protected Element getContainerElement(Element parent) {
        return parent.getFirstChildElement();
    }

}
