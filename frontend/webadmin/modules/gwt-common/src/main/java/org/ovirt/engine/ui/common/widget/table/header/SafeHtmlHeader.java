package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;
import org.ovirt.engine.ui.common.widget.table.column.ColumnWithElementId;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A Header that renders SafeHtml. Supports tooltips. Supports element-id framework.
 *
 * If you want to show an underline on the header to indicate the presence of a tooltip, use the
 * constructor with showUnderline and set it to true.
 *
 * @param <H> Cell data type.
 */
public class SafeHtmlHeader extends AbstractHeader<SafeHtml> implements ColumnWithElementId, TooltipHeader {

    private SafeHtml headerText;
    private SafeHtml tooltipText;
    boolean showUnderline = false;

    public static final SafeHtmlHeader BLANK_HEADER = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant("")); //$NON-NLS-1$

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<span class=\"hasTooltip\">{0}</span>")
        SafeHtml hasTooltip(SafeHtml html);
    }

    private static final CellTemplate templates = GWT.create(CellTemplate.class);

    public SafeHtmlHeader(SafeHtmlCell safeHtmlCell) {
        this(null, null, false, safeHtmlCell);
    }

    public SafeHtmlHeader(SafeHtml headerText) {
        this(headerText, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtmlCell safeHtmlCell) {
        this(headerText, null, false, safeHtmlCell);
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtml tooltipText) {
        this(headerText, tooltipText, false, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtml tooltipText, boolean showUnderline) {
        this(headerText, tooltipText, showUnderline, createSafeHtmlCell());
    }

    public SafeHtmlHeader(SafeHtml headerText, SafeHtml tooltipText, boolean showUnderline, SafeHtmlCell safeHtmlCell) {
        super(safeHtmlCell);
        this.tooltipText = tooltipText;
        this.showUnderline = showUnderline;
        setValue(headerText);
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
        if (showUnderline) {
            this.headerText = templates.hasTooltip(headerText);
        }
        else {
            this.headerText = headerText;
        }
    }

    protected void setTooltip(SafeHtml tooltipText) {
        this.tooltipText = tooltipText;
    }

}
