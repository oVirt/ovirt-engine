package org.ovirt.engine.ui.userportal.section.main.presenter.popup.template;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateEditPopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<TemplateEditPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {
    }

    @Inject
    public TemplateEditPopupPresenterWidget(EventBus eventBus, ViewDef view, ClientStorage clientStorage) {
        super(eventBus, view, clientStorage);
    }

}
