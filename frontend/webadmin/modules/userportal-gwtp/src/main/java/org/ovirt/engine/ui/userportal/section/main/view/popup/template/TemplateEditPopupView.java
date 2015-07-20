package org.ovirt.engine.ui.userportal.section.main.view.popup.template;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.view.popup.VmPopupResources;
import org.ovirt.engine.ui.common.widget.uicommon.popup.template.TemplateEditPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateEditPopupPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateEditPopupView extends AbstractVmPopupView implements TemplateEditPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<TemplateEditPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public TemplateEditPopupView(EventBus eventBus, VmPopupResources resources) {
        super(eventBus, new TemplateEditPopupWidget(eventBus){
            @Override
            protected PopupWidgetConfigMap createWidgetConfiguration() {
                return super.createWidgetConfiguration().update(hostTab, hiddenField())
                        .update(foremanTab, hiddenField());
            }
        }, resources);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
