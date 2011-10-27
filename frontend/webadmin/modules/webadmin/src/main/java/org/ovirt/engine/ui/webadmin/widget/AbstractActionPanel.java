package org.ovirt.engine.ui.webadmin.widget;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.ActionButton;
import org.ovirt.engine.ui.webadmin.widget.table.ActionButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.DynamicUiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

public abstract class AbstractActionPanel<T> extends Composite {

    @UiField
    FlowPanel actionPanel;

    // List of action buttons managed by this action table
    protected final List<ActionButtonDefinition<T>> actionButtonList = new ArrayList<ActionButtonDefinition<T>>();

    protected final SearchableModelProvider<T, ?> dataProvider;
    protected final AbstractSelectionModel<T> selectionModel;

    public AbstractActionPanel(final SearchableModelProvider<T, ?> dataProvider,
            AbstractSelectionModel<T> selectionModel) {
        this.dataProvider = dataProvider;
        this.selectionModel = selectionModel;
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
    }

    /**
     * Adds a new button to the table's action panel.
     * 
     * @param <M>
     */
    @SuppressWarnings("unchecked")
    public <M extends SearchableListModel> void addActionButton(final ActionButtonDefinition<T> buttonDef) {
        final ActionButton newActionButton = createNewActionButton(buttonDef);

        // set the button according to its definition
        newActionButton.setEnabledHtml(buttonDef.getEnabledHtml());
        newActionButton.setDisabledHtml(buttonDef.getDisabledHtml());
        newActionButton.setTitle(buttonDef.getTitle());

        actionPanel.add(newActionButton.asWidget());
        actionButtonList.add(buttonDef);

        newActionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (buttonDef.isImplemented()) {
                    buttonDef.onClick(getSelectedList());
                }
                else {
                    FeatureNotImplementedYetPopup fniyp =
                            new FeatureNotImplementedYetPopup((Widget) event.getSource(),
                                    buttonDef.isImplInUserPortal());
                    fniyp.show();
                }
            }
        });

        // Add listener for changes in the List Model "SelectedItem"
        if (dataProvider instanceof SearchableTableModelProvider) {
            ((SearchableTableModelProvider<T, M>) dataProvider).getModel()
                    .getSelectedItemsChangedEvent()
                    .addListener(new IEventListener() {
                        @Override
                        public void eventRaised(org.ovirt.engine.core.compat.Event ev, Object sender, EventArgs args) {
                            // update the command if it is dynamic
                            if (buttonDef instanceof DynamicUiCommandButtonDefinition) {
                                ((DynamicUiCommandButtonDefinition<T, ?>) buttonDef).updateCommand();
                            }
                        }
                    });
        }

        // Add init Listener
        if (buttonDef instanceof UiCommandButtonDefinition) {
            ((UiCommandButtonDefinition<T>) buttonDef).addInitializeHandler(new InitializeHandler() {
                @Override
                public void onInitialize(InitializeEvent event) {
                    updateActionButton(newActionButton, buttonDef);
                }
            });
        }

        updateActionButton(newActionButton, buttonDef);
    }

    List<T> getSelectedList() {
        List<T> selectedItems = new ArrayList<T>();
        if (selectionModel instanceof SingleSelectionModel) {
            selectedItems.add((T) ((SingleSelectionModel) selectionModel).getSelectedObject());
        } else if (selectionModel instanceof MultiSelectionModel) {
            selectedItems.addAll(((OrderedMultiSelectionModel) selectionModel).getSelectedList());
        }

        return selectedItems;
    }

    public FlowPanel getActionPanel() {
        return actionPanel;
    }

    /**
     * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
     */
    void updateActionButton(ActionButton button, ActionButtonDefinition<T> buttonDef) {

        button.asWidget().setVisible(buttonDef.isAccessible());
        button.setEnabled(buttonDef.isEnabled(getSelectedList()));
    }

    public boolean hasActionButtons() {
        return !actionButtonList.isEmpty();
    }

    public List<ActionButtonDefinition<T>> getActionButtonList() {
        return actionButtonList;
    }

    /**
     * Returns a new action button widget based on the given definition.
     */
    protected abstract ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef);

}
