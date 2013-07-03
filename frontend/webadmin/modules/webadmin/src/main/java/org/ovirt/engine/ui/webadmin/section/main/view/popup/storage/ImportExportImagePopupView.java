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
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportExportRepoImageBaseModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
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
    public ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<Object> storageDomainEditor;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<Object> quotaEditor;

    @UiField(provided = true)
    SimplePanel imageListPanel;

    @Ignore
    EntityModelCellTable<ListModel> imageList;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public ImportExportImagePopupView(EventBus eventBus, ApplicationResources resources,
                                      final ApplicationConstants constants) {
        super(eventBus, resources);

        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((StoragePool) object).getName();
            }
        });
        dataCenterEditor.setLabel(constants.dataCenter());

        storageDomainEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((StorageDomain) object).getStorageName();
            }
        });
        storageDomainEditor.setLabel(constants.domainNameStorage());

        quotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });
        quotaEditor.setLabel(constants.quota());

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

        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!(args instanceof PropertyChangedEventArgs)) {
                    return;
                }

                PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;

                if ("ImportExportEntities".equals(changedArgs.PropertyName) //$NON-NLS-1$
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
