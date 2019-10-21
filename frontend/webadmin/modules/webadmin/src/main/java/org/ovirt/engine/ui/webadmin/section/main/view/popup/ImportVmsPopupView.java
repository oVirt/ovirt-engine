package org.ovirt.engine.ui.webadmin.section.main.view.popup;


import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.ovirt.engine.core.common.businessentities.KVMVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.XENVmProviderProperties;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.VerticalSplitTable;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.OvaVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ImportVmsPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.inject.Inject;

public class ImportVmsPopupView extends AbstractModelBoundPopupView<ImportVmsModel> implements ImportVmsPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ImportVmsModel, ImportVmsPopupView> { }

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

    @UiField
    UiCommandButton loadOvaButton;

    @UiField
    UiCommandButton loadXenButton;

    @UiField
    UiCommandButton loadKvmButton;

    @UiField(provided = true)
    @Path("kvmProviders.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Provider<KVMVmProviderProperties>> kvmProvidersEditor;

    @UiField(provided = true)
    @Path("xenProviders.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Provider<XENVmProviderProperties>> xenProvidersEditor;

    @UiField(provided = true)
    @Path("vmwareProviders.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Provider<VmwareVmProviderProperties>> vmwareProvidersEditor;

    @Path("vmwareDatacenter.entity")
    @WithElementId("vmwareDatacenter")
    StringEntityModelTextBoxOnlyEditor vmwareDatacenterEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo vmwareDatacenterEditorWithInfo;

    @UiField
    @Path("esx.entity")
    @WithElementId("esx")
    StringEntityModelTextBoxEditor esxEditor;

    @UiField
    @Path("vCenter.entity")
    @WithElementId("vCenter")
    StringEntityModelTextBoxEditor vCenterEditor;

    @UiField
    @Path("vmwareCluster.entity")
    @WithElementId("vmwareClusterEditor")
    StringEntityModelTextBoxEditor vmwareClusterEditor;

    @UiField(provided = true)
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

    @UiField(provided = true)
    @Path("hosts.selectedItem")
    @WithElementId("hosts")
    ListModelListBoxEditor<VDS> hostsEditor;

    @UiField(provided = true)
    public InfoIcon ovaPathInfoIcon;

    @UiField
    @Path("ovaPath.entity")
    @WithElementId("ovaPath")
    StringEntityModelTextBoxOnlyEditor ovaPathEditor;

    @Path("xenUri.entity")
    @WithElementId("xenUri")
    StringEntityModelTextBoxOnlyEditor xenUriEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo xenUriWithInfo;

    @UiField(provided = true)
    @Path("xenProxyHosts.selectedItem")
    @WithElementId
    ListModelListBoxEditor<VDS> xenProxyHostsEditor;

    @Path("kvmUri.entity")
    @WithElementId("kvmUri")
    StringEntityModelTextBoxOnlyEditor kvmUriEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo kvmUriWithInfo;

    @UiField(provided = true)
    @Path("kvmRequiresAuthentication.entity")
    @WithElementId("kvmRequiresAuthentication")
    EntityModelCheckBoxEditor kvmRequiresAuthenticationEditor;

    @UiField
    @Path("kvmUsername.entity")
    @WithElementId("kvmUsername")
    StringEntityModelTextBoxEditor kvmUsernameEditor;

    @UiField
    @Path("kvmPassword.entity")
    @WithElementId("kvmPassword")
    StringEntityModelPasswordBoxEditor kvmPasswordEditor;

    @UiField(provided = true)
    @Path("kvmProxyHosts.selectedItem")
    @WithElementId
    ListModelListBoxEditor<VDS> kvmProxyHostsEditor;

    @UiField
    @Ignore
    FlowPanel ovaPanel;

    @UiField
    @Ignore
    FlowPanel xenPanel;

    @UiField
    @Ignore
    FlowPanel kvmPanel;

    @UiField
    @Ignore
    Row errorRow;

    @UiField
    @Ignore
    Alert errorMessage;

    @UiField
    @Path("exportPath")
    StringEntityModelLabelEditor exportDomainPath;

    @UiField
    @Path("exportName")
    StringEntityModelLabelEditor exportDomainName;

    @UiField
    @Path("exportDescription")
    StringEntityModelLabelEditor exportDomainDescription;

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public ImportVmsPopupView(EventBus eventBus) {
        super(eventBus);

        // Initialize Editors
        verifyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        dataCentersEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());
        importSourcesEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<ImportSource>() {
            @Override
            protected String renderNullSafe(ImportSource is) {
                return new EnumRenderer<ImportSource>().render(is);
            }
        });
        proxyHostsEditor = new ListModelListBoxEditor<>(new AbstractRenderer<VDS>() {
            @Override
            public String render(VDS object) {
                return object != null ? object.getName() :
                        ConstantsManager.getInstance().getConstants().anyHostInDataCenter();
            }
        });

        hostsEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());

        xenProxyHostsEditor = new ListModelListBoxEditor<>(new AbstractRenderer<VDS>() {
            @Override
            public String render(VDS object) {
                return object != null ? object.getName() :
                        ConstantsManager.getInstance().getConstants().anyHostInDataCenter();
            }
        });

        kvmProxyHostsEditor = new ListModelListBoxEditor<>(new AbstractRenderer<VDS>() {
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

        kvmProvidersEditor = new ListModelListBoxEditor<>(new AbstractRenderer<Provider<KVMVmProviderProperties>>() {
            @Override
            public String render(Provider<KVMVmProviderProperties> provider) {
                return provider == null ? constants.customExternalProvider() : provider.getName();
            }
        });

        xenProvidersEditor = new ListModelListBoxEditor<>(new AbstractRenderer<Provider<XENVmProviderProperties>>() {
            @Override
            public String render(Provider<XENVmProviderProperties> provider) {
                return provider == null ? constants.customExternalProvider() : provider.getName();
            }
        });

        vmwareDatacenterEditor = new StringEntityModelTextBoxOnlyEditor();
        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.vmwareDataCenter());
        vmwareDatacenterEditorWithInfo = new EntityModelWidgetWithInfo(label, vmwareDatacenterEditor);
        vmwareDatacenterEditorWithInfo.setExplanation(templates.italicText(constants.dataCenterInfo()));

        xenUriEditor = new StringEntityModelTextBoxOnlyEditor();
        EnableableFormLabel xenUriLabel = new EnableableFormLabel();
        xenUriLabel.setText(constants.xenUri());
        xenUriWithInfo = new EntityModelWidgetWithInfo(xenUriLabel, xenUriEditor);
        xenUriWithInfo.setExplanation(SafeHtmlUtils.fromString(constants.xenUriInfo()));

        kvmUriEditor = new StringEntityModelTextBoxOnlyEditor();
        EnableableFormLabel kvmUriLabel = new EnableableFormLabel();
        kvmUriLabel.setText(constants.kvmUri());
        kvmUriWithInfo = new EntityModelWidgetWithInfo(kvmUriLabel, kvmUriEditor);
        kvmUriWithInfo.setExplanation(SafeHtmlUtils.fromString(constants.kvmUriInfo()));
        kvmRequiresAuthenticationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        externalVms = new EntityModelCellTable<>(true, false, true);
        importedVms = new EntityModelCellTable<>(true, false, true);
        vmsTable =
                new VerticalSplitTable<>(externalVms,
                        importedVms,
                        constants.externalVms(),
                        constants.importedVms());

        ovaPathInfoIcon = new InfoIcon(templates.italicText(messages.ovaPathInfo()));
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTables();

        driver.initialize(this);
    }

    void initEntityModelCellTables() {
        externalVms.addColumn(new AbstractTextColumn<EntityModel<VM>>() {
            @Override
            public String getValue(EntityModel<VM> externalVmModel) {
                if (externalVmModel instanceof OvaVmModel && ((OvaVmModel) externalVmModel).getOvaFileName() != null) {
                    return externalVmModel.getEntity().getName() + " (" + ((OvaVmModel) externalVmModel).getOvaFileName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                return externalVmModel.getEntity().getName();
            }
        }, constants.name());

        importedVms.addColumn(new AbstractTextColumn<EntityModel<VM>>() {
            @Override
            public String getValue(EntityModel<VM> importedVmModel) {
                if (importedVmModel instanceof OvaVmModel && ((OvaVmModel) importedVmModel).getOvaFileName() != null) {
                    return importedVmModel.getEntity().getName() + " (" + ((OvaVmModel) importedVmModel).getOvaFileName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                return importedVmModel.getEntity().getName();
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

        model.getProblemDescription().getEntityChangedEvent().addListener((ev, object, args) -> updateErrorAndWarning(model));
        updateErrorAndWarning(model);

        updatePanelsVisibility(model);
        model.getImportSources().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updatePanelsVisibility(model));

        loadVmsFromExportDomainButton.addClickHandler(event -> model.loadVmsFromExportDomain());
        loadVmsFromVmwareButton.addClickHandler(event -> model.loadVmsFromVmware());
        loadOvaButton.addClickHandler(event -> model.loadVmFromOva());
        loadXenButton.addClickHandler(event -> model.loadVmsFromXen());
        loadKvmButton.addClickHandler(event -> model.loadVmsFromKvm());
    }

    private void updateErrorAndWarning(ImportVmsModel model) {
        errorRow.setVisible(false);
        enableDefaultCommand(model, true);
        String message = model.getProblemDescription().getEntity();
        if (message == null) {
            return;
        }
        if (model.getProblemDescription().getIsValid()) {
            errorMessage.setType(AlertType.WARNING);
        } else {
            errorMessage.setType(AlertType.DANGER);
            enableDefaultCommand(model, false);
        }
        errorMessage.setText(message);
        errorRow.setVisible(true);
    }

    private void updatePanelsVisibility(ImportVmsModel model) {
        exportPanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.EXPORT_DOMAIN);
        vmwarePanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.VMWARE);
        vmwareProvidersEditor.setVisible(model.getImportSources().getSelectedItem() == ImportSource.VMWARE);
        ovaPanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.OVA);
        xenPanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.XEN);
        xenProvidersEditor.setVisible(model.getImportSources().getSelectedItem() == ImportSource.XEN);
        kvmPanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.KVM);
        kvmProvidersEditor.setVisible(model.getImportSources().getSelectedItem() == ImportSource.KVM);
    }

    @Override
    public ImportVmsModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public HasEnabled getLoadVmsFromExportDomainButton() {
        return loadVmsFromExportDomainButton;
    }

    @Override
    public HasEnabled getLoadVmsFromVmwareButton() {
        return loadVmsFromVmwareButton;
    }

    @Override
    public HasEnabled getLoadOvaButton() {
        return loadOvaButton;
    }

    @Override
    public HasEnabled getLoadXenButton() {
        return loadXenButton;
    }

    @Override
    public HasEnabled getLoadKvmButton() {
        return loadKvmButton;
    }

    private void enableDefaultCommand(ImportVmsModel model, boolean enable) {
        if (model.getDefaultCommand() != null) {
            model.getDefaultCommand().setIsExecutionAllowed(enable);
        }
    }
}
