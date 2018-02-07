package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;

import com.google.gwt.event.shared.EventBus;

public class UserRolesPopupPresenterWidget extends
    AbstractModelBoundPopupPresenterWidget<AdElementListModel, UserRolesPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AdElementListModel> {
    }

    @Inject
    public UserRolesPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
