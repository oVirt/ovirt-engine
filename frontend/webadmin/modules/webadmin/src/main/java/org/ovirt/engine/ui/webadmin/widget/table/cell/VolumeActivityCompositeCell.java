package org.ovirt.engine.ui.webadmin.widget.table.cell;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.ui.common.widget.table.cell.CompositeCell;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;

public class VolumeActivityCompositeCell<T extends GlusterTaskSupport> extends CompositeCell<T> {

    private final List<HasCell<T, ?>> hasCells;

    public VolumeActivityCompositeCell(List<HasCell<T, ?>> hasCells) {
        super(hasCells);
        this.hasCells = hasCells;
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        if (hasCells == null) {
            return set;
        }
        for(HasCell<T, ?> currentHasCell : hasCells) {
            if(currentHasCell instanceof Column) {
                Set<String> consumedEvents = ((Column) currentHasCell).getCell().getConsumedEvents();
                if(consumedEvents != null) {
                    set.addAll(consumedEvents);
                }
            }
            if(currentHasCell instanceof Cell) {
                set.addAll(((Cell)currentHasCell).getConsumedEvents());
            }
        }
        return set;
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb, String id) {
        sb.appendHtmlConstant("<table id=\"" + id + "\" style=\"margin:0 auto\"><tr>"); //$NON-NLS-1$ //$NON-NLS-2$
        Iterator<HasCell<T, ?>> iterator = hasCells.iterator();
        while (iterator.hasNext()) {
            render(context, value, sb, iterator.next(), id);
        }
        sb.appendHtmlConstant("</tr></table>"); //$NON-NLS-1$
    }

    @Override
    protected <X> void render(Context context,
            T value,
            SafeHtmlBuilder sb,
            HasCell<T, X> hasCell, String id) {
        Cell<X> cell = hasCell.getCell();
        sb.appendHtmlConstant("<td>"); //$NON-NLS-1$
        cell.render(context, hasCell.getValue(value), sb);
        sb.appendHtmlConstant("</td>"); //$NON-NLS-1$
    }

    @Override
    protected Element getContainerElement(Element parent) {
        return super.getContainerElement(parent)
                .getFirstChildElement()
                .getFirstChildElement()
                .getFirstChildElement();
    }
}
