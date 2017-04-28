package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.label;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.model.AffinityLabelModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AffinityLabelPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<AffinityLabelModel, AffinityLabelPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AffinityLabelModel> {
    }

    @Inject
    public AffinityLabelPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
