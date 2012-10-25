package org.ovirt.engine.ui.webadmin.section.main.view.popup.template;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.template.TemplateNewPopupWidget;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateNewPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateNewPopupView extends AbstractVmPopupView implements TemplateNewPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<TemplateNewPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public TemplateNewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates templates) {
        super(eventBus, resources, new TemplateNewPopupWidget(constants, resources, messages, templates));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }


}
