package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractSingleSlotView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderView extends AbstractSingleSlotView implements HeaderPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HeaderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final int mainTabBarInitialOffset = 240;

    @UiField
    @WithElementId("userName")
    InlineLabel userNameLabel;

    @UiField
    @Ignore
    Label headerLabel;

    @UiField(provided = true)
    @WithElementId
    final Anchor logoutLink;

    @UiField(provided = true)
    @WithElementId
    final Anchor configureLink;

    @UiField(provided = true)
    @WithElementId
    final Anchor aboutLink;

    @UiField(provided = true)
    @WithElementId
    final Anchor guideLink;

    @UiField
    SimplePanel searchPanelContainer;

    @UiField
    HTMLPanel mainTabBarPanel;

    @UiField
    FlowPanel mainTabContainer;

    @Inject
    public HeaderView(ApplicationConstants constants,
            ApplicationDynamicMessages dynamicMessages) {
        this.configureLink = new Anchor(constants.configureLinkLabel());
        this.logoutLink = new Anchor(constants.logoutLinkLabel());
        this.aboutLink = new Anchor(constants.aboutLinkLabel());
        this.guideLink = new Anchor(constants.guideLinkLabel());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        mainTabBarPanel.getElement().getStyle().setZIndex(1);

        // Ensure proper main tab bar position
        setMainTabBarOffset(mainTabBarInitialOffset);
        localize(dynamicMessages);
    }

    private void localize(ApplicationDynamicMessages dynamicMessages) {
        headerLabel.setText(dynamicMessages.mainHeaderLabel());
    }

    @Override
    protected Object getContentSlot() {
        return HeaderPresenterWidget.TYPE_SetSearchPanel;
    }

    @Override
    protected void setContent(Widget content) {
        setPanelContent(searchPanelContainer, content);
    }

    @Override
    public void addTabWidget(Widget tabWidget, int index) {
        mainTabContainer.insert(tabWidget, index);
    }

    @Override
    public void removeTabWidget(Widget tabWidget) {
        mainTabContainer.getElement().removeChild(tabWidget.getElement());
    }

    @Override
    public void setMainTabBarOffset(int left) {
        mainTabBarPanel.getElement().getStyle().setLeft(left, Unit.PX);
        mainTabBarPanel.getElement().getStyle().setWidth(Window.getClientWidth() - left, Unit.PX);
    }

    @Override
    public void setUserName(String userName) {
        userNameLabel.setText(userName);
    }

    @Override
    public HasClickHandlers getLogoutLink() {
        return logoutLink;
    }

    @Override
    public HasClickHandlers getAboutLink() {
        return aboutLink;
    }

    @Override
    public HasClickHandlers getGuideLink() {
        return guideLink;
    }

    @Override
    public HasClickHandlers getConfigureLink() {
        return configureLink;
    }

    @Override
    public void setGuideLinkEnabled(boolean enabled) {
        guideLink.getElement().getStyle().setCursor(enabled ? Cursor.POINTER : Cursor.DEFAULT);
    }

}
