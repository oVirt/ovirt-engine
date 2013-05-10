package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template;

import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateNewPresenterWidget extends AbstractVmBasedPopupPresenterWidget<TemplateNewPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {

    }

    @Inject
    public TemplateNewPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
