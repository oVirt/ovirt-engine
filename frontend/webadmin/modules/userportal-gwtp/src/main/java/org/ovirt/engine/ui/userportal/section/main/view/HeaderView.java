package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractHeaderView;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationDynamicMessages;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.HeaderPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderView extends AbstractHeaderView implements HeaderPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HeaderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    HTMLPanel mainTabBarPanel;

    @UiField
    UListElement mainTabContainer;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HeaderView(ApplicationDynamicMessages dynamicMessages) {
        this.logoutLink = new Anchor(constants.logoutLinkLabel());
        this.optionsLink = new Anchor(constants.optionsLinkLabel());
        this.guideLink = new Anchor(dynamicMessages.guideLinkLabel());
        this.aboutLink = new Anchor(constants.aboutLinkLabel());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        this.logoLink.setHref(FrontendUrlUtils.getWelcomePageLink(GWT.getModuleBaseURL()));
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        mainTabContainer.appendChild(tabWidget.asWidget().getElement());
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        mainTabContainer.removeChild(tabWidget.asWidget().getElement());
    }

    @Override
    public void setMainTabPanelVisible(boolean visible) {
        mainTabBarPanel.setVisible(visible);
    }

}
