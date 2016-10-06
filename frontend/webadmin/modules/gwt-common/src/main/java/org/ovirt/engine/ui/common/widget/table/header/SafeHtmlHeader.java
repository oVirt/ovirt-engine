package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;
import org.ovirt.engine.ui.common.widget.tooltip.ProvidesTooltip;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A Header that renders SafeHtml. Supports tooltips. Supports element-id framework.
 */
public class SafeHtmlHeader extends AbstractHeader<SafeHtml> implements ColumnWithElementId, ProvidesTooltip {

    private SafeHtml headerText;
    private SafeHtml tooltipText;

    public SafeHtmlHeader(SafeHtmlCell safeHtmlCell) {
        this(null, null, safeHtmlCell);
    }

    public SafeHtmlHeader(SafeHtml headerText) {
        this(headerText, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtmlCell safeHtmlCell) {
        this(headerText, null, safeHtmlCell);
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtml tooltipText) {
        this(headerText, tooltipText, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtml tooltipText, SafeHtmlCell safeHtmlCell) {
        super(safeHtmlCell);
        setValue(headerText);
        setTooltip(tooltipText);
    }

    public static SafeHtmlCell createSafeHtmlCell() {
        return new SafeHtmlCell() {
            @Override
            public Set<String> getConsumedEvents() {
                Set<String> set = new HashSet<>(super.getConsumedEvents());
                set.add(BrowserEvents.CLICK); // for sorting
                set.add(BrowserEvents.CONTEXTMENU); // for column context menu
                return set;
            }
        };
    }

    public static SafeHtmlHeader fromSafeConstant(String headerText) {
        return new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant(headerText));
    }

    @Override
    public SafeHtmlCell getCell() {
        return (SafeHtmlCell) super.getCell();
    }

    @Override
    public SafeHtml getTooltip() {
        return tooltipText;
    }

    /**
     * Return the SafeHtml to be rendered.
     * @see com.google.gwt.user.cellview.client.Header#getValue()
     */
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
