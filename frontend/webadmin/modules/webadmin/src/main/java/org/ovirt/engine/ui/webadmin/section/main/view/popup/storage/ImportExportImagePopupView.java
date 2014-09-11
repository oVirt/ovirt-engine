package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportExportRepoImageBaseModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.ImportExportImagePopupPresenterWidget;


public class ImportExportImagePopupView extends AbstractModelBoundPopupView<ImportExportRepoImageBaseModel> implements
        ImportExportImagePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ImportExportRepoImageBaseModel, ImportExportImagePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ImportExportImagePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    FlowPanel messagePanel;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<VDSGroup> clusterEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<StorageDomain> storageDomainEditor;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<Quota> quotaEditor;

    @UiField(provided = true)
    SimplePanel imageListPanel;

    @UiField(provided = true)
    @Path(value = "importAsTemplate.entity")
    @WithElementId("importAsTemplate")
    public EntityModelCheckBoxEditor importAsTemplateEditor;

    @Ignore
    EntityModelCellTable<ListModel> imageList;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public ImportExportImagePopupView(EventBus eventBus, ApplicationResources resources,
                                      final ApplicationConstants constants) {
        super(eventBus, resources);

        dataCenterEditor = new ListModelListBoxEditor<StoragePool>(new NullSafeRenderer<StoragePool>() {
            @Override
            public String renderNullSafe(StoragePool storagePool) {
                return storagePool.getName();
            }
        });
        dataCenterEditor.setLabel(constants.dataCenter());

        clusterEditor = new ListModelListBoxEditor<VDSGroup>(new NullSafeRenderer<VDSGroup>() {
            @Override
            public String renderNullSafe(VDSGroup vdsGroup) {
                return vdsGroup.getName();
            }
        });
        clusterEditor.setLabel(constants.makeTemplateClusterLabel());

        storageDomainEditor = new ListModelListBoxEditor<StorageDomain>(new NullSafeRenderer<StorageDomain>() {
            @Override
            public String renderNullSafe(StorageDomain storageDomain) {
                return storageDomain.getStorageName();
            }
        });
        storageDomainEditor.setLabel(constants.domainNameStorage());

        quotaEditor = new ListModelListBoxEditor<Quota>(new NullSafeRenderer<Quota>() {
            @Override
            public String renderNullSafe(Quota quota) {
                return quota.getQuotaName();
            }
        });
        quotaEditor.setLabel(constants.quota());

        importAsTemplateEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        importAsTemplateEditor.setLabel(constants.importAsTemplate());

        imageListPanel = new SimplePanel();

        imageList = new EntityModelCellTable<ListModel>(SelectionMode.NONE, true);
        imageList.addEntityModelColumn(new EntityModelTextColumn<Object>() {
            @Override
            public String getText(Object image) {
                if (image instanceof RepoImage) {
                    return ((RepoImage) image).getRepoImageTitle();
                } else if (image instanceof DiskImage) {
                    return ((DiskImage) image).getDiskAlias();
                }
                return constants.unknown();
            }
        }, constants.fileNameIso());
        imageList.addEntityModelColumn(new EntityModelTextColumn<Object>() {
            @Override
            public String getText(Object image) {
                if (image instanceof RepoImage) {
                    return ((RepoImage) image).getFileType().toString();
                } else if (image instanceof DiskImage) {
                    return ImageFileType.Disk.toString();
                }
                return constants.unknown();
            }
        }, constants.typeIso());
        imageList.addEntityModelColumn(new DiskSizeColumn<EntityModel>() {
            @Override
            protected Long getRawValue(EntityModel object) {
                if (object.getEntity() instanceof RepoImage) {
                    return ((RepoImage) (object.getEntity())).getSize();
                } else if (object.getEntity() instanceof DiskImage) {
                    return ((DiskImage) (object.getEntity())).getSizeInGigabytes();
                }
                return null;
            }
        }, constants.actualSizeTemplate());

        imageList.setWidth("100%", true); //$NON-NLS-1$
        imageListPanel.setWidget(imageList);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void edit(final ImportExportRepoImageBaseModel model) {
        driver.edit(model);

        importAsTemplateEditor.setVisible(model.showImportAsTemplateOptions());
        clusterEditor.setVisible(model.showImportAsTemplateOptions());

        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("ImportExportEntities".equals(args.propertyName) //$NON-NLS-1$
                        && model.getEntities() != null) {
                    imageList.setRowData(model.getEntities());
                }
            }
        });
    }

    @Override
    public ImportExportRepoImageBaseModel flush() {
        return driver.flush();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);

        messagePanel.setVisible(message != null && !message.isEmpty());
        messagePanel.clear();
        messagePanel.add(new Label(message));
    }

}
