package org.ovirt.engine.ui.webadmin.widget.table.cell;

import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class VolumeActivityCompositeCell<T extends GlusterTaskSupport> extends CompositeCell<T> {

    private final List<HasCell<T, ?>> hasCells;

    public VolumeActivityCompositeCell(List<HasCell<T, ?>> hasCells) {
        super(hasCells);
        this.hasCells = hasCells;
    }

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        if (!isVisible(value)) {
            return;
        }

        sb.appendHtmlConstant("<table style=\"margin:0 auto\"><tr>"); //$NON-NLS-1$
        Iterator<HasCell<T, ?>> iterator = hasCells.iterator();
        while (iterator.hasNext()) {
            render(context, value, sb, iterator.next());
        }
        sb.appendHtmlConstant("</tr></table>"); //$NON-NLS-1$
    }

    protected boolean isVisible(T value) {
        if (value == null || value.getAsyncTask() == null || value.getAsyncTask().getStatus() == null
                || value.getAsyncTask().getType() == null) {
            return false;
        }
        return true;
    }

    @Override
    protected <X> void render(Context context,
            T value,
            SafeHtmlBuilder sb,
            HasCell<T, X> hasCell) {
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
