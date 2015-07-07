package org.ovirt.engine.ui.common.widget.uicommon.popup;

import java.util.Set;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractModeSwitchingPopupWidget<T extends Model> extends AbstractModelBoundPopupWidget<T> {

    private PopupWidgetConfigMap widgetConfiguration;

    private boolean createInstanceMode = false;

    private T model;

    // the tab which is always here
    private DialogTab defaultTab;

    protected void initializeModeSwitching(DialogTab defaultTab) {
        this.defaultTab = defaultTab;
        widgetConfiguration = createWidgetConfiguration();

    }

    protected PopupWidgetConfigMap createWidgetConfiguration() {
        // by default empty
        return new PopupWidgetConfigMap();
    }

    protected void hideAlwaysHiddenFields() {
        for (Widget hiddenWidget : widgetConfiguration.getAlwaysHidden().keySet()) {
            hiddenWidget.setVisible(false);
        }
    }

    public void switchMode(boolean advanced) {
        Set<Widget> allConfiguredWidgets = widgetConfiguration.getAll().keySet();
        for (Widget widget : allConfiguredWidgets) {
            widget.setVisible(widgetConfiguration.get(widget).isCurrentlyVisible(advanced, createInstanceMode));
        }

        DialogTab activeTab = ((DialogTabPanel) getWidget()).getActiveTab();

        // select the first tab if the selected tab has been hidden
        if (!advanced && widgetConfiguration.getVisibleInAdvanceMode().keySet().contains(activeTab)) {
            ((DialogTabPanel) getWidget()).switchTab(defaultTab);
        }
    }

    protected void changeApplicationLevelVisibility(Widget widget, boolean visible) {
        widget.setVisible(evaluateNewVisibility(widget, visible));
    }

    private boolean evaluateNewVisibility(Widget source, boolean desiredVisibility) {
        // it is not configured explicitly - change to the desired visibility and terminate
        if (!widgetConfiguration.containsKey(source)) {
            return desiredVisibility;
        }

        // it is always hidden, ignore any app change
        PopupWidgetConfig vmPopupWidgetConfig = widgetConfiguration.get(source);
        if (vmPopupWidgetConfig.isAlwaysHidden()) {
            return false;
        }

        vmPopupWidgetConfig.setApplicationLevelVisible(desiredVisibility);
        boolean advancedMode = model.getAdvancedMode().getEntity();

        return vmPopupWidgetConfig.isCurrentlyVisible(advancedMode, createInstanceMode);
    }

    @Override
    public void edit(T model) {
        this.model = model;
    }

    class ModeSwitchingVisibilityRenderer implements VisibilityRenderer {

        @Override
        public boolean render(Widget source, boolean desiredVisibility) {
            return evaluateNewVisibility(source, desiredVisibility);
        }

    }

    public PopupWidgetConfigMap getWidgetConfiguration() {
        return widgetConfiguration;
    }

    public void setCreateInstanceMode(boolean createInstanceMode) {
        this.createInstanceMode = createInstanceMode;
    }
}
