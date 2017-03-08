package org.ovirt.engine.ui.userportal.section.main.view;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractHeaderView;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.HeaderPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderView extends AbstractHeaderView implements HeaderPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HeaderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HeaderView(ApplicationDynamicMessages dynamicMessages) {
        this.logoutLink = new AnchorListItem(constants.logoutLinkLabel());
        this.optionsLink = new AnchorListItem(constants.optionsLinkLabel());
        this.guideLink = new AnchorListItem(dynamicMessages.guideLinkLabel());
        this.aboutLink = new AnchorListItem(constants.aboutLinkLabel());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        this.logoLink.setHref(FrontendUrlUtils.getWelcomePageLink(GWT.getModuleBaseURL()));
    }

    @Override
    public void addTabWidget(TabDefinition tab, int index) {
    }

    @Override
    public void removeTabWidget(TabDefinition tab) {
    }

    @Override
    public void setMainTabPanelVisible(boolean visible) {
    }

    @Override
    public void addTab(String title, String hashTag, int index) {
    }

    @Override
    public void removeTab(String title, String hashTag) {
    }

    @Override
    public void updateTab(String title, String hashTag, boolean accessible) {
    }

    @Override
    public void updateTab(TabDefinition tab) {
    }

    @Override
    public void markActiveTab(String title, String hashTag) {
    }

    @Override
    public void setActiveTab(TabDefinition tab) {
    }
}
