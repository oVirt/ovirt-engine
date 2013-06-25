package org.ovirt.engine.ui.webadmin.section.main.view;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabBarOffsetUiHandlers;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;
import org.ovirt.engine.ui.webadmin.widget.bookmark.BookmarkList;
import org.ovirt.engine.ui.webadmin.widget.footer.AlertsEventsFooterView;
import org.ovirt.engine.ui.webadmin.widget.tags.TagList;
import org.ovirt.engine.ui.webadmin.widget.tree.SystemTree;

import com.google.gwt.core.client.GWT;
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

public class MainSectionView extends AbstractView implements MainSectionPresenter.ViewDef {

    private static final int BOOKMARK_INDEX = 1;

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

    @Inject
    public MainSectionView(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider,
            TagModelProvider tagModelProvider,
            AlertModelProvider alertModelProvider,
            AlertFirstRowModelProvider alertFirstRowModelProvider,
            EventModelProvider eventModelProvider,
            EventFirstRowModelProvider eventFirstRowModelProvider,
            TaskModelProvider taskModelProvider,
            TaskFirstRowModelProvider taskFirstRowModelProvider,
            ApplicationConstants constants,
            ApplicationResources resources,
            ApplicationTemplates templates,
            EventBus eventBus,
            ClientStorage clientStorage, CommonApplicationConstants commonConstants) {
        westStackPanel = createWestStackPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider);

        verticalSplitLayoutPanel = new SplitLayoutPanel(4);
        horizontalSplitLayoutPanel = new SplitLayoutPanel(4);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initHeaders(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);

        addContentToWestPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider, westStackPanel, constants);

        initAlertEventFooterPanel(alertModelProvider,
                alertFirstRowModelProvider,
                eventModelProvider,
                eventFirstRowModelProvider,
                taskModelProvider,
                taskFirstRowModelProvider,
                resources,
                templates,
                eventBus,
                clientStorage,
                constants);
        headerPanel.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
    }

    private void initHeaders(ApplicationConstants constants) {
        treeHeader = new Label(constants.systemMainSection());
        bookmarksHeader = new Label(constants.bookmarksMainSection());
        tagsHeader = new Label(constants.tagsMainSection());
    }

    StackLayoutPanel createWestStackPanel(SystemTreeModelProvider treeModelProvider,
            final BookmarkModelProvider bookmarkModelProvider, TagModelProvider tagModelProvider) {
        final StackLayoutPanel panel = new StackLayoutPanel(Unit.PX) {
            @Override
            public void onResize() {
                super.onResize();

                if (uiHandlers != null) {
                    uiHandlers.setMainTabBarOffset(getOffsetWidth());
                }
            }
        };
        panel.addSelectionHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                if (event == null) {
                    return;
                }
                if (event.getSelectedItem() == BOOKMARK_INDEX) {
                    bookmarkModelProvider.getModel().executeBookmarksSearch();
                } else {
                    bookmarkModelProvider.getModel().stopRefresh();
                }
            }
        });

        return panel;
    }

    private void addContentToWestPanel(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider,
            TagModelProvider tagModelProvider,
            final StackLayoutPanel panel, ApplicationConstants constants) {
        panel.insert(new SystemTree(treeModelProvider, constants), treeHeader, 26, panel.getWidgetCount());
        panel.insert(new BookmarkList(bookmarkModelProvider, constants), bookmarksHeader, 26, panel.getWidgetCount());
        panel.insert(new TagList(tagModelProvider, constants), tagsHeader, 26, panel.getWidgetCount());
    }

    void initAlertEventFooterPanel(AlertModelProvider alertModelProvider,
            AlertFirstRowModelProvider alertFirstRowModelProvider,
            EventModelProvider eventModelProvider,
            EventFirstRowModelProvider eventFirstRowModelProvider,
            TaskModelProvider taskModelProvider,
            TaskFirstRowModelProvider taskFirstRowModelProvider,
            ApplicationResources resources,
            ApplicationTemplates templates,
            EventBus eventBus,
            ClientStorage clientStorage, ApplicationConstants constants) {
        alertEventFooterPanel.add(new AlertsEventsFooterView(
                alertModelProvider, alertFirstRowModelProvider,
                eventModelProvider, eventFirstRowModelProvider,
                taskModelProvider, taskFirstRowModelProvider,
                resources, templates, eventBus, clientStorage, constants));
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
