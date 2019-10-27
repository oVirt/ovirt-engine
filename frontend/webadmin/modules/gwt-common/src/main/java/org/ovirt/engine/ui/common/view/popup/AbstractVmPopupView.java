package org.ovirt.engine.ui.common.view.popup;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.LeftAlignedUiCommandButton;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.OvirtTabListItem;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public abstract class AbstractVmPopupView extends AbstractModelBoundWidgetPopupView<UnitVmModel> implements AbstractVmBasedPopupPresenterWidget.ViewDef {

    private VmPopupStyle style;

    @Inject
    public AbstractVmPopupView(EventBus eventBus, AbstractVmPopupWidget popupWidget, VmPopupResources resources) {
        this(eventBus, popupWidget, "930px", "812px", resources); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public AbstractVmPopupView(EventBus eventBus,
            AbstractVmPopupWidget popupWidget,
            String width,
            String height,
            VmPopupResources resources) {
        super(eventBus, popupWidget, width, height);
        style = resources.createStyle();
        style.ensureInjected();
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
    public void switchManaged(boolean managed) {
        if (getContentWidget() instanceof AbstractVmPopupWidget) {
            ((AbstractVmPopupWidget) getContentWidget()).switchManaged(managed);
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
    protected UiCommandButton createCommandButton(String label, String uniqueId) {
        if (VmBasedWidgetSwitchModeCommand.NAME.equals(uniqueId)) {
            LeftAlignedUiCommandButton leftAlignedUiCommandButton = new LeftAlignedUiCommandButton(label);
            return leftAlignedUiCommandButton;
        }

        return super.createCommandButton(label, uniqueId);
    }

    @Override
    public List<HasValidation> getInvalidWidgets() {
        if (getContentWidget() instanceof AbstractVmPopupWidget) {
            return ((AbstractVmPopupWidget) getContentWidget()).getInvalidWidgets();
        }

        return Collections.emptyList();
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return ((AbstractVmPopupWidget) getContentWidget()).getTabPanel();
    }

    @Override
    public Map<TabName, OvirtTabListItem> getTabNameMapping() {
        return ((AbstractVmPopupWidget) getContentWidget()).getTabNameMapping();
    }

    @Override
    public HasUiCommandClickHandlers getNumaSupportButton() {
        return ((AbstractVmPopupWidget) getContentWidget()).getNumaSupportButton();
    }

    @Override
    public HasClickHandlers getAddAffinityGroupButton() {
        return ((AbstractVmPopupWidget) getContentWidget()).getAddAffinityGroupButton();
    }

    @Override
    public HasClickHandlers getAddAffinityLabelButton() {
        return ((AbstractVmPopupWidget) getContentWidget()).getAddAffinityLabelButton();
    }
}
