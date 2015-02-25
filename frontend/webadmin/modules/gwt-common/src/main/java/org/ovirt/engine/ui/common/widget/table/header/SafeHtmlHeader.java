package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A Header that renders SafeHtml. Supports tooltips. Supports element-id framework.
 *
 * @param <H> Cell data type.
 */
public class SafeHtmlHeader extends AbstractHeader<SafeHtml> implements ColumnWithElementId, TooltipHeader {

    private SafeHtml headerText;
    private SafeHtml tooltipText;

    public static final SafeHtmlHeader BLANK_HEADER = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant("")); //$NON-NLS-1$

    public SafeHtmlHeader(SafeHtmlCell cell) {
        super(cell);
    }

    public SafeHtmlHeader(SafeHtml headerText) {
        this(headerText, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtmlCell safeHtmlCell) {
        super(safeHtmlCell);
        this.headerText = headerText;
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtml tooltipText) {
        this(headerText, tooltipText, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtml tooltipText, SafeHtmlCell safeHtmlCell) {
        super(safeHtmlCell);
        this.headerText = headerText;
        this.tooltipText = tooltipText;
    }

    public static SafeHtmlCell createSafeHtmlCell() {
        return new SafeHtmlCell() {
            @Override
            public Set<String> getConsumedEvents() {
                Set<String> set = new HashSet<>(super.getConsumedEvents());
                set.add(BrowserEvents.CLICK); // for sorting
                return set;
            }
        };
    }

    public static SafeHtmlHeader fromSafeConstant(String headerText) {
        return new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(headerText));
    }

    public SafeHtmlCell getCell() {
        return (SafeHtmlCell) super.getCell();
    }

    @Override
    public SafeHtml getTooltip() {
        return tooltipText;
    }

    @Override
    public SafeHtml getValue() {
        return headerText;
    }

    protected void setValue(SafeHtml headerText) {
        this.headerText = headerText;
    }

    protected void setTooltip(SafeHtml tooltipText) {
        this.tooltipText = tooltipText;
    }



}
