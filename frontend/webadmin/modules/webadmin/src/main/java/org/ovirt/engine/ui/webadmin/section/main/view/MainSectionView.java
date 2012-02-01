package org.ovirt.engine.ui.webadmin.section.main.view;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabBarOffsetUiHandlers;
import org.ovirt.engine.ui.webadmin.system.InternalConfiguration;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.widget.bookmark.BookmarkList;
import org.ovirt.engine.ui.webadmin.widget.footer.AlertsEventsFooterView;
import org.ovirt.engine.ui.webadmin.widget.tags.TagList;
import org.ovirt.engine.ui.webadmin.widget.tree.SystemTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainSectionView extends AbstractView implements MainSectionPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<MainSectionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private MainTabBarOffsetUiHandlers uiHandlers;

    @UiField
    DockLayoutPanel wrapperLayoutPanel;

    @UiField
    SimplePanel headerPanel;

    @UiField(provided = true)
    final StackLayoutPanel westStackPanel;

    @UiField
    LayoutPanel mainContentPanel;

    @UiField
    SimplePanel alertEventFooterPanel;

    @UiField
    Label footerMessage;

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
            InternalConfiguration intConf,
            ApplicationConstants constants,
            ApplicationMessages messages,
            ApplicationResources resources,
            ApplicationTemplates templates) {
        westStackPanel = createWestStackPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initHeaders();
        ViewIdHandler.idHandler.generateAndSetIds(this);

        addContentToWestPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider, westStackPanel, constants);

        initAlertEventFooterPanel(alertModelProvider, alertFirstRowModelProvider,
                eventModelProvider, eventFirstRowModelProvider, resources, templates);
        headerPanel.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);

        if (!intConf.isCurrentBrowserSupported()) {
            // Browser is not supported
            footerMessage.setText(messages.browserNotSupportedVersion(
                    intConf.getCurrentBrowser(),
                    intConf.getCurrentBrowserVersion()));
        } else {
            // Remove footer message
            wrapperLayoutPanel.remove(footerMessage);
        }
    }

    private void initHeaders() {
        treeHeader = new Label("Tree");
        bookmarksHeader = new Label("Bookmarks");
        tagsHeader = new Label("Tags");
    }

    StackLayoutPanel createWestStackPanel(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider, TagModelProvider tagModelProvider) {
        final StackLayoutPanel panel = new StackLayoutPanel(Unit.PX) {
            @Override
            public void onResize() {
                super.onResize();

                if (uiHandlers != null) {
                    uiHandlers.setMainTabBarOffset(getOffsetWidth());
                }
            }
        };

        return panel;
    }

    private void addContentToWestPanel(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider,
            TagModelProvider tagModelProvider,
            final StackLayoutPanel panel, ApplicationConstants constants) {
        panel.insert(new SystemTree(treeModelProvider, constants), treeHeader, 26, panel.getWidgetCount());
        panel.insert(new BookmarkList(bookmarkModelProvider), bookmarksHeader, 26, panel.getWidgetCount());
        panel.insert(new TagList(tagModelProvider), tagsHeader, 26, panel.getWidgetCount());
    }

    void initAlertEventFooterPanel(AlertModelProvider alertModelProvider,
            AlertFirstRowModelProvider alertFirstRowModelProvider,
            EventModelProvider eventModelProvider,
            EventFirstRowModelProvider eventFirstRowModelProvider,
            ApplicationResources resources,
            ApplicationTemplates templates) {
        alertEventFooterPanel.add(new AlertsEventsFooterView(
                alertModelProvider, alertFirstRowModelProvider,
                eventModelProvider, eventFirstRowModelProvider,
                resources, templates));
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
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
