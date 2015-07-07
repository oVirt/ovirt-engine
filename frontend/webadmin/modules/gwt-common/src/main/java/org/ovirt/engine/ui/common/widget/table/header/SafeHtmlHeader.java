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
 * @param <H> Cell data type.
 */
public class SafeHtmlHeader extends AbstractHeader<SafeHtml> implements ColumnWithElementId, TooltipHeader {

    private SafeHtml headerText;
    private SafeHtml renderedHeaderText;
    private SafeHtml tooltipText;

    public static final SafeHtmlHeader BLANK_HEADER = new SafeHtmlHeader(SafeHtmlUtils.fromSafeConstant("")); //$NON-NLS-1$

    interface CellTemplate extends SafeHtmlTemplates {
        @Template("<span class=\"hasTooltip\">{0}</span>")
        SafeHtml hasTooltip(SafeHtml html);
    }

    private static final CellTemplate templates = GWT.create(CellTemplate.class);

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

    /**
     * Return the SafeHtml to be rendered.
     * @see com.google.gwt.user.cellview.client.Header#getValue()
     */
    @Override
    public SafeHtml getValue() {
        return renderedHeaderText;
    }

    protected void setValue(SafeHtml headerText) {
        this.headerText = headerText;
        setHeaderTooltipStyle(this.tooltipText);
    }

    protected void setTooltip(SafeHtml tooltipText) {
        this.tooltipText = tooltipText;
        setHeaderTooltipStyle(tooltipText);
    }

    /**
     * Toggle the header tooltip style (to give a visual clue that this header can be hovered over).
     */
    protected void setHeaderTooltipStyle(SafeHtml tooltipText) {
        if (tooltipText == null || tooltipText.asString().isEmpty()) {
            renderedHeaderText = this.headerText;
        }
        else {
            renderedHeaderText = templates.hasTooltip(headerText);
        }
    }

}
