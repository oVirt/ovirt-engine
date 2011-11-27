package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure;

import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;

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

    @Inject
    public ConfigurePopupPresenterWidget(EventBus eventBus,
            ViewDef view, ClientGinjector ginjector,
            RoleModelProvider modelProvider,
            RolePermissionModelProvider permissionModelProvider) {
        super(eventBus, view);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getCloseButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().hide();
            }
        }));
    }
}
