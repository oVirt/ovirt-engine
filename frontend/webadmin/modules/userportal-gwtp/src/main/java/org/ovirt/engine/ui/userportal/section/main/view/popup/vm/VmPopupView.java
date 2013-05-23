package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmPopupWidget;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmPopupPresenterWidget;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;

public class VmPopupView extends AbstractVmPopupView implements VmPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates applicationTemplates) {
        super(eventBus, resources, new VmPopupWidget(constants, resources, messages, applicationTemplates) {
            @Override
            protected PopupWidgetConfigMap createWidgetConfiguration() {
                return super.createWidgetConfiguration().update(hostTab, hiddenField());
            }
        });
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
