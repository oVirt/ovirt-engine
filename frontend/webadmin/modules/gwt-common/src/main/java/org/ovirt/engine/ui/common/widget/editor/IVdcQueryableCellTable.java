package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.PopupTableResources;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.table.column.RadioboxCell;
import org.ovirt.engine.ui.common.widget.table.header.SelectAllCheckBoxHeader;
import org.ovirt.engine.ui.common.widget.table.resize.ColumnResizeCellTable;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
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

public class IVdcQueryableCellTable<IVdcQueryable, M extends ListModel> extends ColumnResizeCellTable<IVdcQueryable> implements HasEditorDriver<M> {

    private static final int DEFAULT_PAGESIZE = 1000;
    private static final int CHECK_COLUMN_WIDTH = 27;

    private static CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    private M listModel;

    public IVdcQueryableCellTable() {
        super(DEFAULT_PAGESIZE, (CellTable.Resources) GWT.create(PopupTableResources.class));

        SingleSelectionModel<IVdcQueryable> selectionModel = new SingleSelectionModel<IVdcQueryable>();
        setSelectionModel(selectionModel);

        getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                listModel.setSelectedItem(((SingleSelectionModel<?>) getSelectionModel()).getSelectedObject());
            }
        });
    }

    public IVdcQueryableCellTable(boolean multiSelection) {
        this(multiSelection, false);
    }

    public IVdcQueryableCellTable(boolean multiSelection, boolean showSelectAllCheckbox) {
        this();

        if (!multiSelection) {
            setSelectionModel(new SingleSelectionModel<IVdcQueryable>());
        } else {
            setSelectionModel(new MultiSelectionModel<IVdcQueryable>(),
                    DefaultSelectionEventManager.<IVdcQueryable> createCheckboxManager());
        }

        // Handle Selection
        getSelectionModel().addSelectionChangeHandler(new Handler() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if ((IVdcQueryableCellTable.this.listModel == null)
                        || (IVdcQueryableCellTable.this.listModel.getItems() == null)) {
                    return;
                }

                // Clear "IsSelected"
                IVdcQueryableCellTable.this.listModel.setSelectedItems(null);

                // Set "IsSelected"
                SelectionModel<? super IVdcQueryable> selectionModel = IVdcQueryableCellTable.this.getSelectionModel();
                if (selectionModel instanceof SingleSelectionModel) {
                    IVdcQueryableCellTable.this.listModel.setSelectedItem(((SingleSelectionModel<IVdcQueryable>) selectionModel).getSelectedObject());
                } else if (selectionModel instanceof MultiSelectionModel) {
                    List<IVdcQueryable> selectedItems = new ArrayList<IVdcQueryable>();
                    for (IVdcQueryable entity : ((MultiSelectionModel<IVdcQueryable>) selectionModel).getSelectedSet()) {
                        selectedItems.add(entity);
                    }
                    IVdcQueryableCellTable.this.listModel.setSelectedItems(selectedItems);
                }
            }
        });

        // add selection columns
        Column<IVdcQueryable, Boolean> checkColumn;
        if (multiSelection) {
            checkColumn = new Column<IVdcQueryable, Boolean>(
                    new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(IVdcQueryable object) {
                    return getSelectionModel().isSelected(object);
                }
            };
            if (showSelectAllCheckbox) {
                final SelectAllCheckBoxHeader<IVdcQueryable> selectAllHeader = new SelectAllCheckBoxHeader<IVdcQueryable>() {

                    @Override
                    protected void selectionChanged(Boolean value) {
                        if (listModel == null || listModel.getItems() == null) {
                            return;
                        }
                        handleSelection(value, listModel, getSelectionModel());
                    }

                    @Override
                    public Boolean getValue() {
                        if (listModel == null || listModel.getItems() == null) {
                            return false;
                        }
                        return getCheckValue(listModel.getItems(), getSelectionModel());
                    }
                };
                addColumn(checkColumn, selectAllHeader);
            }
            else {
                addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant(constants.htmlNonBreakingSpace()));
            }
        } else {
            checkColumn = new Column<IVdcQueryable, Boolean>(
                    new RadioboxCell(true, false)) {
                @Override
                public Boolean getValue(IVdcQueryable object) {
                    return getSelectionModel().isSelected(object);
                }
            };
            addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant(constants.htmlNonBreakingSpace()));
        }
        setColumnWidth(checkColumn, CHECK_COLUMN_WIDTH, Unit.PX);
    }

    @Override
    public void addColumn(Column<IVdcQueryable, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    public void addColumnAt(Column<IVdcQueryable, ?> column, String headerText, String width, int position) {
        insertColumn(position, column, headerText);
        setColumnWidth(column, width);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(M object) {
        this.listModel = object;

        if (listModel.getItems() != null) {
            if (listModel.getItems() != null) {
                setRowData((ArrayList<IVdcQueryable>) listModel.getItems());
            } else {
                setRowData(new ArrayList<IVdcQueryable>());
            }
        }

        listModel.getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (listModel.getItems() != null) {
                    setRowData((ArrayList<IVdcQueryable>) listModel.getItems());
                }
                else {
                    setRowData(new ArrayList<IVdcQueryable>());
                }

            }
        });

        listModel.getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M list = (M) sender;
                getSelectionModel().setSelected((IVdcQueryable) list.getSelectedItem(), true);
            }
        });

        listModel.getSelectedItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M list = (M) sender;
                if (list.getSelectedItems() != null) {
                    for (Object item : list.getSelectedItems()) {
                        getSelectionModel().setSelected((IVdcQueryable) item, true);
                    }
                }
            }
        });
    }

    @Override
    public M flush() {
        return listModel;
    }

}
