package org.ovirt.engine.ui.common.widget.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Context menu widget providing UI for {@link ColumnController} interface.
 * <p>
 * By design, the API is column-based (instead of index-based) so that
 * menu item operations map directly to GWT {@link Column} objects.
 * <p>
 * Make sure to call {@link #update}:
 * <ul>
 * <li>before displaying the context menu
 * <li>whenever column {@linkplain ColumnController#setColumnVisible visibility} changes
 * <li>whenever column {@linkplain ColumnController#swapColumns position} changes
 * </ul>
 *
 * @param <T>
 *            Table row data type.
 */
public class ColumnContextMenu<T> extends Widget {

    interface ContextMenuCss extends CssResource {

        @ClassName("columnContextMenuContainer")
        String container();

        @ClassName("columnContextMenuItem")
        String item();

        @ClassName("columnContextMenuItemMoving")
        String itemMoving();

        @ClassName("columnContextMenuItem-title")
        String item_title();

        @ClassName("columnContextMenuItem-button")
        String item_button();

        @ClassName("columnContextMenuItem-visibleIcon")
        String item_visibleIcon();

        @ClassName("columnContextMenuItem-hiddenIcon")
        String item_hiddenIcon();

        @ClassName("columnContextMenuItem-moveUpIcon")
        String item_moveUpIcon();

        @ClassName("columnContextMenuItem-moveDownIcon")
        String item_moveDownIcon();

    }

    interface ContextMenuResources extends ClientBundle {

        @Source("org/ovirt/engine/ui/common/css/ColumnContextMenu.css")
        ContextMenuCss css();

        @Source("org/ovirt/engine/ui/common/images/checkbox_yes.png")
        ImageResource visibleIcon();

        @Source("org/ovirt/engine/ui/common/images/checkbox_no.png")
        ImageResource hiddenIcon();

        @Source("org/ovirt/engine/ui/common/images/move_up.png")
        ImageResource moveUpIcon();

        @Source("org/ovirt/engine/ui/common/images/move_down.png")
        ImageResource moveDownIcon();

    }

    private static final ContextMenuResources RESOURCES = GWT.create(ContextMenuResources.class);

    private final ColumnController<T> controller;
    private final Element container;

    /**
     * JS array of GWT {@link Column} objects.
     */
    private final JavaScriptObject gwtColumns;

    /**
     * JS object that stores CSS class names.
     */
    private final JavaScriptObject classNames;

    /**
     * {@code true} if menu item animation is currently in progress.
     */
    private boolean animating = false;

    public ColumnContextMenu(ColumnController<T> controller) {
        this.controller = controller;
        this.container = DOM.createDiv();
        this.gwtColumns = JavaScriptObject.createArray();
        setElement(container);

        ContextMenuCss css = RESOURCES.css();
        css.ensureInjected();
        container.addClassName(css.container());
        this.classNames = initClassNames(css);
    }

    private native JavaScriptObject initClassNames(ContextMenuCss css) /*-{
        return {
            item: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::item()(),
            itemMoving: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::itemMoving()(),
            item_title: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::item_title()(),
            item_button: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::item_button()(),
            item_visibleIcon: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::item_visibleIcon()(),
            item_hiddenIcon: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::item_hiddenIcon()(),
            item_moveUpIcon: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::item_moveUpIcon()(),
            item_moveDownIcon: css.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu.ContextMenuCss::item_moveDownIcon()()
        };
    }-*/;

    /**
     * Add new item to the context menu.
     */
    public native void addItem(Column<T, ?> column) /*-{
        var $ = $wnd.jQuery;
        var self = this;
        var controller = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::controller;
        var container = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::container;
        var gwtColumns = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::gwtColumns;
        var classNames = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::classNames;

        function isAnimating () {
            return self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::animating;
        }

        function setAnimating (animating) {
            self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::animating = animating;
        }

        // Visually swap the given menu item with its immediate sibling
        function swapItems ($sourceItem, $siblingItem, moveUp, doneCallback) {
            var distance = $sourceItem.outerHeight();
            var duration = 350;
            setAnimating(true);

            $sourceItem.addClass(classNames.itemMoving);
            $siblingItem.addClass(classNames.itemMoving);

            $.when(
                $sourceItem.animate({
                    top: moveUp ? -distance : distance
                }, duration),
                $siblingItem.animate({
                    top: moveUp ? distance : -distance
                }, duration))
                .done(function () {
                    // Update DOM
                    if (moveUp) {
                        $sourceItem.insertBefore($siblingItem);
                    } else {
                        $sourceItem.insertAfter($siblingItem);
                    }

                    // Reset CSS
                    $sourceItem.css('top', '0px');
                    $siblingItem.css('top', '0px');

                    $sourceItem.removeClass(classNames.itemMoving);
                    $siblingItem.removeClass(classNames.itemMoving);

                    setAnimating(false);
                    doneCallback();
                });
        }

        // Item's HTML template (call update method to sync with controller)
        var $item = $('<div>').addClass(classNames.item)
            .append($('<span data-role="Title">').addClass(classNames.item_title))
            .append($('<span data-role="MoveDownButton">').addClass(classNames.item_button).addClass(classNames.item_moveDownIcon))
            .append($('<span data-role="MoveUpButton">').addClass(classNames.item_button).addClass(classNames.item_moveUpIcon))
            .append($('<span data-role="VisibilityButton">').addClass(classNames.item_button));

        // Click handler on VisibilityButton
        $item.on('click', 'span[data-role="VisibilityButton"]', function () {
            var $clickedItem = $(this).closest('div');

            if (!isAnimating()) {
                var clickedColumnIndex = $clickedItem.data('index');
                var clickedColumn = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumn(I)(clickedColumnIndex);
                var clickedColumnVisible = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::isColumnVisible(Lcom/google/gwt/user/cellview/client/Column;)(clickedColumn);

                controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::setColumnVisible(Lcom/google/gwt/user/cellview/client/Column;Z)(clickedColumn, !clickedColumnVisible);
            }

            return false;
        });

        // Click handler on MoveUpButton
        $item.on('click', 'span[data-role="MoveUpButton"]', function () {
            var $clickedItem = $(this).closest('div');
            var $prevItem = $clickedItem.prev();

            if (!isAnimating() && $prevItem.length) {
                var clickedColumnIndex = $clickedItem.data('index');
                var clickedColumn = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumn(I)(clickedColumnIndex);
                var prevColumnIndex = $prevItem.data('index');
                var prevColumn = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumn(I)(prevColumnIndex);

                swapItems($clickedItem, $prevItem, true, function () {
                    controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::swapColumns(Lcom/google/gwt/user/cellview/client/Column;Lcom/google/gwt/user/cellview/client/Column;)(clickedColumn, prevColumn);
                });
            }

            return false;
        });

        // Click handler on MoveDownButton
        $item.on('click', 'span[data-role="MoveDownButton"]', function () {
            var $clickedItem = $(this).closest('div');
            var $nextItem = $clickedItem.next();

            if (!isAnimating() && $nextItem.length) {
                var clickedColumnIndex = $clickedItem.data('index');
                var clickedColumn = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumn(I)(clickedColumnIndex);
                var nextColumnIndex = $nextItem.data('index');
                var nextColumn = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumn(I)(nextColumnIndex);

                swapItems($clickedItem, $nextItem, false, function () {
                    controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::swapColumns(Lcom/google/gwt/user/cellview/client/Column;Lcom/google/gwt/user/cellview/client/Column;)(clickedColumn, nextColumn);
                });
            }

            return false;
        });

        // Update GWT columns
        gwtColumns.push(column);

        // Update DOM
        $(container).append($item);
    }-*/;

    /**
     * Remove existing item from the context menu.
     */
    public native void removeItem(Column<T, ?> column) /*-{
        var self = this;
        var gwtColumns = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::gwtColumns;

        var itemInfo = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::getItemInfo(Lcom/google/gwt/user/cellview/client/Column;)(column);
        var $item = itemInfo.$item;

        if (itemInfo.isValid) {
            // Remove all handlers attached to item's element
            $item.off();

            // Update GWT columns
            gwtColumns.splice(itemInfo.storedColumnIndex, 1);

            // Update DOM
            $item.remove();
        }
    }-*/;

    /**
     * Check if the context menu contains given item.
     */
    public native boolean containsItem(Column<T, ?> column) /*-{
        return this.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::getItemInfo(Lcom/google/gwt/user/cellview/client/Column;)(column).isValid;
    }-*/;

    /**
     * Update existing context menu items and corresponding GWT column meta-data.
     */
    public native void update() /*-{
        var $ = $wnd.jQuery;
        var self = this;
        var controller = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::controller;
        var container = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::container;
        var gwtColumns = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::gwtColumns;
        var classNames = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::classNames;

        var missingColumns = [];

        // Update existing menu items
        gwtColumns.forEach(function (column) {
            var actualColumnIndex = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumnIndex(Lcom/google/gwt/user/cellview/client/Column;)(column);

            // Mark missing column for removal
            if (actualColumnIndex < 0) {
                missingColumns.push(column);
                return;
            }

            var itemInfo = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::getItemInfo(Lcom/google/gwt/user/cellview/client/Column;)(column);
            var $item = itemInfo.$item;

            if (itemInfo.isValid) {
                var $title = $('span[data-role="Title"]', $item);
                var $visibilityButton = $('span[data-role="VisibilityButton"]', $item);

                var title = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumnContextMenuTitle(Lcom/google/gwt/user/cellview/client/Column;)(column);
                var visible = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::isColumnVisible(Lcom/google/gwt/user/cellview/client/Column;)(column);

                // Assign actual column index
                $item.data('index', actualColumnIndex);

                // Update title
                $title.text(title);

                // Update buttons
                $visibilityButton.toggleClass(classNames.item_visibleIcon, visible);
                $visibilityButton.toggleClass(classNames.item_hiddenIcon, !visible);
            }
        });

        // Remove missing columns
        missingColumns.forEach(function (column) {
            self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::removeItem(Lcom/google/gwt/user/cellview/client/Column;)(column);
        });

        // Sort GWT columns based on actual column index (update meta-data)
        gwtColumns.sort(function (a, b) {
            var aColumnIndex = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumnIndex(Lcom/google/gwt/user/cellview/client/Column;)(a);
            var bColumnIndex = controller.@org.ovirt.engine.ui.common.widget.table.ColumnController::getColumnIndex(Lcom/google/gwt/user/cellview/client/Column;)(b);
            return aColumnIndex - bColumnIndex;
        });

        // Sort menu items based on actual column index (update DOM)
        $('div', container).sort(function (a, b) {
            return $(a).data('index') - $(b).data('index');
        }).appendTo(container);
    }-*/;

    /**
     * Get an object describing given context menu item.
     */
    private native JavaScriptObject getItemInfo(Column<T, ?> column) /*-{
        var $ = $wnd.jQuery;
        var self = this;
        var container = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::container;
        var gwtColumns = self.@org.ovirt.engine.ui.common.widget.table.ColumnContextMenu::gwtColumns;

        var storedColumnIndex = gwtColumns.indexOf(column);
        var $item = $('div:nth-child(' + (storedColumnIndex + 1) + ')', container);
        var isValid = !!(storedColumnIndex >= 0 && $item.length);

        return {
            storedColumnIndex: storedColumnIndex,
            $item: $item,
            isValid: isValid
        };
    }-*/;

}
