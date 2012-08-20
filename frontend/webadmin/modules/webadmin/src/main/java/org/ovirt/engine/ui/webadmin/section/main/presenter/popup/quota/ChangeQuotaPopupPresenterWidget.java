package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ChangeQuotaPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ChangeQuotaModel, ChangeQuotaPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ChangeQuotaModel> {
    }

    @Inject
    public ChangeQuotaPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
