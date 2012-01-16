package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class ConfigurePopupPresenterWidget extends PresenterWidget<ConfigurePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends PopupView {
        HasClickHandlers getCloseButton();
    }

    private RoleModelProvider roleModelProvider;
    private RolePermissionModelProvider permissionModelProvider;
    private SystemPermissionModelProvider systemPermissionModelProvider;

    @Inject
    public ConfigurePopupPresenterWidget(EventBus eventBus,
            ViewDef view, ClientGinjector ginjector,
            RoleModelProvider roleModelProvider,
            RolePermissionModelProvider permissionModelProvider,
            SystemPermissionModelProvider systemPermissionModelProvider) {
        super(eventBus, view);
        this.roleModelProvider = roleModelProvider;
        this.permissionModelProvider = permissionModelProvider;
        this.systemPermissionModelProvider = systemPermissionModelProvider;
    }

    @Override
    protected void onBind() {
        super.onBind();

        roleModelProvider.getModel().Search();
        systemPermissionModelProvider.getModel().Search();
        registerHandler(getView().getCloseButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().hide();
            }
        }));
    }

    @Override
    protected void onHide() {
        super.onHide();
        roleModelProvider.getModel().EnsureAsyncSearchStopped();
        permissionModelProvider.getModel().EnsureAsyncSearchStopped();
        systemPermissionModelProvider.getModel().EnsureAsyncSearchStopped();
    }
}
