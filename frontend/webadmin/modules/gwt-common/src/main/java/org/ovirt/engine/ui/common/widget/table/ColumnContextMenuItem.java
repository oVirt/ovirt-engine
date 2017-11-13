package org.ovirt.engine.ui.common.widget.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Item rendered within the {@link ColumnContextMenu}.
 *
 * @param <T>
 *            Table row data type.
 */
public class ColumnContextMenuItem<T> extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, ColumnContextMenuItem> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {

        String highlightable();

        String dragSource();

        String validDropTarget();

    }

    @UiField
    Style style;

    @UiField
    FocusPanel container;

    @UiField
    Label title;

    @UiField
    SimpleCheckBox checkBox;

    private final ColumnController<T> controller;
    private final Column<T, ?> column;

    public ColumnContextMenuItem(ColumnController<T> controller, Column<T, ?> column) {
        this.controller = controller;
        this.column = column;

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        getElement().setDraggable(Element.DRAGGABLE_TRUE);

        update();
    }

    public void update() {
        title.setText(controller.getColumnContextMenuTitle(column));
        checkBox.setValue(controller.isColumnVisible(column));

        container.setStyleName(style.highlightable(), true);
        container.setStyleName(style.dragSource(), false);
        container.setStyleName(style.validDropTarget(), false);
    }

    boolean eventTargetIsCheckBox(DomEvent<?> event) {
        EventTarget eventTarget = event.getNativeEvent().getEventTarget();
        return InputElement.is(eventTarget)
                && "checkbox".equals(InputElement.as(eventTarget).getAttribute("type")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @UiHandler("checkBox")
    void onClick(ClickEvent event) {
        controller.setColumnVisible(column, checkBox.getValue());
    }

    @UiHandler("container")
    void onMouseDown(MouseDownEvent event) {
        if (!eventTargetIsCheckBox(event)) {
            container.setStyleName(style.highlightable(), false);
            container.setStyleName(style.dragSource(), true);
        }
    }

    @UiHandler("container")
    void onMouseUp(MouseUpEvent event) {
        if (!eventTargetIsCheckBox(event)) {
            container.setStyleName(style.highlightable(), true);
            container.setStyleName(style.dragSource(), false);
        }
    }

    @UiHandler("container")
    void onDragStart(DragStartEvent event) {
        int itemColumnIndex = controller.getColumnIndex(column);

        // While the HTML5 DnD spec allows setting "drag data" within
        // "dragstart" event, it prevents access to that data in every
        // other event, except for the "drop" event. This way, it's not
        // possible to check "drag data" _during_ a drag operation, e.g.
        // in order to visually highlight a possible drop target.
        controller.setDragIndex(itemColumnIndex);

        // Without this, the DnD behavior doesn't seem to be triggered.
        event.getDataTransfer().setData("text", String.valueOf(itemColumnIndex)); //$NON-NLS-1$

        // Define a custom drag image.
        event.getDataTransfer().setDragImage(getElement(),
                event.getNativeEvent().getClientX() - getAbsoluteLeft(),
                event.getNativeEvent().getClientY() - getAbsoluteTop());
    }

    @UiHandler("container")
    void onDragEnter(DragEnterEvent event) {
        // This is actually needed for the "drop" event to fire at all.
        event.preventDefault();

        container.setStyleName(style.highlightable(), false);
    }

    @UiHandler("container")
    void onDragOver(DragOverEvent event) {
        // This is actually needed for the "drop" event to fire at all.
        event.preventDefault();

        int draggedColumnIndex = controller.getDragIndex();
        int itemColumnIndex = controller.getColumnIndex(column);

        // Highlight the drop target.
        container.setStyleName(style.validDropTarget(), draggedColumnIndex != itemColumnIndex);
    }

    @UiHandler("container")
    void onDragLeave(DragLeaveEvent event) {
        container.setStyleName(style.validDropTarget(), false);
    }

    @UiHandler("container")
    void onDragEnd(DragEndEvent event) {
        controller.updateColumnContextMenu();
    }

    @UiHandler("container")
    void onDrop(DropEvent event) {
        // Make sure to prevent the (useless) default browser action,
        // e.g. "open as link" for the dropped element.
        event.preventDefault();

        int draggedColumnIndex = controller.getDragIndex();
        int itemColumnIndex = controller.getColumnIndex(column);

        // Swap the columns.
        if (draggedColumnIndex != itemColumnIndex) {
            controller.swapColumns(controller.getColumn(draggedColumnIndex), column);
        }

        // Reset the drag index.
        controller.setDragIndex(ColumnController.NO_DRAG);
    }

}
