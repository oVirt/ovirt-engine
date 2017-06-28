package org.ovirt.engine.ui.common.widget.action;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.Kebab;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Contain the toolbar portion of a data table including the search panel, action buttons, action kebab
 * and search results.  The search panel will take up all the space available from the action buttons,
 * pushing them to the right.  If no search panel is provided, the action buttons are displayed flush left.
 */
public class PatternflyActionPanel extends FlowPanel {
    private FlowPanel actionContainer;
    private FormGroup searchFormGroup;
    private FormGroup actionFormGroup;
    private FlowPanel resultsContainer;
    private Kebab actionKebab;

    public PatternflyActionPanel() {
        actionContainer = new FlowPanel(); // filer/search + actions + action-right
        actionContainer.addStyleName(PatternflyConstants.PF_TOOLBAR_ACTIONS);

        actionFormGroup = new FormGroup(); // action buttons + kebab menu
        actionFormGroup.addStyleName(Styles.ROW);
        actionContainer.add(actionFormGroup);

        actionKebab = new Kebab();
        actionKebab.setVisible(false);
        actionFormGroup.add(actionKebab);

        resultsContainer = new FlowPanel();

        FlowPanel panel = new FlowPanel();
        panel.add(actionContainer);
        panel.add(resultsContainer);

        addStyleName(PatternflyConstants.PF_TOOLBAR);
        add(panel);
        setVisible(false);
    }

    /**
     * Create the container to hold the search panel as necessary and insert the
     * provided search panel to be displayed.
     */
    public void setSearchPanel(IsWidget searchPanel) {
        if (searchPanel == null) {
            if (searchFormGroup != null) {
                actionContainer.remove(searchFormGroup);
                searchFormGroup = null;
            }
        } else {
            if (searchFormGroup != null) {
                searchFormGroup.clear();
            } else {
                searchFormGroup = new FormGroup();
                searchFormGroup.addStyleName(PatternflyConstants.PF_TOOLBAR_FILTER);
                actionContainer.insert(searchFormGroup, 0);
            }
            searchFormGroup.add(searchPanel);

            actionFormGroup.removeStyleName(Styles.ROW);
        }
    }

    /**
     * Add the toolbar's search result / applied filter row to the action panel below the search panel and buttons
     */
    public void addResult(IsWidget result) {
        resultsContainer.add(result);
    }

    public void addButtonToActionGroup(ActionButton button) {
        if (actionFormGroup.getWidgetCount() > 0) {
            actionFormGroup.remove(actionFormGroup.getWidgetCount() - 1);
        }
        actionFormGroup.add(button);
        actionFormGroup.add(actionKebab);
        setVisible(true);
    }

    public void addMenuItemToKebab(ActionButton menuItem) {
        actionKebab.addActionButton(menuItem);
        actionKebab.setVisible(true);
        setVisible(true);
    }

    public void addDividerToKebab() {
        actionKebab.addDivider();
    }
}
