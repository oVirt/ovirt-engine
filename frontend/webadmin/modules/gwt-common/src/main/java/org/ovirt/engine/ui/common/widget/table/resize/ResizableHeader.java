package org.ovirt.engine.ui.common.widget.table.resize;

import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlCellWithTooltip;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;

/**
 * A {@link Header} that allows the user to resize the associated column by dragging its right-hand border using mouse.
 * <p>
 * This header has its value rendered through safe HTML markup.
 *
 * @param <T>
 *            Table row data type.
 */
public class ResizableHeader<T> extends Header<SafeHtml> {

    /**
     * The template interface that defines the wrapper around the content. This is so we can
     * add/remove style sheet classes to/from the content.
     */
    public interface HeaderTemplate extends SafeHtmlTemplates {
        /**
         * Apply the template to the passed in value and style sheet classes. If more than one class is used
         * you must have them space separated.
         * @param cssClassNames The names of the css classes, space separated.
         * @param value The value of the content.
         * @return The content wrapped in the template.
         */
        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml templatedContent(String cssClassNames, SafeHtml value);
    }

    /**
     * Style sheet interface.
     */
    public interface ResizableHeaderCss extends CssResource {
        /**
         * Get the style sheet class name for the cell table header content.
         * @return The style sheet class name as a {@code String}
         */
        String cellTableHeaderContent();
    }

    /**
     * Resize-able Header resources interface.
     */
    public interface ResizableHeaderResources extends ClientBundle {
        /**
         * Get the Resource containing the resize-able header style sheet classes.
         * @return The resource as a {@code ResizableHeaderCss} object.
         */
        @Source("org/ovirt/engine/ui/common/css/ResizableHeader.css")
        ResizableHeaderCss resizableHeaderCss();
    }

    /**
     * Singleton instance of the {@code HeaderTemplate}.
     */
    private static final HeaderTemplate TEMPLATE = GWT.create(HeaderTemplate.class);

    /**
     * Singleton instance of the {@code ResizableHeaderResource}.
     */
    private static final ResizableHeaderResources RESOURCES = GWT.create(ResizableHeaderResources.class);

    /**
     * Width of the column header resize bar area, in pixels.
     */
    public static final int RESIZE_BAR_WIDTH = 7;

    /**
     * The content text.
     */
    private final SafeHtml text;
    /**
     * The resize-able column.
     */
    private final Column<T, ?> column;
    /**
     * The table that contains the header and columns.
     */
    private final HasResizableColumns<T> table;
    /**
     * The styles for the header.
     */
    private final ResizableHeaderCss style;

    private final boolean applyStyle;

    /**
     * Constructor.
     * @param text The contents of the header.
     * @param column The column associated with the header.
     * @param table The table containing the header/column.
     */
    public ResizableHeader(SafeHtml text, Column<T, ?> column, HasResizableColumns<T> table, boolean applyStyle) {
        this(text, column, table, new SafeHtmlCellWithTooltip(BrowserEvents.CLICK,
                BrowserEvents.MOUSEDOWN,
                BrowserEvents.MOUSEMOVE,
                BrowserEvents.MOUSEOVER), applyStyle);
    }

    /**
     * Constructor.
     * @param text The contents of the header.
     * @param column The column associated with the header.
     * @param table The table containing the header/column.
     * @param cell The cell that defines the header cell.
     */
    public ResizableHeader(SafeHtml text, Column<T, ?> column, HasResizableColumns<T> table,
            Cell<SafeHtml> cell) {
        this(text, column, table, cell, true);
    }

    /**
     * Constructor.
     * @param text The contents of the header.
     * @param column The column associated with the header.
     * @param table The table containing the header/column.
     * @param cell The cell that defines the header cell.
     * @param applyStyle Whether to apply default styling.
     */
    public ResizableHeader(SafeHtml text, Column<T, ?> column, HasResizableColumns<T> table,
            Cell<SafeHtml> cell, boolean applyStyle) {
        super(cell);
        style = RESOURCES.resizableHeaderCss();
        style.ensureInjected();
        this.text = text;
        this.column = column;
        this.table = table;
        this.applyStyle = applyStyle;
    }

    @Override
    public SafeHtml getValue() {
        return applyStyle ? TEMPLATE.templatedContent(style.cellTableHeaderContent(), text) : text;
    }

    @Override
    public void onBrowserEvent(Context context, Element target, NativeEvent event) {
        super.onBrowserEvent(context, target, event);

        int clientX = event.getClientX();
        int absoluteLeft = target.getAbsoluteLeft();
        int offsetWidth = target.getOffsetWidth();
        boolean mouseOverResizeBarArea = clientX > absoluteLeft + offsetWidth - RESIZE_BAR_WIDTH;

        // Resolve th element (header cell for given column)
        Element headerElement = findThElement(target);
        assert headerElement != null;

        // Update mouse cursor for the header element
        if (mouseOverResizeBarArea) {
            headerElement.getStyle().setCursor(Cursor.COL_RESIZE);
        } else if (column.isSortable()) {
            headerElement.getStyle().setCursor(Cursor.POINTER);
        } else {
            headerElement.getStyle().setCursor(Cursor.DEFAULT);
        }

        // On mouse down event, which initiates the column resize operation,
        // register a column resize handler that listens to mouse move events
        if (BrowserEvents.MOUSEDOWN.equals(event.getType())) {
            if (mouseOverResizeBarArea) {
                new ColumnResizeHandler<T>(headerElement, column, table);
            }
            event.preventDefault();
            event.stopPropagation();
        }
    }

    /**
     * Find the 'TH' DOM element for the passed in {@code Element}.
     * @param elm The element to start the search at.
     * @return The 'TH' DOM element if found or null.
     */
    Element findThElement(Element elm) {
        if (elm == null) {
            return null;
        } else if (TableCellElement.TAG_TH.equalsIgnoreCase(elm.getTagName())) {
            return elm;
        }
        return findThElement(elm.getParentElement());
    }

}
