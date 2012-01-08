package org.ovirt.engine.ui.webadmin.section.main.view;

import javax.inject.Inject;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabBarOffsetUiHandlers;
import org.ovirt.engine.ui.webadmin.section.main.view.ApplicationFocusChangeEvent.ApplicationFocusChangeHandler;
import org.ovirt.engine.ui.webadmin.system.InternalConfiguration;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.view.AbstractView;
import org.ovirt.engine.ui.webadmin.widget.bookmark.BookmarkList;
import org.ovirt.engine.ui.webadmin.widget.footer.AlertsEventsFooterView;
import org.ovirt.engine.ui.webadmin.widget.tags.TagList;
import org.ovirt.engine.ui.webadmin.widget.tree.SystemTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
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

    private final EventBus eventBus;

    private final SystemTreeModelProvider treeModelProvider;

    private final AlertModelProvider alertModelProvider;

    private final EventModelProvider eventModelProvider;

    @Inject
    public MainSectionView(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider,
            TagModelProvider tagModelProvider,
            AlertModelProvider alertModelProvider,
            AlertFirstRowModelProvider alertFirstRowModelProvider,
            EventModelProvider eventModelProvider,
            EventFirstRowModelProvider eventFirstRowModelProvider,
            InternalConfiguration intConf,
            ApplicationResources resources,
            ApplicationTemplates templates,
            ApplicationMessages messages,
            ApplicationConstants constants,
            EventBus eventBus) {
        attachWindowFocusEvents();
        this.eventBus = eventBus;
        this.treeModelProvider = treeModelProvider;
        this.alertModelProvider = alertModelProvider;
        this.eventModelProvider = eventModelProvider;
        this.westStackPanel = createWestStackPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider);

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
        lastEventWasBlur = false;
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

    @Override
    public HandlerRegistration addApplicationFocusChangeHandler(ApplicationFocusChangeHandler handler) {
        return eventBus.addHandler(ApplicationFocusChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    private JavaScriptObject activeElement;

    private boolean lastEventWasBlur;

    public void onWindowFocus() {
        GWT.log("onWindowFocus() called");
        ApplicationFocusChangeEvent.fire(this, true);
        alertModelProvider.getModel().toForground();
        eventModelProvider.getModel().toForground();
        treeModelProvider.getModel().toForground();
    }

    public void onWindowBlur() {
        GWT.log("onWindowBlur() called");
        ApplicationFocusChangeEvent.fire(this, false);
        alertModelProvider.getModel().toBackground();
        eventModelProvider.getModel().toBackground();
        treeModelProvider.getModel().toBackground();
    }

    native void attachWindowFocusEvents() /*-{
                                          var clientAgentType = @org.ovirt.engine.ui.webadmin.uicommon.ClientAgentType::new()();
                                          var browser = clientAgentType.@org.ovirt.engine.ui.webadmin.uicommon.ClientAgentType::browser;
                                          var isIE = browser.toLowerCase() == "explorer";

                                          if (isIE) {
                                          $doc.attachEvent("onfocusin", onFocus);
                                          $doc.attachEvent("onfocusout", onBlur);
                                          } else {
                                          $wnd.addEventListener("focus", onFocus, false);
                                          $wnd.addEventListener("blur", onBlur, false);
                                          }

                                          var context = this;
                                          function onFocus() {
                                          // only focus if previous event was a blur or we get lots of focus events (On IE)
                                          if (context.@org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView::lastEventWasBlur) {
                                          context.@org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView::lastEventWasBlur = false;
                                          context.@org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView::onWindowFocus()();
                                          }
                                          }
                                          function onBlur() {
                                          debugger;
                                          if (context.@org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView::activeElement != $doc.activeElement) {
                                          context.@org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView::activeElement = $doc.activeElement;
                                          } else {
                                          context.@org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView::lastEventWasBlur = true;
                                          context.@org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView::onWindowBlur()();
                                          }
                                          }
                                          }-*/;

}
