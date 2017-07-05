package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.ActionCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmTemplateNameRenderer;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;

public class SubTabStorageVmBackupView extends AbstractSubTabTableView<StorageDomain, VM, StorageListModel, VmBackupModel>
        implements SubTabStorageVmBackupPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SubTabStorageVmBackupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageVmBackupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final VmTemplateNameRenderer vmTemplateNameRenderer = new VmTemplateNameRenderer();

    @UiField
    SimplePanel vmTableContainer;

    @UiField
    SimplePanel applicationsTableContainer;

    ActionCellTable<String> applicationsTable;

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SubTabStorageVmBackupView(SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> modelProvider) {
        super(modelProvider);
        initVmTable();
        initApplicationsTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        vmTableContainer.add(getTableContainer());
        applicationsTableContainer.add(applicationsTable);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initVmTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VM> nameColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> templateColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return vmTemplateNameRenderer.render(object);
            }
        };
        templateColumn.makeSortable();
        getTable().addColumn(templateColumn, constants.templateVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> originColumn = new AbstractEnumColumn<VM, OriginType>() {
            @Override
            protected OriginType getRawValue(VM object) {
                return object.getOrigin();
            }
        };
        originColumn.makeSortable();
        getTable().addColumn(originColumn, constants.originVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> memoryColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return messages.megabytes(String.valueOf(object.getVmMemSizeMb()));
            }
        };
        memoryColumn.makeSortable();
        getTable().addColumn(memoryColumn, constants.memoryVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> cpuColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getNumOfCpus());
            }
        };
        cpuColumn.makeSortable();
        getTable().addColumn(cpuColumn, constants.cpusVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> archColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getClusterArch());
            }
        };
        archColumn.makeSortable();
        getTable().addColumn(archColumn, constants.architectureVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> diskColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getDiskMap().size());
            }
        };
        diskColumn.makeSortable();
        getTable().addColumn(diskColumn, constants.disksVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> creationDateColumn = new AbstractFullDateTimeColumn<VM>() {
            @Override
            protected Date getRawValue(VM object) {
                return object.getVmCreationDate();
            }
        };
        creationDateColumn.makeSortable(Comparator.comparing(VM::getVmCreationDate));
        getTable().addColumn(creationDateColumn, constants.creationDateVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> exportDateColumn = new AbstractFullDateTimeColumn<VM>() {
            @Override
            protected Date getRawValue(VM object) {
                return object.getExportDate();
            }
        };
        exportDateColumn.makeSortable(Comparator.comparing(VM::getExportDate));
        getTable().addColumn(exportDateColumn, constants.exportDateVm(), "95px"); //$NON-NLS-1$

        getTable().showRefreshButton();
    }

    private void initApplicationsTable() {
        applicationsTable = new ActionCellTable<>(new AbstractDataProvider<String>() {
            @Override
            protected void onRangeChanged(HasData<String> display) {
            }
        }, GWT.create(SubTableResources.class));

        AbstractTextColumn<String> nameColumn = new AbstractTextColumn<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };

        applicationsTable.addColumn(nameColumn, constants.installedAppsVm());
        applicationsTable.setRowData(new ArrayList<>());

        getDetailModel().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (getDetailModel().getAppListModel().getItems() != null) {
                applicationsTable.setRowData(new ArrayList<>(getDetailModel().getAppListModel().getItems()));
            } else {
                applicationsTable.setRowData(new ArrayList<>());
            }
        });
    }

}
