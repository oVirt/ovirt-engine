package org.ovirt.engine.ui.webadmin.section.main.view.popup.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.qouta.QuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.QuotaModelProvider;
import org.ovirt.engine.ui.webadmin.widget.editor.IVdcQueryableCellTable;
import org.ovirt.engine.ui.webadmin.widget.table.column.NullableButtonCell;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class QuotaPopupView extends AbstractModelBoundPopupView<QuotaModel> implements QuotaPopupPresenterWidget.ViewDef {

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField
    @Ignore
    Label memAndCpuLabel;

    @UiField
    @Ignore
    Label storageLabel;

    @UiField(provided = true)
    @Path(value = "globalClusterQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor globalClusterQuotaRadioButtonEditor;

    @UiField(provided = true)
    @Path(value = "specificClusterQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor specificClusterQuotaRadioButtonEditor;

    @UiField(provided = true)
    @Path(value = "globalStorageQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor globalStorageQuotaRadioButtonEditor;

    @UiField(provided = true)
    @Path(value = "specificStorageQuota.entity")
    @WithElementId
    EntityModelRadioButtonEditor specificStorageQuotaRadioButtonEditor;

    @UiField
    @Ignore
    ScrollPanel clusterQuotaTableContainer;

    @Ignore
    private IVdcQueryableCellTable<QuotaVdsGroup, ListModel> quotaClusterTable;

    @Ignore
    private IVdcQueryableCellTable<QuotaStorage, ListModel> quotaStorageTable;

    @UiField
    @Ignore
    ScrollPanel storageQuotaTableContainer;

    private Column<QuotaVdsGroup, Boolean> isClusterInQuotaColumn = null;
    private Column<QuotaStorage, Boolean> isStorageInQuota = null;

    private QuotaModel model;

    private QuotaModelProvider quotaModelProvider;

    private boolean firstTime = false;

    ArrayList<Guid> selectedClusterGuid = new ArrayList<Guid>();
    ArrayList<Guid> selectedStorageGuid = new ArrayList<Guid>();

    interface Driver extends SimpleBeanEditorDriver<QuotaModel, QuotaPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, QuotaPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<QuotaPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public QuotaPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants,
            QuotaModelProvider quotaModelProvider) {
        super(eventBus, resources);
        this.quotaModelProvider = quotaModelProvider;
        initListBoxEditors();
        initRadioButtonEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        Driver.driver.initialize(this);
        initTables();
    }

    private void initTables() {
        initQuotaClusterTable();
        initQuotaStorageTable();
    }

    private void initQuotaStorageTable() {
        quotaStorageTable = new IVdcQueryableCellTable<QuotaStorage, ListModel>();
        storageQuotaTableContainer.add(quotaStorageTable);

        isStorageInQuota = new Column<QuotaStorage, Boolean>(
                new CheckboxCell(false, true)) {
            @Override
            public Boolean getValue(QuotaStorage object) {
                if (selectedStorageGuid.contains(object.getStorageId()) || object.getStorageSizeGB() != null) {
                    return true;
                }
                return false;
            }
        };

        isStorageInQuota.setFieldUpdater(new FieldUpdater<QuotaStorage, Boolean>() {
            @Override
            public void update(int index, QuotaStorage object, Boolean value) {
                if (value) {
                    selectedStorageGuid.add(object.getStorageId());
                } else {
                    selectedStorageGuid.remove(object.getStorageId());
                    object.setStorageSizeGB(null);
                }
                if ((Boolean) model.getGlobalStorageQuota().getEntity()) {
                    quotaStorageTable.edit(model.getQuotaStorages());
                } else {
                    quotaStorageTable.edit(model.getAllDataCenterStorages());
                }
            }
        });

        quotaStorageTable.addColumn(new TextColumnWithTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                if (object.getStorageName() == null || object.getStorageName().length() == 0) {
                    return "Unlimited Storage";
                }
                return object.getStorageName();
            }
        }, "Storage Name");

        quotaStorageTable.addColumn(new TextColumnWithTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                String str = "";
                if (object.getStorageSizeGB() == null) {
                    return "";
                } else if (object.getStorageSizeGB() == -1) {
                    str = "/*";
                } else {
                    str = "/" + object.getStorageSizeGB();
                }
                return object.getStorageSizeGBUsage() + str + " GB";
            }
        }, "Quota");

        NullableButtonCell editCellButton = new NullableButtonCell();
        Column<QuotaStorage, String> editColumn = new Column<QuotaStorage, String>(editCellButton) {
            @Override
            public String getValue(QuotaStorage object) {
                if ((Boolean) model.getGlobalStorageQuota().getEntity()
                        || ((Boolean) model.getSpecificStorageQuota().getEntity() && selectedStorageGuid.contains(object.getStorageId()))) {
                    return "Edit";
                }
                return null;
            }
        };

        quotaStorageTable.addColumn(editColumn, "", "50px");
        editColumn.setFieldUpdater(new FieldUpdater<QuotaStorage, String>() {
            @Override
            public void update(int index, QuotaStorage object, String value) {
                model.editQuotaStorage(object);
            }
        });
    }

    private void initQuotaClusterTable() {
        quotaClusterTable = new IVdcQueryableCellTable<QuotaVdsGroup, ListModel>();
        clusterQuotaTableContainer.add(quotaClusterTable);

        isClusterInQuotaColumn = new Column<QuotaVdsGroup, Boolean>(
                new CheckboxCell(false, true)) {
            @Override
            public Boolean getValue(QuotaVdsGroup object) {
                if (selectedClusterGuid.contains(object.getVdsGroupId()) || object.getMemSizeMB() != null) {
                    return true;
                }
                return false;
            }
        };
        isClusterInQuotaColumn.setFieldUpdater(new FieldUpdater<QuotaVdsGroup, Boolean>() {
            @Override
            public void update(int index, QuotaVdsGroup object, Boolean value) {
                if (value) {
                    selectedClusterGuid.add(object.getVdsGroupId());
                } else {
                    selectedClusterGuid.remove(object.getVdsGroupId());
                    object.setMemSizeMB(null);
                    object.setVirtualCpu(null);
                }
                if ((Boolean) model.getGlobalClusterQuota().getEntity()) {
                    quotaClusterTable.edit(model.getQuotaClusters());
                } else {
                    quotaClusterTable.edit(model.getAllDataCenterClusters());
                }
            }
        });

        quotaClusterTable.addColumn(new TextColumnWithTooltip<QuotaVdsGroup>() {
            @Override
            public String getValue(QuotaVdsGroup object) {
                if (object.getVdsGroupName() == null || object.getVdsGroupName().length() == 0) {
                    return "Unlimited Cluster";
                }
                return object.getVdsGroupName();
            }
        }, "Cluster Name");

        quotaClusterTable.addColumn(new TextColumnWithTooltip<QuotaVdsGroup>() {
            @Override
            public String getValue(QuotaVdsGroup object) {
                String str = "";
                if (object.getMemSizeMB() == null) {
                    return "";
                } else if (object.getMemSizeMB() == -1) {
                    str = "/*";
                } else {
                    str = "/" + object.getMemSizeMB();
                }
                return object.getMemSizeMBUsage() + str + " GB";
            }
        }, "Quota of Mem");

        quotaClusterTable.addColumn(new TextColumnWithTooltip<QuotaVdsGroup>() {
            @Override
            public String getValue(QuotaVdsGroup object) {
                String str = "";
                if (object.getVirtualCpu() == null) {
                    return "";
                } else if (object.getVirtualCpu() == -1) {
                    str = "/*";
                } else {
                    str = "/" + object.getVirtualCpu();
                }
                return object.getVirtualCpuUsage() + str + " vCPUs";
            }
        }, "Quota of vCPU");

        NullableButtonCell editCellButton = new NullableButtonCell();
        Column<QuotaVdsGroup, String> editColumn = new Column<QuotaVdsGroup, String>(editCellButton) {
            @Override
            public String getValue(QuotaVdsGroup object) {
                if ((Boolean) model.getGlobalClusterQuota().getEntity()
                        || ((Boolean) model.getSpecificClusterQuota().getEntity() && selectedClusterGuid.contains(object.getVdsGroupId()))) {
                    return "Edit";
                }
                return null;
            }
        };

        quotaClusterTable.addColumn(editColumn, "", "50px");
        editColumn.setFieldUpdater(new FieldUpdater<QuotaVdsGroup, String>() {
            @Override
            public void update(int index, QuotaVdsGroup object, String value) {
                model.editQuotaCluster(object);
            }
        });
    }

    private void initRadioButtonEditors() {
        globalClusterQuotaRadioButtonEditor = new EntityModelRadioButtonEditor("1");
        specificClusterQuotaRadioButtonEditor = new EntityModelRadioButtonEditor("1");
        globalStorageQuotaRadioButtonEditor = new EntityModelRadioButtonEditor("2");
        specificStorageQuotaRadioButtonEditor = new EntityModelRadioButtonEditor("2");
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_pool) object).getname();
            }
        });
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel("Name");
        descriptionEditor.setLabel("Description");
        dataCenterEditor.setLabel("Data Center");
        memAndCpuLabel.setText("Mem & CPU");
        storageLabel.setText("Storage");
        globalClusterQuotaRadioButtonEditor.setLabel("Unlimited Quota for all Clusters");
        specificClusterQuotaRadioButtonEditor.setLabel("Use Quota for specific Clusters");
        globalStorageQuotaRadioButtonEditor.setLabel("Unlimited Quota for all Storages");
        specificStorageQuotaRadioButtonEditor.setLabel("Use Quota for specific Storages");
    }

    @Override
    public void edit(QuotaModel object) {
        this.model = object;
        if (!firstTime) {
            registerHandlers();
            quotaModelProvider.setModel(object);
            firstTime = true;
        }

        quotaClusterTable.edit(object.getQuotaClusters());
        quotaStorageTable.edit(object.getQuotaStorages());
        Driver.driver.edit(object);
    }

    private void registerHandlers() {
        model.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("Window".equals(propName) && model.getWindow() == null) {
                    if ((Boolean) model.getSpecificClusterQuota().getEntity()) {
                        quotaClusterTable.edit(model.getAllDataCenterClusters());
                    } else {
                        quotaClusterTable.edit(model.getQuotaClusters());
                    }
                    if ((Boolean) model.getSpecificStorageQuota().getEntity()) {
                        quotaStorageTable.edit(model.getAllDataCenterStorages());
                    } else {
                        quotaStorageTable.edit(model.getQuotaStorages());
                    }
                }
            }
        });

        model.getSpecificClusterQuota().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) model.getSpecificClusterQuota().getEntity()) {
                    quotaClusterTable.insertColumn(0, isClusterInQuotaColumn);
                    quotaClusterTable.edit(model.getAllDataCenterClusters());
                } else {
                    quotaClusterTable.removeColumn(isClusterInQuotaColumn);
                    quotaClusterTable.edit(model.getQuotaClusters());
                }
            }
        });

        model.getSpecificStorageQuota().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ((Boolean) model.getSpecificStorageQuota().getEntity()) {
                    quotaStorageTable.insertColumn(0, isStorageInQuota);
                    quotaStorageTable.edit(model.getAllDataCenterStorages());
                } else {
                    quotaStorageTable.removeColumn(isStorageInQuota);
                    quotaStorageTable.edit(model.getQuotaStorages());
                }
            }
        });

    }

    @Override
    public QuotaModel flush() {
        quotaClusterTable.flush();
        quotaStorageTable.flush();
        return Driver.driver.flush();
    }
}
