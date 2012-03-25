package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.quota.EditQuotaClusterModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EditQuotaClusterPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<EditQuotaClusterModel, EditQuotaClusterPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<EditQuotaClusterModel> {
    }

    @Inject
    public EditQuotaClusterPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
