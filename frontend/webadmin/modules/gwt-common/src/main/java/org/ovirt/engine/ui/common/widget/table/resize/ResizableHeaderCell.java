package org.ovirt.engine.ui.common.widget.table.resize;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ResizableHeaderCell extends AbstractCell<SafeHtml> {

    public ResizableHeaderCell() {
        super("click", "dblclick", "mousedown", "mousemove"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.append(value);
        }
    }

}
