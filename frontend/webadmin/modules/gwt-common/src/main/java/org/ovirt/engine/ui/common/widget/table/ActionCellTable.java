package org.ovirt.engine.ui.common.widget.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ProvidesKey;

public class ActionCellTable<T> extends ElementIdCellTable<T> {

    private static final int DEFAULT_PAGESIZE = 1000;
    // Magic number determined by trial and error. For some reason if the grid goes all the way to the bottom
    // of the page, a secondary scroll bar appears in all browsers. This is the minimum needed to avoid that
    // extra scroll bar.
    private static final int GRID_SUBTRACT = 17;

    private static final Resources DEFAULT_RESOURCES = GWT.create(Resources.class);

    public ActionCellTable(ProvidesKey<T> keyProvider, Resources resources) {
        this(keyProvider, resources, ROW_HEIGHT + Unit.PX.getType());
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
        isHeightSet = true;
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
