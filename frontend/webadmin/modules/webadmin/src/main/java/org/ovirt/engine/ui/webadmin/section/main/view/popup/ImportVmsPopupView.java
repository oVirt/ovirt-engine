package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.VerticalSplitTable;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportSource;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ImportVmsPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ImportVmsPopupView extends AbstractModelBoundPopupView<ImportVmsModel> implements ImportVmsPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ImportVmsModel, ImportVmsPopupView> { }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportVmsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Style extends CssResource {
        String providersStyle();
    }

    @UiField
    Style style;

    @UiField(provided = true)
    @Path(value = "dataCenters.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StoragePool> DataCentersEditor;

    @UiField(provided = true)
    @Path(value = "importSources.selectedItem")
    @WithElementId
    ListModelListBoxEditor<ImportSource> importSourcesEditor;

    @UiField(provided = true)
    VerticalSplitTable<EntityModel<VM>> splitTable;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<VM>>> externalVms;

    @Ignore
    EntityModelCellTable<ListModel<EntityModel<VM>>> importedVms;

    @UiField
    @Ignore
    public ButtonBase refreshButton;

    @UiField
    @Ignore
    Label message;

    private ImportVmsModel model;

    private ApplicationMessages messages;

    @UiHandler("refreshButton")
    void handleRefreshButtonClick(ClickEvent event) {
        model.reload();
    }

    @Inject
    public ImportVmsPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates, ApplicationMessages messages) {
        super(eventBus, resources);
        this.messages = messages;

        // Initialize Editors
        DataCentersEditor = new ListModelListBoxEditor<StoragePool>(new NullSafeRenderer<StoragePool>() {

            @Override
            protected String renderNullSafe(StoragePool sp) {
                return sp.getName();
            }
        });
        importSourcesEditor = new ListModelListBoxEditor<ImportSource>(new NullSafeRenderer<ImportSource>() {

            @Override
            protected String renderNullSafe(ImportSource is) {
                return new EnumRenderer<ImportSource>().render(is);
            }
        });
        externalVms = new EntityModelCellTable<ListModel<EntityModel<VM>>>(true, false, true);
        importedVms = new EntityModelCellTable<ListModel<EntityModel<VM>>>(true, false, true);
        splitTable =
                new VerticalSplitTable<EntityModel<VM>>(externalVms,
                        importedVms,
                        constants.externalVms(),
                        constants.importedVms());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTables(constants, templates, resources);
        DataCentersEditor.setLabel(constants.dataCenter());
        DataCentersEditor.addWrapperStyleName(style.providersStyle());
        importSourcesEditor.setLabel(constants.hostPopupSourceText());
        importSourcesEditor.addWrapperStyleName(style.providersStyle());
        driver.initialize(this);
    }

    void initEntityModelCellTables(final ApplicationConstants constants,
            final ApplicationTemplates templates,
            final ApplicationResources resources) {
        externalVms.addColumn(new AbstractTextColumnWithTooltip<EntityModel<VM>>() {
            @Override
            public String getValue(EntityModel<VM> externalVmModel) {
                return externalVmModel.getEntity().getName();
            }
        }, constants.name());

        importedVms.addColumn(new AbstractTextColumnWithTooltip<EntityModel<VM>>() {
            @Override
            public String getValue(EntityModel<VM> externalVmModel) {
                return externalVmModel.getEntity().getName();
            }
        }, constants.name());
    }

    @Override
    public void edit(final ImportVmsModel model) {
        this.model = model;
        splitTable.edit(
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
    }

    @Override
    public ImportVmsModel flush() {
        return driver.flush();
    }
}
