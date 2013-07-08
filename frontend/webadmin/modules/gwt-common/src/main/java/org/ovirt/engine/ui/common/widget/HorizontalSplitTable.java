package org.ovirt.engine.ui.common.widget;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class HorizontalSplitTable extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, HorizontalSplitTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
    }

    private final MultiSelectionModel<EntityModel> topSelectionModel;
    private final MultiSelectionModel<EntityModel> bottomSelectionModel;

    private IEventListener topItemsChangedListener;
    private IEventListener bottomItemsChangedListener;

    @UiField
    protected SimpleDialogButton downButton;

    @UiField
    protected SimpleDialogButton upButton;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel> topTable;

    @UiField(provided = true)
    protected EntityModelCellTable<ListModel> bottomTable;

    @UiField(provided = true)
    protected Label topTitle;

    @UiField(provided = true)
    protected Label bottomTitle;

    @SuppressWarnings("unchecked")
    public HorizontalSplitTable(EntityModelCellTable<ListModel> topTable,
            EntityModelCellTable<ListModel> bottomTable,
            String topTitle,
            String bottomTitle,
            CommonApplicationConstants constants) {

        this.topTable = topTable;
        this.bottomTable = bottomTable;
        this.topTitle = new Label(topTitle);
        this.bottomTitle = new Label(bottomTitle);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        topSelectionModel = (MultiSelectionModel<EntityModel>) topTable.getSelectionModel();
        bottomSelectionModel = (MultiSelectionModel<EntityModel>) bottomTable.getSelectionModel();

        downButton.setText(constants.horizontalSplitTableDown());
        upButton.setText(constants.horizontalSplitTableUp());
        downButton.setEnabled(false);
        upButton.setEnabled(false);

        addSelectionHandler(true);
        addSelectionHandler(false);
        addClickHandler(true);
        addClickHandler(false);
    }

    private void addSelectionHandler(boolean topTable) {
        final MultiSelectionModel<EntityModel> selectionModel = getSelectionModel(topTable);
        final ButtonBase button = getButton(topTable);
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                button.setEnabled(!selectionModel.getSelectedSet().isEmpty());
            }
        });
    }

    private void addClickHandler(final boolean topTableIsSource) {
        getButton(topTableIsSource).addClickHandler(new ClickHandler() {

            @SuppressWarnings("unchecked")
            @Override
            public void onClick(ClickEvent event) {
                MultiSelectionModel<EntityModel> sourceSelectionModel = getSelectionModel(topTableIsSource);
                EntityModelCellTable<ListModel> sourceTable = getTable(topTableIsSource);
                EntityModelCellTable<ListModel> targetTable = getTable(!topTableIsSource);

                Set<EntityModel> selectedItems = sourceSelectionModel.getSelectedSet();
                ((Collection<EntityModel>) sourceTable.flush().getItems()).removeAll(selectedItems);
                ListModel targetListModel = targetTable.flush();
                Collection<EntityModel> targetItems = (Collection<EntityModel>) targetListModel.getItems();
                if (targetItems == null) {
                    targetItems = new LinkedList<EntityModel>();
                    targetListModel.setItems(targetItems);
                }
                targetItems.addAll(selectedItems);
                refresh();
            }
        });
    }

    private MultiSelectionModel<EntityModel> getSelectionModel(boolean top) {
        return top ? topSelectionModel : bottomSelectionModel;
    }

    private ButtonBase getButton(boolean down) {
        return down ? downButton : upButton;
    }

    private EntityModelCellTable<ListModel> getTable(boolean top) {
        return top ? topTable : bottomTable;
    }

    private void refresh() {
        topSelectionModel.clear();
        bottomSelectionModel.clear();
        topTable.edit(topTable.flush());
        bottomTable.edit(bottomTable.flush());
    }

    private void edit(ListModel model, final boolean topTableIsEdited) {
        EntityModelCellTable<ListModel> table = getTable(topTableIsEdited);
        IEventListener listener = topTableIsEdited ? topItemsChangedListener : bottomItemsChangedListener;
        if (listener != null) {
            table.flush().getItemsChangedEvent().removeListener(listener);
        }
        listener = new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getSelectionModel(topTableIsEdited).clear();
            }
        };
        model.getItemsChangedEvent().addListener(listener);
        table.edit(model);
    }

    public void edit(ListModel topListModel, ListModel bottomListModel) {
        edit(topListModel, true);
        edit(bottomListModel, false);
    }

}
