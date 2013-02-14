package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportSanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Label;

public class SanImportStorageView extends AbstractSanStorageView {

    @UiField
    @Path(value = "error")
    Label errorMessage;

    EntityModelCellTable<ListModel> table;

    static final double panelHeight = 396;

    @Override
    protected void initLists(SanStorageModelBase object) {
        table = new EntityModelCellTable<ListModel>(false, (Resources) GWT.create(LunTableResources.class));

        table.addColumn(new EntityModelTextColumn<StorageDomain>() {
            @Override
            public String getText(StorageDomain storage) {
                return storage.getstorage_name();
            }
        }, constants.nameSanImStorage());

        table.addColumn(new EntityModelEnumColumn<StorageDomain, StorageFormatType>() {

            @Override
            protected StorageFormatType getEnum(StorageDomain storage) {
                return
                storage.getStorageStaticData().getStorageFormat();
            }
        }, constants.formatSanImStorage(), "80px"); //$NON-NLS-1$

        table.setColumnWidth(table.getColumn(0), "20px"); //$NON-NLS-1$

        listPanel.add(table);

        contentPanel.setHeight(panelHeight + "PX"); //$NON-NLS-1$
    }

    @Override
    public void edit(SanStorageModelBase object) {
        super.edit(object);

        table.setRowData(new ArrayList<EntityModel>());
        table.edit(((ImportSanStorageModel) object).getCandidatesList());
    }

    public interface LunTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/SanStorageListHeader.css" })
        TableStyle cellTableStyle();
    }
}
