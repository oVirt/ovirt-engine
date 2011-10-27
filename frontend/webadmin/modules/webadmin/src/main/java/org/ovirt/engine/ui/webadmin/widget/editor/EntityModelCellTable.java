package org.ovirt.engine.ui.webadmin.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.webadmin.widget.HasEditorDriver;
import org.ovirt.engine.ui.webadmin.widget.table.column.RadioboxCell;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A CellTable of a {@link ListModel} of {@link EntityModel}s
 *
 * @param <M>
 */
public class EntityModelCellTable<M extends ListModel> extends CellTable<EntityModel> implements HasEditorDriver<M> {

    /**
     * The ListModel
     */
    private M listModel;

    /**
     * Whether to allow multi/single selection
     */
    private final boolean multiSelection;

    /**
     * Create a new {@link EntityModelCellTable} with Single Selection
     */
    public EntityModelCellTable() {
        this(false);
    }

    /**
     * Create a new {@link EntityModelCellTable}
     *
     * @param multiSelection
     *            Whether to allow multi/single selection
     */
    public EntityModelCellTable(boolean multiSelection) {
        this.multiSelection = multiSelection;

        if (!multiSelection) {
            setSelectionModel(new SingleSelectionModel<EntityModel>());
        } else {
            setSelectionModel(new MultiSelectionModel<EntityModel>(),
                    DefaultSelectionEventManager.<EntityModel> createCheckboxManager());
        }

        // Handle Selection
        getSelectionModel().addSelectionChangeHandler(new Handler() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                // Clear "IsSelected"
                for (EntityModel entity : (List<EntityModel>) EntityModelCellTable.this.listModel.getItems()) {
                    entity.setIsSelected(false);
                }
                EntityModelCellTable.this.listModel.setSelectedItems(null);

                // Set "IsSelected"
                SelectionModel<? super EntityModel> selectionModel = EntityModelCellTable.this.getSelectionModel();
                if (selectionModel instanceof SingleSelectionModel) {
                    ((SingleSelectionModel<EntityModel>) selectionModel).getSelectedObject().setIsSelected(true);
                    EntityModelCellTable.this.listModel.setSelectedItem(((SingleSelectionModel<EntityModel>) selectionModel).getSelectedObject());
                } else if (selectionModel instanceof MultiSelectionModel) {
                    List<EntityModel> selectedItems = new ArrayList<EntityModel>();
                    for (EntityModel entity : ((MultiSelectionModel<EntityModel>) selectionModel).getSelectedSet()) {
                        entity.setIsSelected(true);
                        selectedItems.add(entity);
                    }

                    EntityModelCellTable.this.listModel.setSelectedItems(selectedItems);
                }
            }
        });

        // add selection columns
        Column<EntityModel, Boolean> checkColumn;
        if (multiSelection) {
            checkColumn = new Column<EntityModel, Boolean>(
                    new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(EntityModel object) {
                    return getSelectionModel().isSelected(object);
                }
            };
        } else {
            checkColumn = new Column<EntityModel, Boolean>(
                    new RadioboxCell(true, false)) {
                @Override
                public Boolean getValue(EntityModel object) {
                    return getSelectionModel().isSelected(object);
                }
            };
        }
        addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        setColumnWidth(checkColumn, 40, Unit.PX);
    }

    /**
     * Ad an EntityModelColumn to the Grid
     *
     * @param column
     * @param headerString
     */
    public void addEntityModelColumn(Column<EntityModel, ?> column, String headerString) {
        addColumn(column, headerString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(M object) {
        this.listModel = object;

        // Add ItemsChangedEvent Listener
        object.getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M list = (M) sender;
                List<EntityModel> items = (List<EntityModel>) list.getItems();
                setRowData(items == null ? new ArrayList<EntityModel>() : items);
            }
        });

        object.getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M list = (M) sender;
                list.getSelectedItem();
                getSelectionModel().setSelected((EntityModel)list.getSelectedItem(),true);
            }
        });
    }

    @Override
    public M flush() {
        return listModel;
    }

}
