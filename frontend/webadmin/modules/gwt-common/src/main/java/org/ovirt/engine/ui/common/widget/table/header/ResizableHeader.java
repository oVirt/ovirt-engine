package org.ovirt.engine.ui.common.widget.table.header;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.table.ColumnResizeHandler;
import org.ovirt.engine.ui.common.widget.table.HasResizableColumns;
import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;

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

/**
 * A Header that allows the user to resize the associated column by dragging its right-hand
 * border using mouse. Renders SafeHtml. Supports tooltips.
 *
 * @param <T>
 *            Table row data type.
 */
public class ResizableHeader<T> extends SafeHtmlHeader {

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

    private boolean resizeEnabled = true;

    /**
     * Constructor.
     * @param safeHtmlHeader The header.
     * @param column The column associated with the header.
     * @param table The table containing the header/column.
     * @param applyStyle Whether to apply default styling.
     */
    public ResizableHeader(SafeHtmlHeader safeHtmlHeader, Column<T, ?> column, HasResizableColumns<T> table,
            boolean applyStyle) {
        super(createSafeHtmlCell()); // ignore the header's cell -- we need to specify our own set of events
        style = RESOURCES.resizableHeaderCss();
        style.ensureInjected();
        setValue(safeHtmlHeader.getValue());
        setTooltip(safeHtmlHeader.getTooltip());
        this.column = column;
        this.table = table;
        this.applyStyle = applyStyle;
    }

    public static SafeHtmlCell createSafeHtmlCell() {
        return new SafeHtmlCell() {
            @Override
            public Set<String> getConsumedEvents() {
                Set<String> set = new HashSet<>(super.getConsumedEvents());
                set.add(BrowserEvents.CLICK); // for sorting
                set.add(BrowserEvents.CONTEXTMENU); // for column context menu
                set.add(BrowserEvents.MOUSEMOVE); // for changing mouse cursor
                set.add(BrowserEvents.CHANGE); // for checkbox toggle
                return set;
            }
        };
    }

    @Override
    public SafeHtml getValue() {
        return applyStyle ? TEMPLATE.templatedContent(style.cellTableHeaderContent(), super.getValue()) : super.getValue();
    }

    @Override
    public void onBrowserEvent(Context context, Element target, NativeEvent event) {
        super.onBrowserEvent(context, target, event);

        if (!resizeEnabled) {
            return;
        }

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
                new ColumnResizeHandler<>(headerElement, column, table);
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

    public void setResizeEnabled(boolean resizeEnabled) {
        this.resizeEnabled = resizeEnabled;
    }

}
