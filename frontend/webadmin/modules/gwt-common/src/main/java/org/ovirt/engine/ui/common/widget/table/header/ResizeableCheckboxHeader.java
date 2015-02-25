package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;
import org.ovirt.engine.ui.common.widget.table.resize.HasResizableColumns;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;

public class ResizeableCheckboxHeader<T> extends ResizableHeader<T> {

    private final AbstractCheckboxHeader checkboxHeaderDelegate;

    public ResizeableCheckboxHeader(AbstractCheckboxHeader checkboxHeader,
            Column<T, ?> column, HasResizableColumns<T> table) {
        super(new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(""), checkboxHeader.getTooltip(), //$NON-NLS-1$
                createSafeHtmlCell()), column, table, true);
        this.checkboxHeaderDelegate = checkboxHeader;
    }

    public static SafeHtmlCell createSafeHtmlCell() {
        return new SafeHtmlCell() {
            @Override
            public Set<String> getConsumedEvents() {
                Set<String> set = new HashSet<String>(super.getConsumedEvents());
                set.add(BrowserEvents.CLICK); // for sorting
                set.add(BrowserEvents.MOUSEMOVE); // for changing mouse cursor
                set.add(BrowserEvents.CHANGE); // for checkbox toggle
                return set;
            }
        };
    }

    @Override
    public void onBrowserEvent(Context context, Element target, NativeEvent event) {
        if (checkboxHeaderDelegate.getCell().getConsumedEvents().contains(event.getType())) {
            checkboxHeaderDelegate.onBrowserEvent(context, target, event);
        }
        super.onBrowserEvent(context, target, event);
    }

    @Override
    public void render(Context context, SafeHtmlBuilder sb) {
        checkboxHeaderDelegate.render(context, sb);
    }

}
