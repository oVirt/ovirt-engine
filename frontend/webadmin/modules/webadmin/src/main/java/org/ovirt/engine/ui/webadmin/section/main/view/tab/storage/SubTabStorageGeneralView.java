package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.label.PercentLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabStorageGeneralView extends AbstractSubTabFormView<StorageDomain, StorageListModel, StorageGeneralModel> implements SubTabStorageGeneralPresenter.ViewDef, Editor<StorageGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<StorageGeneralModel, SubTabStorageGeneralView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabStorageGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Ignore
    // TODO Primitive getters not supported in 2.2
    DiskSizeLabel<Integer> totalSize = new DiskSizeLabel<Integer>();

    @Ignore
    // TODO Primitive getters not supported in 2.2
    DiskSizeLabel<Integer> availableSize = new DiskSizeLabel<Integer>();

    @Ignore
    // TODO Primitive getters not supported in 2.2
    DiskSizeLabel<Integer> usedSize = new DiskSizeLabel<Integer>();

    @Ignore
    // TODO Primitive getters not supported in 2.2
    DiskSizeLabel<Integer> allocatedSize = new DiskSizeLabel<Integer>();

    @Ignore
    // TODO Primitive getters not supported in 2.2
    PercentLabel<Integer> overAllocationRatio = new PercentLabel<Integer>();

    @Path("path")
    TextBoxLabel path = new TextBoxLabel();

    @Path("vfsType")
    TextBoxLabel vfsType = new TextBoxLabel();

    @Path("mountOptions")
    TextBoxLabel mountOptions = new TextBoxLabel();

    @Path("nfsVersion")
    TextBoxLabel nfsVersion = new TextBoxLabel();

    @Path("retransmissions")
    TextBoxLabel retransmissions = new TextBoxLabel();

    @Path("timeout")
    TextBoxLabel timeout = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public SubTabStorageGeneralView(DetailModelProvider<StorageListModel, StorageGeneralModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 12);

        formBuilder.addFormItem(new FormItem(constants.sizeStorageGeneral(), totalSize, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.availableStorageGeneral(), availableSize, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.usedStorageGeneral(), usedSize, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.allocatedStorageGeneral(), allocatedSize, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.overAllocRatioStorageGeneral(), overAllocationRatio, 4, 0) {
            @Override
            public boolean getIsAvailable() {
                StorageDomain entity = (StorageDomain) getDetailModel().getEntity();
                StorageDomainType storageDomainType = entity != null ? entity.getStorageDomainType() : null;
                return !StorageDomainType.ISO.equals(storageDomainType)
                        && !StorageDomainType.ImportExport.equals(storageDomainType);
            }
        });
        formBuilder.addFormItem(new FormItem(5, 0)); // empty cell
        formBuilder.addFormItem(new FormItem(constants.pathStorageGeneral(), path, 6, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getPath() != null;
            }
        });
        formBuilder.addFormItem(new FormItem(constants.vfsTypeStorageGeneral(), vfsType, 7, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsPosix() && getDetailModel().getVfsType() != null
                        && !getDetailModel().getVfsType().isEmpty();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.mountOptionsGeneral(), mountOptions, 8, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsPosix() && getDetailModel().getMountOptions() != null
                        && !getDetailModel().getMountOptions().isEmpty();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.nfsVersionGeneral(), nfsVersion, 9, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsNfs() && getDetailModel().getNfsVersion() != null;
            }
        });
        formBuilder.addFormItem(new FormItem(constants.nfsRetransmissionsGeneral(), retransmissions, 10, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsNfs() && getDetailModel().getRetransmissions() != null;
            }
        });
        formBuilder.addFormItem(new FormItem(constants.nfsTimeoutGeneral(), timeout, 11, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getIsNfs() && getDetailModel().getTimeout() != null;
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(StorageDomain selectedItem) {
        driver.edit(getDetailModel());

        StorageDomain entity = (StorageDomain) getDetailModel().getEntity();
        if (entity != null) {
            totalSize.setValue(entity.getTotalDiskSize());
            availableSize.setValue(entity.getAvailableDiskSize());
            usedSize.setValue(entity.getUsedDiskSize());
            allocatedSize.setValue(entity.getCommittedDiskSize());
            overAllocationRatio.setValue(entity.getStorageDomainOverCommitPercent());
        }

        formBuilder.update(getDetailModel());
    }

}
