package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.VerticalSplitTable;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ImportVmsPopupPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ImportVmsPopupView extends AbstractModelBoundPopupView<ImportVmsModel> implements ImportVmsPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ImportVmsModel, ImportVmsPopupView> { }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportVmsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Path("dataCenters.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StoragePool> dataCentersEditor;

    @UiField(provided = true)
    @Path("importSources.selectedItem")
    @WithElementId
    ListModelListBoxEditor<ImportSource> importSourcesEditor;

    @UiField(provided = true)
    @Path("proxyHosts.selectedItem")
    @WithElementId
    ListModelListBoxEditor<VDS> proxyHostsEditor;

    @UiField(provided = true)
    VerticalSplitTable<ListModel<EntityModel<VM>>, EntityModel<VM>> vmsTable;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<VM>>> externalVms;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<VM>>> importedVms;

    @UiField
    @Ignore
    FlowPanel exportPanel;

    @UiField
    UiCommandButton loadVmsFromExportDomainButton;

    @UiField
    UiCommandButton loadVmsFromVmwareButton;

    @UiField(provided = true)
    @Path("vmwareProviders.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Provider<VmwareVmProviderProperties>> vmwareProvidersEditor;

    @UiField
    @Path("vCenter.entity")
    @WithElementId("vCenter")
    StringEntityModelTextBoxEditor vCenterEditor;

    @UiField
    @Path("esx.entity")
    @WithElementId("esx")
    StringEntityModelTextBoxEditor esxEditor;

    @UiField
    @Path("vmwareDatacenter.entity")
    @WithElementId("vmwareDatacenter")
    StringEntityModelTextBoxEditor vmwareDatacenterEditor;

    @UiField
    @Path("verify.entity")
    @WithElementId("verify")
    EntityModelCheckBoxEditor verifyEditor;

    @UiField
    @Path("username.entity")
    @WithElementId("username")
    StringEntityModelTextBoxEditor usernameEditor;

    @UiField
    @Path("password.entity")
    @WithElementId("password")
    StringEntityModelPasswordBoxEditor passwordEditor;

    @UiField
    @Ignore
    FlowPanel vmwarePanel;

    @UiField
    @Ignore
    Label message;

    @UiField
    @Path("exportPath")
    StringEntityModelLabelEditor exportDomainPath;

    @UiField
    @Path("exportName")
    StringEntityModelLabelEditor exportDomainName;

    @UiField
    @Path("exportDescription")
    StringEntityModelLabelEditor exportDomainDescription;

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ImportVmsPopupView(EventBus eventBus) {
        super(eventBus);

        // Initialize Editors
        dataCentersEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());
        importSourcesEditor = new ListModelListBoxEditor<ImportSource>(new NullSafeRenderer<ImportSource>() {
            @Override
            protected String renderNullSafe(ImportSource is) {
                return new EnumRenderer<ImportSource>().render(is);
            }
        });
        proxyHostsEditor = new ListModelListBoxEditor<VDS>(new AbstractRenderer<VDS>() {
            @Override
            public String render(VDS object) {
                return object != null ? object.getName() :
                    ConstantsManager.getInstance().getConstants().anyHostInDataCenter();
            }
        });
        vmwareProvidersEditor = new ListModelListBoxEditor<>(new AbstractRenderer<Provider<VmwareVmProviderProperties>>() {
            @Override
            public String render(Provider<VmwareVmProviderProperties> provider) {
                return provider == null ? constants.customExternalProvider() : provider.getName();
            }
        });

        externalVms = new EntityModelCellTable<>(true, false, true);
        importedVms = new EntityModelCellTable<>(true, false, true);
        vmsTable =
                new VerticalSplitTable<>(externalVms,
                        importedVms,
                        constants.externalVms(),
                        constants.importedVms());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTables();

        dataCentersEditor.setLabel(constants.dataCenter());
        importSourcesEditor.setLabel(constants.importSource());
        vmwareProvidersEditor.setLabel(constants.externalProviderLabel());

        exportDomainName.setLabel(constants.nameLabel());
        exportDomainPath.setLabel(constants.pathStorageGeneral());
        exportDomainDescription.setLabel(constants.descriptionLabel());

        vCenterEditor.setLabel(constants.vCenter());
        esxEditor.setLabel(constants.esxi());
        vmwareDatacenterEditor.setLabel(constants.vmwareDataCenter());
        verifyEditor.setLabel(constants.vmwareVerifyCredentials());
        usernameEditor.setLabel(constants.usernameProvider());
        passwordEditor.setLabel(constants.passwordProvider());
        proxyHostsEditor.setLabel(constants.proxyHost());

        loadVmsFromExportDomainButton.setLabel(constants.loadLabel());
        loadVmsFromVmwareButton.setLabel(constants.loadLabel());
        driver.initialize(this);
    }

    void initEntityModelCellTables() {
        externalVms.addColumn(new AbstractTextColumn<EntityModel<VM>>() {
            @Override
            public String getValue(EntityModel<VM> externalVmModel) {
                return externalVmModel.getEntity().getName();
            }
        }, constants.name());

        importedVms.addColumn(new AbstractTextColumn<EntityModel<VM>>() {
            @Override
            public String getValue(EntityModel<VM> externalVmModel) {
                return externalVmModel.getEntity().getName();
            }
        }, constants.name());
    }

    @Override
    public void edit(final ImportVmsModel model) {
        vmsTable.edit(
                model.getExternalVmModels(),
                model.getImportedVmModels(),
                model.getAddImportCommand(),
                model.getCancelImportCommand());
        driver.edit(model);

        model.getImportSourceValid().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            public void eventRaised(org.ovirt.engine.ui.uicompat.Event<? extends EventArgs> ev, Object object, EventArgs args) {
                if (Boolean.FALSE.equals(model.getImportSourceValid().getEntity())) {
                    message.setText(model.getImportSourceValid().getMessage());
                }
            };
        });

        updatePanelsVisibility(model);
        model.getImportSources().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updatePanelsVisibility(model);
            }
        });

        loadVmsFromExportDomainButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.loadVmsFromExportDomain();
            }
        });
        loadVmsFromVmwareButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.loadVmsFromVmware();
            }
        });
    }

    private void updatePanelsVisibility(ImportVmsModel model) {
        exportPanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.EXPORT_DOMAIN);
        vmwarePanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.VMWARE);
        vmwareProvidersEditor.setVisible(model.getImportSources().getSelectedItem() == ImportSource.VMWARE);
    }

    @Override
    public ImportVmsModel flush() {
        return driver.flush();
    }
}
