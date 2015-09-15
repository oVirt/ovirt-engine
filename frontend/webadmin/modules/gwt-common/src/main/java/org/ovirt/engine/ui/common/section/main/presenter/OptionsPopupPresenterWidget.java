package org.ovirt.engine.ui.common.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.EditOptionsModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class OptionsPopupPresenterWidget
        extends AbstractModelBoundPopupPresenterWidget<EditOptionsModel, OptionsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<EditOptionsModel> {
    }

    @Inject
    public OptionsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
