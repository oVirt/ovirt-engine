package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure;

import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RolePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RoleModel, RolePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RoleModel> {

    }

    @Inject
    public RolePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
        // TODO Auto-generated constructor stub
    }
}
