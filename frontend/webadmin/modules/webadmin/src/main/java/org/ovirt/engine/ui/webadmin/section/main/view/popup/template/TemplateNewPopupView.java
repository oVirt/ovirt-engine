package org.ovirt.engine.ui.webadmin.section.main.view.popup.template;

import org.ovirt.engine.ui.common.widget.uicommon.popup.template.TemplateNewPopupWidget;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateNewPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractVmPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateNewPopupView extends AbstractVmPopupView implements TemplateNewPresenterWidget.ViewDef {

    @Inject
    public TemplateNewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources, new TemplateNewPopupWidget(constants));
    }


}
