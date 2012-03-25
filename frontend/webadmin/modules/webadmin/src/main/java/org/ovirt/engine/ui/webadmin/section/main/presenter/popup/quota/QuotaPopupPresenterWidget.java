package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class QuotaPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<QuotaModel, QuotaPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<QuotaModel> {
    }

    @Inject
    public QuotaPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
