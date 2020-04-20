package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.table.PagingDataProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

public class PaginationControl extends Composite {

    private static final PagingDataProvider defaultDataProvider = new PagingDataProvider() {
        @Override
        public int getFirstItemOnPage() {
            return -1;
        }

        @Override
        public int getLastItemOnPage() {
            return -1;
        }

        @Override
        public int getTotalItemsCount() {
            return -1;
        }

        @Override
        public boolean canGoForward() {
            return false;
        }

        @Override
        public boolean canGoBack() {
            return false;
        }

        @Override
        public void goForward() {
        }

        @Override
        public void goBack() {
        }

        @Override
        public void refresh() {
        }
    };

    interface WidgetUiBinder extends UiBinder<FlowPanel, PaginationControl> {
        PaginationControl.WidgetUiBinder uiBinder = GWT.create(PaginationControl.WidgetUiBinder.class);
    }

    @UiField
    @WithElementId
    Button prevPageButton;

    @UiField
    @WithElementId
    Button nextPageButton;

    @UiField
    HTMLPanel itemsCurrent;

    private PagingDataProvider dataProvider;

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    public PaginationControl() {
        initWidget(PaginationControl.WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void updateTableControls() {
        // keep the current convention
        // "0 - 0" is displayed when there is no data
        int from = 0;
        int to = 0;

        if (getDataProvider().getFirstItemOnPage() <= getDataProvider().getLastItemOnPage() &&
                getDataProvider().getFirstItemOnPage() >= 0) {
            // switch to one-based paging
            from = getDataProvider().getFirstItemOnPage() + 1;
            to = getDataProvider().getLastItemOnPage() + 1;
        }

        itemsCurrent.getElement().setInnerText(buildMessage(from, to, getDataProvider().getTotalItemsCount()));

        prevPageButton.setEnabled(getDataProvider().canGoBack());
        nextPageButton.setEnabled(getDataProvider().canGoForward());
    }

    private String buildMessage(int from, int to, int total) {
        if (total > 0) {
            return messages.fromIndexToIndexOfTotalCount(from, to, total);
        }
        return messages.fromIndexToIndex(from, to);
    }

    private PagingDataProvider getDataProvider() {
        return dataProvider == null ? defaultDataProvider : dataProvider;
    }

    public void setDataProvider(PagingDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        updateTableControls();
    }

    @UiHandler("prevPageButton")
    public void handlePrevPageButtonClick(ClickEvent event) {
        getDataProvider().goBack();
        updateTableControls();
    }

    @UiHandler("nextPageButton")
    public void handleNextPageButtonClick(ClickEvent event) {
        getDataProvider().goForward();
        updateTableControls();
    }

}
