package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.ClusterPolicyView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class ConfigurePopupView extends AbstractPopupView<SimpleDialogPanel> implements ConfigurePopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ConfigurePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Label titleLabel;

    @UiField
    PushButton closeButton;

    @UiField
    DialogTab rolesTab;

    @UiField
    DialogTab clusterPoliciesTab;

    @UiField
    DialogTab systemPermissionsTab;

    @UiField
    SimplePanel rolesTabPanel;

    @UiField
    SimplePanel clusterPoliciesTabPanel;

    @UiField
    SimplePanel systemPermissionTabPanel;

    @Inject
    public ConfigurePopupView(ClientGinjector ginjector,
            EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            RoleView roleView,
            ClusterPolicyView clusterPolicyView,
            SystemPermissionView systemPermissionView) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        roleView.setWidth("100%"); //$NON-NLS-1$
        rolesTabPanel.add(roleView);
        clusterPolicyView.setWidth("100%"); //$NON-NLS-1$
        clusterPoliciesTabPanel.add(clusterPolicyView);
        systemPermissionTabPanel.setWidth("100%"); //$NON-NLS-1$
        systemPermissionTabPanel.add(systemPermissionView);
    }

    void localize(ApplicationConstants constants) {
        titleLabel.setText(constants.configurePopupTitle());
        closeButton.setText(constants.closeButtonLabel());

        rolesTab.setLabel(constants.configureRoleTabLabel());
        clusterPoliciesTab.setLabel(constants.configureClusterPolicyTabLabel());
        systemPermissionsTab.setLabel(constants.configureSystemPermissionTabLabel());
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

    @Override
    public HasClickHandlers getCloseIconButton() {
        return asWidget().getCloseIconButton();
    }

    @Override
    public void setPopupKeyPressHandler(PopupNativeKeyPressHandler handler) {
        asWidget().setKeyPressHandler(handler);
    }

    @Override
    public void hideClusterPolicyTab() {
        clusterPoliciesTab.setVisible(false);
    }

}
