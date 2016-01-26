package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import org.ovirt.engine.ui.common.view.AbstractPopupView;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.instancetypes.InstanceTypesView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool.SharedMacPoolView;
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
    DialogTab instanceTypesTab;

    @UiField
    DialogTab macPoolsTab;

    @UiField
    SimplePanel rolesTabPanel;

    @UiField
    SimplePanel clusterPoliciesTabPanel;

    @UiField
    SimplePanel systemPermissionTabPanel;

    @UiField
    SimplePanel instanceTypesTabPanel;

    @UiField
    SimplePanel macPoolsTabPanel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ConfigurePopupView(
            EventBus eventBus,
            RoleView roleView,
            ClusterPolicyView clusterPolicyView,
            SystemPermissionView systemPermissionView,
            InstanceTypesView instanceTypesView,
            SharedMacPoolView sharedMacPoolView) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();

        roleView.setWidth("100%"); //$NON-NLS-1$
        roleView.addStyleName("cpv_roleView_pfly_fix"); //$NON-NLS-1$
        rolesTabPanel.add(roleView);

        clusterPolicyView.setWidth("100%"); //$NON-NLS-1$
        clusterPoliciesTabPanel.add(clusterPolicyView);

        systemPermissionTabPanel.setWidth("100%"); //$NON-NLS-1$
        systemPermissionTabPanel.add(systemPermissionView);

        instanceTypesView.setWidth("100%"); //$NON-NLS-1$
        instanceTypesTabPanel.add(instanceTypesView);

        macPoolsTabPanel.add(sharedMacPoolView);
    }

    void localize() {
        titleLabel.setText(constants.configurePopupTitle());
        closeButton.setText(constants.closeButtonLabel());

        rolesTab.setLabel(constants.configureRoleTabLabel());
        clusterPoliciesTab.setLabel(constants.configureClusterPolicyTabLabel());
        systemPermissionsTab.setLabel(constants.configureSystemPermissionTabLabel());
        instanceTypesTab.setLabel(constants.instanceTypes());
        macPoolsTab.setLabel(constants.configureMacPoolsTabLabel());
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
