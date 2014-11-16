package org.ovirt.engine.ui.common.widget;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.ovirt.engine.ui.common.widget.dialog.ShapedButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public abstract class SplitTable<T> extends Composite {

    private final MultiSelectionModel<T> excludedSelectionModel;
    private final MultiSelectionModel<T> includedSelectionModel;

    private final IEventListener<EventArgs> excludedItemsChangedListener;
    private final IEventListener<EventArgs> includedItemsChangedListener;

    private UICommand onIncludeButtonPressed;
    private UICommand onExcludeButtonPressed;

    @UiField(provided = true)
    protected ShapedButton includeButton;

    @UiField(provided = true)
    protected ShapedButton excludeButton;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel<T>> excludedTable;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel<T>> includedTable;

    @UiField(provided = true)
    protected Label excludedTitle;

    @UiField(provided = true)
    protected Label includedTitle;

    @SuppressWarnings("unchecked")
    public SplitTable(EntityModelCellTable<ListModel<T>> excludedTable,
            EntityModelCellTable<ListModel<T>> includedTable,
            String excludedTitle,
            String includedTitle) {

        this.excludedTable = excludedTable;
        this.includedTable = includedTable;
        this.excludedTitle = new Label(excludedTitle);
        this.includedTitle = new Label(includedTitle);

        includeButton = createIncludeButton();
        excludeButton = createExcludeButton();

        includeButton.setEnabled(false);
        excludeButton.setEnabled(false);

        initWidget();

        excludedSelectionModel = (MultiSelectionModel<T>) excludedTable.getSelectionModel();
        includedSelectionModel = (MultiSelectionModel<T>) includedTable.getSelectionModel();

        excludedItemsChangedListener = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                excludedSelectionModel.clear();
            }
        };
        includedItemsChangedListener = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                includedSelectionModel.clear();
            }
        };

        addSelectionHandler(true);
        addSelectionHandler(false);
        addClickHandler(true);
        addClickHandler(false);
    }

    protected abstract ShapedButton createIncludeButton();

    protected abstract ShapedButton createExcludeButton();

    protected abstract void initWidget();

    private void addSelectionHandler(boolean excludedTable) {
        final MultiSelectionModel<T> selectionModel = getSelectionModel(excludedTable);
        final ShapedButton button = getButton(excludedTable);
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                button.setEnabled(!selectionModel.getSelectedSet().isEmpty());
            }
        });
    }

    private void addClickHandler(final boolean excludedTableIsSource) {
        getButton(excludedTableIsSource).addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                MultiSelectionModel<T> sourceSelectionModel = getSelectionModel(excludedTableIsSource);
                EntityModelCellTable<ListModel<T>> sourceTable = getTable(excludedTableIsSource);
                EntityModelCellTable<ListModel<T>> targetTable = getTable(!excludedTableIsSource);
                UICommand command = excludedTableIsSource ? onIncludeButtonPressed : onExcludeButtonPressed;

                if (command != null) {
                    command.execute();
                }

                Set<T> selectedItems = sourceSelectionModel.getSelectedSet();
                sourceTable.asEditor().flush().getItems().removeAll(selectedItems);
                ListModel<T> targetListModel = targetTable.asEditor().flush();
                Collection<T> targetItems = targetListModel.getItems();
                if (targetItems == null) {
                    targetItems = new LinkedList<T>();
                    targetListModel.setItems(targetItems);
                }
                targetItems.addAll(selectedItems);
                refresh();
            }
        });
    }

    private MultiSelectionModel<T> getSelectionModel(boolean excluded) {
        return excluded ? excludedSelectionModel : includedSelectionModel;
    }

    private ShapedButton getButton(boolean include) {
        return include ? includeButton : excludeButton;
    }

    private EntityModelCellTable<ListModel<T>> getTable(boolean excluded) {
        return excluded ? excludedTable : includedTable;
    }

    private void refresh() {
        excludedSelectionModel.clear();
        includedSelectionModel.clear();
        excludedTable.asEditor().edit(excludedTable.asEditor().flush());
        includedTable.asEditor().edit(includedTable.asEditor().flush());
    }

    private void edit(ListModel<T> model, final boolean excludedTableIsEdited) {
        EntityModelCellTable<ListModel<T>> table = getTable(excludedTableIsEdited);
        ListModel<T> oldModel = table.asEditor().flush();
        IEventListener<EventArgs> listener = excludedTableIsEdited ? excludedItemsChangedListener : includedItemsChangedListener;
        if (oldModel != null) {
            oldModel.getItemsChangedEvent().removeListener(listener);
        }
        model.getItemsChangedEvent().addListener(listener);
        table.asEditor().edit(model);
    }

    public void edit(ListModel<T> excludedListModel, ListModel<T> includedListModel) {
        edit(excludedListModel, true);
        edit(includedListModel, false);
    }

    public void edit(ListModel<T> excludedListModel,
            ListModel<T> includedListModel,
            UICommand onIncludeButtonPressed,
            UICommand onExcludeButtonPressed) {
        edit(excludedListModel, includedListModel);
        this.onIncludeButtonPressed = onIncludeButtonPressed;
        this.onExcludeButtonPressed = onExcludeButtonPressed;
    }
}
