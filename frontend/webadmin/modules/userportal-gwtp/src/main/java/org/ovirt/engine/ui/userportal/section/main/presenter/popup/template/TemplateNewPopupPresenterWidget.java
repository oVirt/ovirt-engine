package org.ovirt.engine.ui.userportal.section.main.presenter.popup.template;

import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateNewPopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<TemplateNewPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {
    }

    @Inject
    public TemplateNewPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
