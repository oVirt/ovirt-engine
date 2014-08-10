package org.ovirt.engine.ui.common.widget;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.dialog.ShapedButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class HorizontalSplitTable<T> extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, HorizontalSplitTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);

    private final MultiSelectionModel<T> topSelectionModel;
    private final MultiSelectionModel<T> bottomSelectionModel;

    private final IEventListener<EventArgs> topItemsChangedListener;
    private final IEventListener<EventArgs> bottomItemsChangedListener;

    private UICommand onDownButtonPressed;
    private UICommand onUpButtonPressed;

    @UiField(provided = true)
    protected ShapedButton downButton;

    @UiField(provided = true)
    protected ShapedButton upButton;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel<T>> topTable;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel<T>> bottomTable;

    @UiField(provided = true)
    protected Label topTitle;

    @UiField(provided = true)
    protected Label bottomTitle;

    @SuppressWarnings("unchecked")
    public HorizontalSplitTable(EntityModelCellTable<ListModel<T>> topTable,
            EntityModelCellTable<ListModel<T>> bottomTable,
            String topTitle,
            String bottomTitle) {

        this.topTable = topTable;
        this.bottomTable = bottomTable;
        this.topTitle = new Label(topTitle);
        this.bottomTitle = new Label(bottomTitle);

        downButton =
                new ShapedButton(resources.arrowDownNormal(),
                        resources.arrowDownClick(),
                        resources.arrowDownOver(),
                        resources.arrowDownDisabled());
        upButton =
                new ShapedButton(resources.arrowUpNormal(),
                        resources.arrowUpClick(),
                        resources.arrowUpOver(),
                        resources.arrowUpDisabled());
        downButton.setEnabled(false);
        upButton.setEnabled(false);

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        topSelectionModel = (MultiSelectionModel<T>) topTable.getSelectionModel();
        bottomSelectionModel = (MultiSelectionModel<T>) bottomTable.getSelectionModel();

        topItemsChangedListener = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<EventArgs> ev, Object sender, EventArgs args) {
                topSelectionModel.clear();
            }
        };
        bottomItemsChangedListener = new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<EventArgs> ev, Object sender, EventArgs args) {
                bottomSelectionModel.clear();
            }
        };

        addSelectionHandler(true);
        addSelectionHandler(false);
        addClickHandler(true);
        addClickHandler(false);
    }

    private void addSelectionHandler(boolean topTable) {
        final MultiSelectionModel<T> selectionModel = getSelectionModel(topTable);
        final ShapedButton button = getButton(topTable);
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                button.setEnabled(!selectionModel.getSelectedSet().isEmpty());
            }
        });
    }

    private void addClickHandler(final boolean topTableIsSource) {
        getButton(topTableIsSource).addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                MultiSelectionModel<T> sourceSelectionModel = getSelectionModel(topTableIsSource);
                EntityModelCellTable<ListModel<T>> sourceTable = getTable(topTableIsSource);
                EntityModelCellTable<ListModel<T>> targetTable = getTable(!topTableIsSource);
                UICommand command = topTableIsSource ? onDownButtonPressed : onUpButtonPressed;

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

    private MultiSelectionModel<T> getSelectionModel(boolean top) {
        return top ? topSelectionModel : bottomSelectionModel;
    }

    private ShapedButton getButton(boolean down) {
        return down ? downButton : upButton;
    }

    private EntityModelCellTable<ListModel<T>> getTable(boolean top) {
        return top ? topTable : bottomTable;
    }

    private void refresh() {
        topSelectionModel.clear();
        bottomSelectionModel.clear();
        topTable.asEditor().edit(topTable.asEditor().flush());
        bottomTable.asEditor().edit(bottomTable.asEditor().flush());
    }

    private void edit(ListModel<T> model, final boolean topTableIsEdited) {
        EntityModelCellTable<ListModel<T>> table = getTable(topTableIsEdited);
        ListModel<T> oldModel = table.asEditor().flush();
        IEventListener<EventArgs> listener = topTableIsEdited ? topItemsChangedListener : bottomItemsChangedListener;
        if (oldModel != null) {
            oldModel.getItemsChangedEvent().removeListener(listener);
        }
        model.getItemsChangedEvent().addListener(listener);
        table.asEditor().edit(model);
    }

    public void edit(ListModel<T> topListModel, ListModel<T> bottomListModel) {
        edit(topListModel, true);
        edit(bottomListModel, false);
    }

    public void edit(ListModel<T> topListModel,
            ListModel<T> bottomListModel,
            UICommand onDownButtonPressed,
            UICommand onUpButtonPressed) {
        edit(topListModel, bottomListModel);
        this.onDownButtonPressed = onDownButtonPressed;
        this.onUpButtonPressed = onUpButtonPressed;
    }

}
