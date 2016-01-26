package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.ArrayList;
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
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmTemplateNameRenderer;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
    HorizontalPanel mainContainer;

    @UiField
    SimplePanel vmTableContainer;

    @UiField
    SimplePanel applicationsTableContainer;

    ActionCellTable<String> applicationsTable;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabStorageVmBackupView(SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> modelProvider) {
        super(modelProvider);
        initVmTable();
        initApplicationsTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        vmTableContainer.add(getTable());
        applicationsTableContainer.add(applicationsTable);

        mainContainer.setCellWidth(vmTableContainer, "50%"); //$NON-NLS-1$
        mainContainer.setCellWidth(applicationsTableContainer, "50%"); //$NON-NLS-1$
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
                return String.valueOf(object.getVmMemSizeMb()) + " MB"; //$NON-NLS-1$
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
        creationDateColumn.makeSortable();
        getTable().addColumn(creationDateColumn, constants.creationDateVm(), "95px"); //$NON-NLS-1$

        AbstractTextColumn<VM> exportDateColumn = new AbstractFullDateTimeColumn<VM>() {
            @Override
            protected Date getRawValue(VM object) {
                return object.getExportDate();
            }
        };
        exportDateColumn.makeSortable();
        getTable().addColumn(exportDateColumn, constants.exportDateVm(), "95px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.restoreVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestoreCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<VM>(constants.removeVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });

        getTable().showRefreshButton();
    }

    private void initApplicationsTable() {
        applicationsTable = new ActionCellTable<>(new AbstractDataProvider<String>() {
            @Override
            protected void onRangeChanged(HasData<String> display) {
            }
        }, GWT.<Resources> create(SubTableResources.class));

        AbstractTextColumn<String> nameColumn = new AbstractTextColumn<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };

        applicationsTable.addColumn(nameColumn, constants.installedAppsVm());
        applicationsTable.setWidth("100%"); //$NON-NLS-1$
        applicationsTable.setRowData(new ArrayList<String>());

        getDetailModel().getPropertyChangedEvent().addListener(new IEventListener<EventArgs>() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (getDetailModel().getAppListModel().getItems() != null) {
                    applicationsTable.setRowData(Linq.toList(getDetailModel().getAppListModel().getItems()));
                } else {
                    applicationsTable.setRowData(new ArrayList<String>());
                }
            }
        });
    }

}
