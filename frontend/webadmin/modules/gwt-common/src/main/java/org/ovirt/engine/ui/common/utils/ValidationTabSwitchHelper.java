package org.ovirt.engine.ui.common.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.TabbedView;
import org.ovirt.engine.ui.common.widget.dialog.tab.OvirtTabListItem;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Helper class that does all the calculations and work to determine which tab to switch to if any. The current
 * active tab is invalid it won't switch tabs. It also manages the display of an invalid tab.
 */
public final class ValidationTabSwitchHelper {
    /**
     * Private constructor so you can't instantiate this class.
     */
    private ValidationTabSwitchHelper() {
        //Don't allow instances.
    }
    /**
     * Register the {@code ValidationCompleteEvent} handler.
     * @param eventBus The GWT event bus.
     * @param presenterWidget The {@code PresenterWidget} containing the model that was validated.
     * @param view The {@code View} to update based on state of the model.
     * @return The {@code HandlerRegistration} so the caller can manage the handlers.
     */
    public static HandlerRegistration registerValidationHandler(final EventBus eventBus,
            final AbstractModelBoundPopupPresenterWidget<?, ?> presenterWidget, final TabbedView view) {
        return eventBus.addHandler(ValidationCompleteEvent.getType(),
                event -> {
                    //Make sure the model in the event is the one we are interested in.
                    if (event.getModel() != null && event.getModel().equals(presenterWidget.getModel())) {
                        //Get the invalid tab names from the model.
                        Set<TabName> invalidTabs = presenterWidget.getModel().getInvalidTabs();
                        //Get the tab names to dialog tab widget map from the view.
                        Map<TabName, OvirtTabListItem> mapping = view.getTabNameMapping();
                        markTabs(invalidTabs, mapping);
                        //Check if the current active tab is invalid, if so don't do anything.
                        for (TabName invalidTabName: invalidTabs) {
                            if (view.getTabPanel().getActiveTab().equals(mapping.get(invalidTabName))) {
                                return;
                            }
                        }
                        //The current tab is not invalid, switch to the top invalid tab.
                        switchTab(invalidTabs, mapping, view);
                    }
                });
    }

    /**
     * Iterate over the {@code DialogTab}s and mark them valid/invalid based on the passed in
     * {@code Set} of invalid tab names.
     * @param invalidTabs The set of invalid tab names.
     * @param mapping The TabName to DialogTab mapping.
     */
    private static void markTabs(Set<TabName> invalidTabs, Map<TabName, OvirtTabListItem> mapping) {
        for (Map.Entry<TabName, OvirtTabListItem> entry: mapping.entrySet()) {
            if (invalidTabs.contains(entry.getKey())) {
                entry.getValue().markAsInvalid(null);
            } else {
                entry.getValue().markAsValid();
            }
        }
    }

    /**
     * Switch to the lowest number invalid tab in the {@code TabbedView}. The tabbed view returns an ordered
     * list of tabs that we can compare against the invalid tabs passed into this method.
     * @param invalidTabs The {@code Set} of invalid tab names.
     * @param mapping The TabName to DialogTab mapping.
     * @param view The {@code TabbedView} containing the tabs.
     */
    private static void switchTab(Set<TabName> invalidTabs, Map<TabName, OvirtTabListItem> mapping, TabbedView view) {
        int lowestIndex = Integer.MAX_VALUE;
        //Get all the tabs from the view.
        List<OvirtTabListItem> allTabs = view.getTabPanel().getTabs();
        //For each invalid tab find the index, and the lowest index will be the best fit.
        for (TabName invalidTabName: invalidTabs) {
            if (allTabs.indexOf(mapping.get(invalidTabName)) < lowestIndex) {
                lowestIndex = allTabs.indexOf(mapping.get(invalidTabName));
            }
        }
        if (lowestIndex != Integer.MAX_VALUE) {
            view.getTabPanel().switchTab(allTabs.get(lowestIndex));
        }
    }

}
