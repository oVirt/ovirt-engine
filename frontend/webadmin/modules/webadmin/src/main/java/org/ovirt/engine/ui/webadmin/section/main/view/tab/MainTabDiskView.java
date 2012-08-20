package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.CommonModelManager;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewRadioGroup;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class MainTabDiskView extends AbstractMainTabWithDetailsTableView<Disk, DiskListModel> implements MainTabDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    SimplePanel tablePanel;

    private ApplicationConstants constants;
    private CommonModel commonModel;
    private DisksViewRadioGroup disksViewRadioGroup;

    @Inject
    public MainTabDiskView(MainModelProvider<Disk, DiskListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);

        this.constants = constants;
        this.commonModel = CommonModelManager.instance();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTableButtons();
        initTableOverhead();
        initWidget(getTable());

        handleRadioButtonClick(null);

        modelProvider.getModel().getDiskViewType().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel diskViewType = (EntityModel) sender;
                if (!disksViewRadioGroup.getAllButton().getValue()) {
                    disksViewRadioGroup.getAllButton().setValue(diskViewType.getEntity() == null);
                    handleRadioButtonClick(null);
                }
            }
        });
    }

    final ClickHandler clickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if (((RadioButton) event.getSource()).getValue()) {
                handleRadioButtonClick(event);
            }
        }
    };

    void handleRadioButtonClick(ClickEvent event) {
        boolean all = disksViewRadioGroup.getAllButton().getValue();
        boolean images = disksViewRadioGroup.getImagesButton().getValue();
        boolean luns = disksViewRadioGroup.getLunsButton().getValue();

        getMainModel().getDiskViewType().setEntity(disksViewRadioGroup.getDiskStorageType());
        searchByDiskViewType(disksViewRadioGroup.getDiskStorageType());

        getTable().ensureColumnPresent(
                DisksViewColumns.aliasColumn, constants.aliasDisk(), all || images || luns);

        getTable().ensureColumnPresent(
                DisksViewColumns.idColumn, constants.idDisk(), all || images || luns);

        getTable().ensureColumnPresent(
                DisksViewColumns.bootableDiskColumn, "", all || images || luns, "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().ensureColumnPresent(
                DisksViewColumns.shareableDiskColumn, "", all || images || luns, "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunDiskColumn, "", all, "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersIconColumn, "", all || images || luns, "40px"); //$NON-NLS-1$ //$NON-NLS-2$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersColumn, constants.attachedToDisk(), all || images || luns, "120px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.storageDomainsColumn, constants.storageDomainsDisk(), images, "120px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.sizeColumn, constants.provisionedSizeDisk(), all || images || luns, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.actualSizeColumn, constants.sizeDisk(), images, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.allocationColumn, constants.allocationDisk(), images, "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.dateCreatedColumn, constants.creationDateDisk(), images, "150px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.statusColumn, constants.statusDisk(), images, "80px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunIdColumn, constants.lunIdSanStorage(), luns);

        getTable().ensureColumnPresent(
                DisksViewColumns.lunSerialColumn, constants.serialSanStorage(), luns);

        getTable().ensureColumnPresent(
                DisksViewColumns.lunVendorIdColumn, constants.vendorIdSanStorage(), luns);

        getTable().ensureColumnPresent(
                DisksViewColumns.lunProductIdColumn, constants.productIdSanStorage(), luns);

        getTable().ensureColumnPresent(
                DisksViewColumns.descriptionColumn, constants.descriptionDisk(), all || images || luns);

    }

    void initTableOverhead() {
        disksViewRadioGroup = new DisksViewRadioGroup();
        disksViewRadioGroup.setClickHandler(clickHandler);
        getTable().setTableOverhead(disksViewRadioGroup);
        getTable().setTableTopMargin(20);
    }

    void initTableButtons() {
        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.addDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.moveDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getMoveCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.copyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCopyCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.assignQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getChangeQuotaCommand();
            }
        });
    }

    void searchByDiskViewType(Object diskViewType) {
        String diskTypePostfix = diskViewType != null ?
                ((DiskStorageType) diskViewType).name().toLowerCase() + constants.space() : null;
        String diskType = diskTypePostfix != null ?
                constants.diskTypeSearchPrefix() + diskTypePostfix : constants.empty();

        String searchConjunction = !diskType.equals(constants.empty()) ?
                constants.searchConjunctionAnd() : constants.empty();

        String searchStringPrefixRaw = commonModel.getSearchStringPrefix().replaceAll(
                constants.diskTypeSearchPrefix() + constants.searchRegexAll(), constants.empty()).replaceAll(
                constants.searchConjunctionAnd() + constants.searchRegexAll(), constants.empty()).trim();

        String searchStringPrefix;
        if (diskType.equals(constants.empty())) {
            searchStringPrefix = searchStringPrefixRaw;
        }
        else if (searchStringPrefixRaw.equals(constants.disksSearchPrefix())) {
            searchStringPrefix = constants.disksSearchPrefix() + constants.space() + diskType;
        }
        else {
            searchStringPrefix = searchStringPrefixRaw.isEmpty() ?
                    constants.disksSearchPrefix() + constants.space() + diskType :
                    searchStringPrefixRaw + searchConjunction + diskType;
        }

        String searchString;
        if (!commonModel.getSearchString().contains(":")) { //$NON-NLS-1$
            searchString = searchStringPrefix.isEmpty() ?
                    constants.disksSearchPrefix() + constants.space() + commonModel.getSearchString() :
                    commonModel.getSearchString().replace(constants.disksSearchPrefix(), constants.empty());
        }
        else {
            searchString = searchStringPrefix.isEmpty() ? constants.disksSearchPrefix() : constants.empty();
        }

        commonModel.setSearchStringPrefix(searchStringPrefix);
        commonModel.setSearchString(searchString);

        getTable().getSelectionModel().clear();
        getMainModel().setItems(null);
        getMainModel().setSearchString(commonModel.getEffectiveSearchString());
        getMainModel().Search();
    }
}
