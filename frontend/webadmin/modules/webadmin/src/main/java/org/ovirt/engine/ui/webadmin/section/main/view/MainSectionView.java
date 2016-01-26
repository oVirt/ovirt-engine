package org.ovirt.engine.ui.webadmin.section.main.view;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabBarOffsetUiHandlers;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;
import org.ovirt.engine.ui.webadmin.widget.bookmark.BookmarkList;
import org.ovirt.engine.ui.webadmin.widget.footer.AlertsEventsFooterView;
import org.ovirt.engine.ui.webadmin.widget.main.TabbedSplitLayoutPanel;
import org.ovirt.engine.ui.webadmin.widget.tags.TagList;
import org.ovirt.engine.ui.webadmin.widget.tree.SystemTree;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.name.Named;

public class MainSectionView extends AbstractView implements MainSectionPresenter.ViewDef {

    private static final int TREE_INDEX = 0;
    private static final int BOOKMARK_INDEX = 1;
    private static final int TAG_INDEX = 2;
    private static final int SPLITTER_THICKNESS = 4;
    private static final int SECTION_HEADER_HEIGHT = 30;

    interface ViewUiBinder extends UiBinder<Widget, MainSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<MainSectionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private MainTabBarOffsetUiHandlers uiHandlers;

    @UiField
    SimplePanel headerPanel;

    @UiField(provided = true)
    final StackLayoutPanel westStackPanel;

    @UiField
    LayoutPanel mainContentPanel;

    @UiField
    SimplePanel alertEventFooterPanel;

    @UiField(provided = true)
    SplitLayoutPanel verticalSplitLayoutPanel;

    @UiField(provided = true)
    SplitLayoutPanel horizontalSplitLayoutPanel;

    @WithElementId
    Label treeHeader;

    @WithElementId
    Label bookmarksHeader;

    @WithElementId
    Label tagsHeader;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainSectionView(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider,
            TagModelProvider tagModelProvider,
            AlertModelProvider alertModelProvider,
            @Named("footer") EventModelProvider eventModelProvider,
            TaskModelProvider taskModelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        westStackPanel = createWestStackPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider);

        verticalSplitLayoutPanel = new SplitLayoutPanel(SPLITTER_THICKNESS);
        horizontalSplitLayoutPanel = new TabbedSplitLayoutPanel(SPLITTER_THICKNESS, clientStorage);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initHeaders();
        ViewIdHandler.idHandler.generateAndSetIds(this);

        addContentToWestPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider, westStackPanel);

        initAlertEventFooterPanel(alertModelProvider,
                eventModelProvider,
                taskModelProvider,
                eventBus,
                clientStorage);
        headerPanel.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
        //Enable double clicking to collapse/expand the stack panel (with the treeview).
        horizontalSplitLayoutPanel.setWidgetToggleDisplayAllowed(westStackPanel, true);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                //Manually call onResize() so the tabs at the top are positioned correctly. For some reason
                //doing setWidgetSize doesn't trigger the onResize event. Also this need to be deferred
                //otherwise the handlers haven't been added yet, and the resize won't do anything.
                westStackPanel.onResize();
            }
        });
    }

    private void initHeaders() {
        treeHeader = new Label(constants.systemMainSection());
        bookmarksHeader = new Label(constants.bookmarksMainSection());
        tagsHeader = new Label(constants.tagsMainSection());
    }

    StackLayoutPanel createWestStackPanel(final SystemTreeModelProvider treeModelProvider,
            final BookmarkModelProvider bookmarkModelProvider, final TagModelProvider tagModelProvider) {
        final StackLayoutPanel panel = new StackLayoutPanel(Unit.PX) {
            @Override
            public void onResize() {
                super.onResize();
                Double westStackWidth = horizontalSplitLayoutPanel.getWidgetSize(westStackPanel);
                if (uiHandlers != null && westStackWidth != null) {
                    uiHandlers.setMainTabBarOffset(westStackWidth.intValue());
                }
            }
        };
        panel.addSelectionHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                if (event == null) {
                    return;
                }
                treeModelProvider.getModel().setSearchString(StringUtils.EMPTY);
                treeModelProvider.getModel().refresh();
                switch(event.getSelectedItem()) {
                    case TREE_INDEX:
                        bookmarkModelProvider.getModel().stopRefresh();
                        tagModelProvider.getModel().stopRefresh();
                        // Reset system tree to the root item.
                        treeModelProvider.getModel().getResetCommand().execute();
                        break;
                    case BOOKMARK_INDEX:
                        treeModelProvider.getModel().stopRefresh();
                        tagModelProvider.getModel().stopRefresh();
                        bookmarkModelProvider.getModel().executeBookmarksSearch();
                        break;
                    case TAG_INDEX:
                        treeModelProvider.getModel().stopRefresh();
                        bookmarkModelProvider.getModel().stopRefresh();
                        break;
                }
            }
        });

        return panel;
    }

    private void addContentToWestPanel(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider,
            TagModelProvider tagModelProvider,
            final StackLayoutPanel panel) {
        panel.insert(new SystemTree(treeModelProvider), treeHeader, SECTION_HEADER_HEIGHT, panel.getWidgetCount());
        panel.insert(new BookmarkList(bookmarkModelProvider), bookmarksHeader, SECTION_HEADER_HEIGHT, panel.getWidgetCount());
        panel.insert(new TagList(tagModelProvider), tagsHeader, SECTION_HEADER_HEIGHT, panel.getWidgetCount());
    }

    void initAlertEventFooterPanel(AlertModelProvider alertModelProvider,
            EventModelProvider eventModelProvider,
            TaskModelProvider taskModelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        alertEventFooterPanel.add(new AlertsEventsFooterView(alertModelProvider, eventModelProvider, taskModelProvider,
                eventBus, clientStorage));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainSectionPresenter.TYPE_SetHeader) {
            setPanelContent(headerPanel, content);
        } else if (slot == MainSectionPresenter.TYPE_SetMainContent) {
            setPanelContent(mainContentPanel, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setUiHandlers(MainTabBarOffsetUiHandlers uiHandlers) {
        this.uiHandlers = uiHandlers;
    }

}
