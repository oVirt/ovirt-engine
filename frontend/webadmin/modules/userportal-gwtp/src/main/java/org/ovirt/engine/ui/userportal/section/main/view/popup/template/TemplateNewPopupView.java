package org.ovirt.engine.ui.userportal.section.main.view.popup.template;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.template.TemplateNewPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateNewPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateNewPopupView extends AbstractVmPopupView implements TemplateNewPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<TemplateNewPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public TemplateNewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources, new TemplateNewPopupWidget(constants) {
            @Override
            protected void setupHostTabAvailability(UnitVmModel model) {
                hostTab.setVisible(false);
            }
        });
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
