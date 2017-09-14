package org.ovirt.engine.ui.common.widget.table;

import java.util.List;

import org.ovirt.engine.ui.common.widget.WindowHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

public class ActionCellTable<T> extends ElementIdCellTable<T> {

    private static final int DEFAULT_PAGESIZE = 1000;
    // Magic number determined by trial and error. For some reason if the grid goes all the way to the bottom
    // of the page, a secondary scroll bar appears in all browsers. This is the minimum needed to avoid that
    // extra scroll bar.
    private static final int GRID_SUBTRACT = 7;
    // The height of the header + 1 empty row.
    private static final int NO_ITEMS_HEIGHT = 55;
    private static final int LOADING_HEIGHT = 96;

    private static final Resources DEFAULT_RESOURCES = GWT.create(Resources.class);

    private static final int scrollbarThickness = WindowHelper.determineScrollbarThickness();

    private boolean useFullHeight = false;
    private boolean heightSet = false;

    public ActionCellTable(ProvidesKey<T> keyProvider, Resources resources) {
        this(keyProvider, resources, NO_ITEMS_HEIGHT + Unit.PX.getType());
    }

    public ActionCellTable(ProvidesKey<T> keyProvider, Resources resources, String height) {
        super(DEFAULT_PAGESIZE, resources != null ? resources : DEFAULT_RESOURCES, keyProvider,
                createDefaultLoadingIndicator(resources != null ? resources : DEFAULT_RESOURCES));
        super.setHeight(height);
    }

    public ActionCellTable(Resources resources) {
        super(DEFAULT_PAGESIZE, resources != null ? resources : DEFAULT_RESOURCES);
    }

    /**
     * This method makes the DataGrid take up the entire space of the content or the height of the screen
     * minus the height of stuff above it. This is useful for main content grids and detail tab grids, but NOT
     * for grids in pop-up dialogs.
     */
    public void enableFullHeight() {
        useFullHeight = true;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        updateGridSize();
        redraw();
    }

    private void resizeGridToFullHeight() {
        int top = getAbsoluteTop();
        int windowHeight = Window.getClientHeight();
        int contentHeight = this.getTableBodyElement().getOffsetHeight();
        if (contentHeight == 0) {
            contentHeight = this.getLoadingIndicator() != null ? LOADING_HEIGHT : NO_ITEMS_HEIGHT;
        } else {
            contentHeight += getGridHeaderHeight();
        }
        if (isHorizontalScrollbarVisible()) {
            contentHeight += scrollbarThickness;
        }
        // This is to prevent scrolling in the grid without a visible scrollbar. The 3 works in FF and leaves a 1px
        // gap in chrome. 2 is fine in Chrome but leaves the scrolling in FF.
        contentHeight += 3;

        int maxGridHeight = windowHeight - top;
        maxGridHeight -= GRID_SUBTRACT;
        if (top > 0 && top < windowHeight) {
            super.setHeight(Math.min(maxGridHeight, contentHeight) + Unit.PX.getType());
        }
        redraw();
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        redraw();
        heightSet = true;
    }

    private boolean isHorizontalScrollbarVisible() {
        int tableScrollWidth = this.getTableBodyElement().getScrollWidth();
        return tableScrollWidth != this.getElement().getScrollWidth() && tableScrollWidth != 0;
    }

    private void resizeGridToContentHeight() {
        int contentHeight = this.getTableBodyElement().getOffsetHeight();
        if (contentHeight == 0) {
            contentHeight = NO_ITEMS_HEIGHT;
        } else {
            contentHeight += getGridHeaderHeight();
        }
        if (contentHeight > 0) {
            super.setHeight(contentHeight + Unit.PX.getType());
        }
        redraw();
    }

    private int getGridHeaderHeight() {
        return this.getTableHeadElement().getOffsetHeight();
    }

    @Override
    public void setRowData(int start, final List<? extends T> values) {
        super.setRowData(start, values);
        updateGridSize();
    }

    public void updateGridSize() {
        Scheduler.get().scheduleDeferred(() -> {
            if (!heightSet) {
                if (useFullHeight) {
                    resizeGridToFullHeight();
                } else {
                    resizeGridToContentHeight();
                }
            }
        });
    }

    public void setLoadingState(LoadingState state) {
        super.onLoadingStateChanged(state);
    }

    protected static Widget createDefaultLoadingIndicator(Resources resources) {
        ImageResource loadingImg = resources.dataGridLoading();
        Image image;
        if (loadingImg == null) {
            image = new Image();
        } else {
            image = new Image(loadingImg);
        }
        image.getElement().getStyle().setMarginTop(30.0, Unit.PX);
        return image;
    }
}
