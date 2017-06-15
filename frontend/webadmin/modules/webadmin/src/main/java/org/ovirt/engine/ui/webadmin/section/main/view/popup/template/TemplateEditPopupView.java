package org.ovirt.engine.ui.webadmin.section.main.view.popup.template;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.view.popup.VmPopupResources;
import org.ovirt.engine.ui.common.widget.uicommon.popup.template.TemplateEditPopupWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateEditPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateEditPopupView extends AbstractVmPopupView implements TemplateEditPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<TemplateEditPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public TemplateEditPopupView(EventBus eventBus, VmPopupResources resources) {
        super(eventBus, new TemplateEditPopupWidget(), resources);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }


}
