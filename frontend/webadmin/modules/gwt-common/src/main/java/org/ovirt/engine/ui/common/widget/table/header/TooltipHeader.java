package org.ovirt.engine.ui.common.widget.table.header;

import com.google.gwt.safehtml.shared.SafeHtml;

public interface TooltipHeader {

    /**
     * <p>
     * Implement this to return tooltip content for a header. Since it's a table header,
     * you'll likely use a constant.
     * </p>
     * <p>
     * The tooltip cell will then use this value when rendering the header.
     * </p>
     *
     * @return tooltip content
     */
    public SafeHtml getTooltip();
}
