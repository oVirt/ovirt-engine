package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.VerticalSplitTable;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.OvaTemplateModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.ImportTemplatesModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ImportTemplatesPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.inject.Inject;

public class ImportTemplatesPopupView extends AbstractModelBoundPopupView<ImportTemplatesModel> implements ImportTemplatesPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ImportTemplatesModel, ImportTemplatesPopupView> { }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportTemplatesPopupView> {
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
    VerticalSplitTable<ListModel<EntityModel<VmTemplate>>, EntityModel<VmTemplate>> templatesTable;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<VmTemplate>>> externalTemplates;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<VmTemplate>>> importedTemplates;

    @UiField
    @Ignore
    FlowPanel exportPanel;

    @UiField
    UiCommandButton loadVmsFromExportDomainButton;

    @UiField
    UiCommandButton loadOvaButton;


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

    @UiField
    @Ignore
    FlowPanel ovaPanel;

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
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    @Inject
    public ImportTemplatesPopupView(EventBus eventBus) {
        super(eventBus);

        // Initialize Editors
        dataCentersEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());
        importSourcesEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<ImportSource>() {
            @Override
            protected String renderNullSafe(ImportSource is) {
                return new EnumRenderer<ImportSource>().render(is);
            }
        });

        hostsEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());

        externalTemplates = new EntityModelCellTable<>(true, false, true);
        importedTemplates = new EntityModelCellTable<>(true, false, true);
        templatesTable =
                new VerticalSplitTable<>(externalTemplates,
                        importedTemplates,
                        constants.externalVms(),
                        constants.importedVms());

        ovaPathInfoIcon = new InfoIcon(templates.italicText(messages.ovaPathInfo()));
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTables();

        driver.initialize(this);
    }

    void initEntityModelCellTables() {
        externalTemplates.addColumn(new AbstractTextColumn<EntityModel<VmTemplate>>() {
            @Override
            public String getValue(EntityModel<VmTemplate> externalVmModel) {
                if (externalVmModel instanceof OvaTemplateModel && ((OvaTemplateModel) externalVmModel).getOvaFileName() != null) {
                    return externalVmModel.getEntity().getName() + " (" + ((OvaTemplateModel) externalVmModel).getOvaFileName() + ")";//$NON-NLS-1$ //$NON-NLS-2$
                }
                return externalVmModel.getEntity().getName();
            }
        }, constants.name());

        importedTemplates.addColumn(new AbstractTextColumn<EntityModel<VmTemplate>>() {
            @Override
            public String getValue(EntityModel<VmTemplate> externalVmModel) {
                if (externalVmModel instanceof OvaTemplateModel && ((OvaTemplateModel) externalVmModel).getOvaFileName() != null) {
                    return externalVmModel.getEntity().getName() + " (" + ((OvaTemplateModel) externalVmModel).getOvaFileName() + ")";//$NON-NLS-1$ //$NON-NLS-2$
                }
                return externalVmModel.getEntity().getName();
            }
        }, constants.name());
    }

    @Override
    public void edit(final ImportTemplatesModel model) {
        driver.edit(model);
        templatesTable.edit(
                model.getExternalTemplatesModels(),
                model.getImportedTemplatesModels(),
                model.getAddImportCommand(),
                model.getCancelImportCommand());

        model.getProblemDescription().getEntityChangedEvent().addListener((ev, object, args) -> updateErrorAndWarning(model));
        updateErrorAndWarning(model);

        updatePanelsVisibility(model);
        model.getImportSources().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updatePanelsVisibility(model));

        loadVmsFromExportDomainButton.addClickHandler(event -> model.loadTemplatesFromExportDomain());
        loadOvaButton.addClickHandler(event -> model.loadVmFromOva());
    }

    private void updateErrorAndWarning(ImportTemplatesModel model) {
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

    private void updatePanelsVisibility(ImportTemplatesModel model) {
        exportPanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.EXPORT_DOMAIN);
        ovaPanel.setVisible(model.getImportSources().getSelectedItem() == ImportSource.OVA);
    }

    @Override
    public ImportTemplatesModel flush() {
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
    public HasEnabled getLoadOvaButton() {
        return loadOvaButton;
    }

    private void enableDefaultCommand(ImportTemplatesModel model, boolean enable) {
        if (model.getDefaultCommand() != null) {
            model.getDefaultCommand().setIsExecutionAllowed(enable);
        }
    }
}
