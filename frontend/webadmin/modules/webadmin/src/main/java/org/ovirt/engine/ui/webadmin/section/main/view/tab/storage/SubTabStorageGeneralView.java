package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.StorageSizeLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.label.PercentLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabStorageGeneralView extends AbstractSubTabFormView<StorageDomain, StorageListModel, StorageGeneralModel> implements SubTabStorageGeneralPresenter.ViewDef, Editor<StorageGeneralModel> {

    interface Driver extends UiCommonEditorDriver<StorageGeneralModel, SubTabStorageGeneralView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabStorageGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Ignore
    StringValueLabel storageDomainId = new StringValueLabel();

    @Ignore
    StorageSizeLabel<Integer> totalSize = new StorageSizeLabel<>();

    @Ignore
    StorageSizeLabel<Integer> availableSize = new StorageSizeLabel<>();

    @Ignore
    StorageSizeLabel<Integer> usedSize = new StorageSizeLabel<>();

    @Ignore
    StorageSizeLabel<Integer> allocatedSize = new StorageSizeLabel<>();

    @Ignore
    PercentLabel<Integer> overAllocationRatio = new PercentLabel<>();

    @Ignore
    StringValueLabel warningLowSpaceIndicator = new StringValueLabel();

    @Ignore
    StorageSizeLabel<Integer> criticalSpaceActionBlocker = new StorageSizeLabel<>();

    @Path("numOfImages")
    StringValueLabel numOfImages = new StringValueLabel();

    @Path("path")
    StringValueLabel path = new StringValueLabel();

    @Path("vfsType")
    StringValueLabel vfsType = new StringValueLabel();

    @Path("mountOptions")
    StringValueLabel mountOptions = new StringValueLabel();

    @Path("nfsVersion")
    StringValueLabel nfsVersion = new StringValueLabel();

    @Path("retransmissions")
    StringValueLabel retransmissions = new StringValueLabel();

    @Path("timeout")
    StringValueLabel timeout = new StringValueLabel();

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SubTabStorageGeneralView(DetailModelProvider<StorageListModel, StorageGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 16);
        formBuilder.setRelativeColumnWidth(0, 12);
        formBuilder.addFormItem(new FormItem(constants.storageDomainIdGeneral(), storageDomainId, 0, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.sizeStorageGeneral(), totalSize, 1, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.availableStorageGeneral(), availableSize, 2, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.usedStorageGeneral(), usedSize, 3, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.allocatedStorageGeneral(), allocatedSize, 4, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.overAllocRatioStorageGeneral(), overAllocationRatio, 5, 0) {
            @Override
            public boolean getIsAvailable() {
                StorageDomain entity = getDetailModel().getEntity();
                StorageDomainType storageDomainType = entity != null ? entity.getStorageDomainType() : null;
                return !StorageDomainType.ISO.equals(storageDomainType)
                        && !StorageDomainType.ImportExport.equals(storageDomainType);
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.numberOfImagesStorageGeneral(), numOfImages, 6, 0) {
            @Override
            public boolean getIsAvailable() {
                StorageDomain entity = getDetailModel().getEntity();
                return entity != null && entity.getStorageDomainType().isDataDomain();
            }
        }, 2, 10);
       formBuilder.addFormItem(new FormItem(7, 0), 2, 10); // empty cell
        formBuilder.addFormItem(new FormItem(constants.pathStorageGeneral(), path, 8, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getPath() != null;
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.vfsTypeStorageGeneral(), vfsType, 9, 0) {
            @Override
            public boolean getIsAvailable() {
                return (getDetailModel().getIsPosix() || getDetailModel().getIsGlusterfs())
                        && getDetailModel().getVfsType() != null
                        && !getDetailModel().getVfsType().isEmpty();
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.mountOptionsGeneral(), mountOptions, 10, 0) {
            @Override
            public boolean getIsAvailable() {
                return (getDetailModel().getIsPosix() || getDetailModel().getIsGlusterfs())
                        && getDetailModel().getMountOptions() != null
                        && !getDetailModel().getMountOptions().isEmpty();
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.nfsVersionGeneral(), nfsVersion, 11, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsNfs() && getDetailModel().getNfsVersion() != null;
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.nfsRetransmissionsGeneral(), retransmissions, 12, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsNfs() && getDetailModel().getRetransmissions() != null;
            }
        }, 2, 10);
        formBuilder.addFormItem(new FormItem(constants.nfsTimeoutGeneral(), timeout, 13, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsNfs() && getDetailModel().getTimeout() != null;
            }
        }, 2, 10);

        formBuilder.addFormItem(new FormItem(constants.warningLowSpaceIndicator(), warningLowSpaceIndicator, 14, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.criticalSpaceActionBlocker(), criticalSpaceActionBlocker, 15, 0), 2, 10);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(StorageDomain selectedItem) {
        driver.edit(getDetailModel());

        // Required because of StorageGeneralModel.getEntity() returning Object
        StorageDomain entity = getDetailModel().getEntity();
        if (entity != null) {
            storageDomainId.setValue(entity.getId().toString());
            totalSize.setValue(entity.getTotalDiskSize());
            availableSize.setValue(entity.getAvailableDiskSize());
            usedSize.setValue(entity.getUsedDiskSize());
            allocatedSize.setValue(entity.getCommittedDiskSize());
            overAllocationRatio.setValue(entity.getStorageDomainOverCommitPercent());
            warningLowSpaceIndicator.setValue(messages.percentWithValueInGiB(
                    entity.getWarningLowSpaceIndicator(), entity.getWarningLowSpaceSize()));
            criticalSpaceActionBlocker.setValue(entity.getCriticalSpaceActionBlocker());
        }

        formBuilder.update(getDetailModel());
    }

}
