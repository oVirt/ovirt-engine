package org.ovirt.engine.ui.common.widget.table;

import java.util.List;

import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.widget.WindowHelper;

import com.google.gwt.core.client.GWT;
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
    private static final int GRID_SUBTRACT = 17;
    // The height of the header + 1 empty row.
    private static final int NO_ITEMS_HEIGHT = 55;
    private static final int LOADING_HEIGHT = 96;

    private static final int CHROME_HEIGHT_ADJUST = 2;
    private static final int FF_HEIGHT_ADJUST = 3;
    private static final int IE_HEIGHT_ADJUST = 3;

    // The height of a row of data in the grid. I wish I could dynamically detect this.
    private static final int ROW_HEIGHT = 26;

    private static final Resources DEFAULT_RESOURCES = GWT.create(Resources.class);

    private static final int scrollbarThickness = WindowHelper.determineScrollbarThickness();
    private static final ClientAgentType clientAgentType = new ClientAgentType();

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

    @Override
    protected void onLoad() {
        super.onLoad();
        updateGridSize();
    }

    public void setHeight(String height) {
        super.setHeight(height);
        redraw();
        heightSet = true;
    }

    private boolean isHorizontalScrollbarVisible() {
        int tableScrollWidth = this.getTableBodyElement().getScrollWidth();
        return tableScrollWidth != this.getElement().getScrollWidth() && tableScrollWidth != 0;
    }

    private void resizeGridToContentHeight(int height) {
        int top = getAbsoluteTop();
        int maxGridHeight = Window.getClientHeight() - top - GRID_SUBTRACT;
        int contentHeight = determineBrowserHeightAdjustment(height);
        if (contentHeight > maxGridHeight) {
            contentHeight = maxGridHeight;
        }
        if (contentHeight > 0) {
            super.setHeight(contentHeight + Unit.PX.getType());
        }
        redraw();
    }

    public int determineBrowserHeightAdjustment(int height) {
        int contentHeight = height;
        if (clientAgentType.isFirefox()) {
            contentHeight += FF_HEIGHT_ADJUST;
        } else if(clientAgentType.isIE()) {
            contentHeight += IE_HEIGHT_ADJUST;
        } else {
            contentHeight += CHROME_HEIGHT_ADJUST;
        }
        if (isHorizontalScrollbarVisible()) {
            contentHeight += scrollbarThickness;
        }
        return contentHeight;
    }

    private int getGridHeaderHeight() {
        return this.getTableHeadElement().getOffsetHeight();
    }

    @Override
    public void setRowData(int start, final List<? extends T> values) {
        super.setRowData(start, values);
        updateGridSize(values.size() * ROW_HEIGHT);
    }

    public void updateGridSize() {
        int rowCount = getRowCount();
        int height = getLoadingIndicator() != null ? LOADING_HEIGHT : NO_ITEMS_HEIGHT;
        if (rowCount > 0) {
            height = rowCount * ROW_HEIGHT;
        }
        updateGridSize(height);
    }

    public void updateGridSize(int height) {
        if (!heightSet) {
            resizeGridToContentHeight(height + getGridHeaderHeight());
        }
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
