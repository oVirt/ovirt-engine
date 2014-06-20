package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.LeftAlignedUiCommandButton;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;

public abstract class AbstractVmPopupView extends AbstractModelBoundWidgetPopupView<UnitVmModel> implements AbstractVmBasedPopupPresenterWidget.ViewDef {

    @Inject
    public AbstractVmPopupView(EventBus eventBus, CommonApplicationResources resources,
            AbstractVmPopupWidget popupWidget) {
        this(eventBus, resources, popupWidget, "690px", "580px"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public AbstractVmPopupView(EventBus eventBus, CommonApplicationResources resources,
            AbstractVmPopupWidget popupWidget, String width, String height) {
        super(eventBus, resources, popupWidget, width, height);
    }

    @Override
    public void switchAttachToInstanceType(boolean isAttached) {
        if (getContentWidget() instanceof AbstractVmPopupWidget) {
            ((AbstractVmPopupWidget) getContentWidget()).switchAttachToInstanceType(isAttached);
        }
    }

    @Override
    public void switchMode(boolean isAdvanced) {
        if (getContentWidget() instanceof AbstractVmPopupWidget) {
            ((AbstractVmPopupWidget) getContentWidget()).switchMode(isAdvanced);
        }
    }

    @Override
    public void initToCreateInstanceMode() {
        if (getContentWidget() instanceof AbstractVmPopupWidget) {
            ((AbstractVmPopupWidget) getContentWidget()).initCreateInstanceMode();
        }
    }

    @Override
    public void setSpiceProxyOverrideExplanation(String explanation) {
        if (getContentWidget() instanceof AbstractVmPopupWidget) {
            ((AbstractVmPopupWidget) getContentWidget()).setSpiceProxyOverrideExplanation(explanation);
        }

    }

    @Override
    protected AbstractUiCommandButton createCommandButton(String label, String uniqueId) {
        if (VmBasedWidgetSwitchModeCommand.NAME.equals(uniqueId)) {
            return new LeftAlignedUiCommandButton(label);
        }

        return super.createCommandButton(label, uniqueId);
    }

    @Override
    public List<HasValidation> getInvalidWidgets() {
        if (getContentWidget() instanceof AbstractVmPopupWidget) {
            return ((AbstractVmPopupWidget) getContentWidget()).getInvalidWidgets();
        }

        return Collections.EMPTY_LIST;
    }
}
