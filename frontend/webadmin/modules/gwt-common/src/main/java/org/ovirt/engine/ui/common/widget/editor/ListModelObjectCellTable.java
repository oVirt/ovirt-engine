package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.DataGridPopupTableResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.IsEditorDriver;
import org.ovirt.engine.ui.common.widget.table.ColumnResizeCellTable;
import org.ovirt.engine.ui.common.widget.table.cell.CheckboxCell;
import org.ovirt.engine.ui.common.widget.table.cell.RadioboxCell;
import org.ovirt.engine.ui.common.widget.table.header.AbstractSelectAllCheckBoxHeader;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A {@code CellTable} acting as Editor of {@link ListModel} objects containing arbitrary items.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            List model type.
 */
public class ListModelObjectCellTable<T, M extends ListModel> extends ColumnResizeCellTable<T> implements IsEditorDriver<M> {

    private static final int DEFAULT_PAGESIZE = 1000;
    private static final int CHECK_COLUMN_WIDTH = 27;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final HasDataListModelEditorAdapter<M, T> editorAdapter;

    public ListModelObjectCellTable() {
        super(DEFAULT_PAGESIZE, (DataGrid.Resources) GWT.create(DataGridPopupTableResources.class));
        this.editorAdapter = new HasDataListModelEditorAdapter<>(this);

        SingleSelectionModel<T> selectionModel = new SingleSelectionModel<>();
        setSelectionModel(selectionModel);

        getSelectionModel().addSelectionChangeHandler(event -> getListModel().setSelectedItem(((SingleSelectionModel<?>) getSelectionModel()).getSelectedObject()));
    }

    public ListModelObjectCellTable(boolean multiSelection) {
        this(multiSelection, false);
    }

    @SuppressWarnings("unchecked")
    public ListModelObjectCellTable(boolean multiSelection, boolean showSelectAllCheckbox) {
        this();

        if (!multiSelection) {
            setSelectionModel(new SingleSelectionModel<>());
        } else {
            setSelectionModel(new MultiSelectionModel<>(), DefaultSelectionEventManager.createCheckboxManager());
        }

        // Handle Selection
        getSelectionModel().addSelectionChangeHandler(event -> {
            if (getListModel() == null || getListModel().getItems() == null) {
                return;
            }

            // Clear "IsSelected"
            getListModel().setSelectedItems(null);

            // Set "IsSelected"
            SelectionModel<? super T> selectionModel = ListModelObjectCellTable.this.getSelectionModel();
            if (selectionModel instanceof SingleSelectionModel) {
                getListModel().setSelectedItem(((SingleSelectionModel<T>) selectionModel).getSelectedObject());
            } else if (selectionModel instanceof MultiSelectionModel) {
                List<T> selectedItems = new ArrayList<>(((MultiSelectionModel<T>) selectionModel).getSelectedSet());
                getListModel().setSelectedItems(selectedItems);
            }
        });

        // add selection columns
        Column<T, Boolean> checkColumn;
        if (multiSelection) {
            checkColumn = new Column<T, Boolean>(
                    new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(T object) {
                    return getSelectionModel().isSelected(object);
                }
            };
            if (showSelectAllCheckbox) {
                final AbstractSelectAllCheckBoxHeader<T> selectAllHeader =
                        new AbstractSelectAllCheckBoxHeader<T>() {

                            @Override
                            protected void selectionChanged(Boolean value) {
                                if (getListModel() == null || getListModel().getItems() == null) {
                                    return;
                                }
                                handleSelection(value, getListModel(), getSelectionModel());
                            }

                            @Override
                            public Boolean getValue() {
                                if (getListModel() == null || getListModel().getItems() == null) {
                                    return false;
                                }
                                return getCheckValue(getListModel().getItems(), getSelectionModel());
                            }
                        };
                addColumn(checkColumn, selectAllHeader);
            } else {
                addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant(constants.htmlNonBreakingSpace()));
            }
        } else {
            checkColumn = new Column<T, Boolean>(
                    new RadioboxCell(true, false)) {
                @Override
                public Boolean getValue(T object) {
                    return getSelectionModel().isSelected(object);
                }
            };
            addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant(constants.htmlNonBreakingSpace()));
        }
        setColumnWidth(checkColumn, CHECK_COLUMN_WIDTH, Unit.PX);
    }

    public void selectAll() {
        for (T item : getVisibleItems()) {
            getSelectionModel().setSelected(item, true);
        }
    }

    M getListModel() {
        return asEditor().flush();
    }

    @Override
    public HasEditorDriver<M> asEditor() {
        return editorAdapter;
    }

    @Override
    public void addColumn(Column<T, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    public void addColumn(Column column, SafeHtml headerHtml, String width) {
        addColumnWithHtmlHeader(column, headerHtml, width);
    }

    public void addColumn(Column column, SafeHtmlHeader header, String width) {
        super.addColumnAndSetWidth(column, header, width);
    }

    public void addColumnAt(Column<T, ?> column, String headerText, String width, int position) {
        insertColumn(position, column, headerText);
        setColumnWidth(column, width);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateGridSize();
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        redraw();
        isHeightSet = true;
    }
}
