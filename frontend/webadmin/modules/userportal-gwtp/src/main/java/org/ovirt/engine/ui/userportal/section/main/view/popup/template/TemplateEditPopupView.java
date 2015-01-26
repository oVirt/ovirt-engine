package org.ovirt.engine.ui.userportal.section.main.view.popup.template;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.template.TemplateEditPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateEditPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateEditPopupView extends AbstractVmPopupView implements TemplateEditPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<TemplateEditPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public TemplateEditPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates applicationTemplates) {
        super(eventBus, resources, new TemplateEditPopupWidget(constants, resources, messages, applicationTemplates, eventBus){
            @Override
            protected PopupWidgetConfigMap createWidgetConfiguration() {
                return super.createWidgetConfiguration().update(hostTab, hiddenField());
            }
        });
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
