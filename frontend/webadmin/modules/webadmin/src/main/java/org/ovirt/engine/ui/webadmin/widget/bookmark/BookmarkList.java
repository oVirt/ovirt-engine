package org.ovirt.engine.ui.webadmin.widget.bookmark;

import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.action.AbstractActionStackPanelItem;
import org.ovirt.engine.ui.common.widget.action.SimpleActionPanel;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;

public class BookmarkList extends AbstractActionStackPanelItem<BookmarkModelProvider, Bookmark, CellList<Bookmark>> {

    interface WidgetUiBinder extends UiBinder<Widget, BookmarkList> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<BookmarkList> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    @UiField
    ScrollPanel scrollPanel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public BookmarkList(BookmarkModelProvider modelProvider) {
        super(modelProvider);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
        addActionButtons(modelProvider);
        addScrollEventHandler(scrollPanel);
        setProperVisibleRange();
    }

    @Override
    protected CellList<Bookmark> createDataDisplayWidget(BookmarkModelProvider modelProvider) {
        final CellList<Bookmark> display = new CellList<>(new BookmarkListItemCell());

        display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
        display.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);

        // Using KeyboardSelectionPolicy.BOUND_TO_SELECTION is preferable, but broken (see
        // gwt issue 6310).  Instead, use ENABLED and handle the keyboard input ourselves.
        // TODO-GWT is this issue now fixed?
        display.addDomHandler(new KeyDownHandler() {
            @Override
            @SuppressWarnings("unchecked")
            public void onKeyDown(KeyDownEvent event) {
                SingleSelectionModel<Bookmark> selectionModel = (SingleSelectionModel<Bookmark>) display.getSelectionModel();
                if (selectionModel.getSelectedObject() != null) {
                    Bookmark item = null;
                    int index = display.getVisibleItems().indexOf(selectionModel.getSelectedObject());
                    int key = event.getNativeEvent().getKeyCode();

                    if (key == KeyCodes.KEY_UP) {
                        item = display.getVisibleItems().get(index - 1);
                    } else if (key == KeyCodes.KEY_DOWN) {
                        item = display.getVisibleItems().get(index + 1);
                    }

                    if (item != null) {
                        selectionModel.setSelected(item, true);
                        event.stopPropagation();
                        event.preventDefault();
                    }
                }
            }
        }, KeyDownEvent.getType());
        display.sinkEvents(Event.ONKEYDOWN);

        modelProvider.addDataDisplay(display);

        return display;
    }

    @Override
    protected SimpleActionPanel<Bookmark> createActionPanel(BookmarkModelProvider modelProvider) {
        return new SimpleActionPanel<>(modelProvider, modelProvider.getSelectionModel(),
                ClientGinjectorProvider.getEventBus());
    }

    private void addActionButtons(final BookmarkModelProvider modelProvider) {
        actionPanel.addActionButton(new WebAdminButtonDefinition<Bookmark>(constants.newBookmark()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getNewCommand();
            }
        });

        actionPanel.addActionButton(new WebAdminButtonDefinition<Bookmark>(constants.editBookmark()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getEditCommand();
            }
        });

        actionPanel.addActionButton(new WebAdminButtonDefinition<Bookmark>(constants.removeBookmark()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });
    }

    void addScrollEventHandler(final ScrollPanel scrollPanel) {
        scrollPanel.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                if (scrollPanel.getVerticalScrollPosition() >= scrollPanel.getMaximumVerticalScrollPosition()) {
                    setProperVisibleRange();
                }
            }
        });
    }

    private void setProperVisibleRange() {
        // Extend the visible range of data display widget
        Range visibleRange = getDataDisplayWidget().getVisibleRange();
        getDataDisplayWidget().setVisibleRange(
                visibleRange.getStart(),
                visibleRange.getLength() + getDataDisplayWidget().getPageSize());
    }
}
