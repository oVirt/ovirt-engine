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
        initTableColumns();
        initTableButtons();
        initTableOverhead();
        initWidget(getTable());

        modelProvider.getModel().getDiskViewType().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                EntityModel diskViewType = (EntityModel) sender;
                disksViewRadioGroup.setDiskStorageType((DiskStorageType) diskViewType.getEntity());
                onDiskViewTypeChanged();
            }
        });
    }

    final ClickHandler clickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if (((RadioButton) event.getSource()).getValue()) {
                getMainModel().getDiskViewType().setEntity(disksViewRadioGroup.getDiskStorageType());
            }
        }
    };

    void onDiskViewTypeChanged() {
        boolean all = disksViewRadioGroup.getAllButton().getValue();
        boolean images = disksViewRadioGroup.getImagesButton().getValue();
        boolean luns = disksViewRadioGroup.getLunsButton().getValue();

        searchByDiskViewType(disksViewRadioGroup.getDiskStorageType());

        getTable().ensureColumnPresent(
                DisksViewColumns.aliasColumn, constants.aliasDisk(), all || images || luns,
                "150px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.idColumn, constants.idDisk(), all || images || luns,
                "150px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.bootableDiskColumn,
                DisksViewColumns.bootableDiskColumn.getHeaderHtml(), all || images || luns, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.shareableDiskColumn,
                DisksViewColumns.shareableDiskColumn.getHeaderHtml(), all || images || luns, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunDiskColumn,
                DisksViewColumns.lunDiskColumn.getHeaderHtml(), all, "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersIconColumn, "", all || images || luns, //$NON-NLS-1$
                "30px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.diskContainersColumn, constants.attachedToDisk(), all || images || luns,
                "125px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.storageDomainsColumn, constants.storageDomainsDisk(), images,
                "125px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.sizeColumn, constants.provisionedSizeDisk(), all || images || luns,
                "110px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.allocationColumn, constants.allocationDisk(), images,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.dateCreatedColumn, constants.creationDateDisk(), images,
                "150px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.statusColumn, constants.statusDisk(), images,
                "80px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunIdColumn, constants.lunIdSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunSerialColumn, constants.serialSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunVendorIdColumn, constants.vendorIdSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.lunProductIdColumn, constants.productIdSanStorage(), luns,
                "100px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.descriptionColumn, constants.descriptionDisk(), all || images || luns,
                "100px"); //$NON-NLS-1$
    }

    void initTableColumns() {
        getTable().enableColumnResizing();
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
        final String disksSearchPrefix = "Disks:"; //$NON-NLS-1$
        final String diskTypeSearchPrefix = "disk_type = "; //$NON-NLS-1$
        final String searchConjunctionAnd = " all "; //$NON-NLS-1$
        final String searchRegexAll = "(.)*"; //$NON-NLS-1$
        final String space = " "; //$NON-NLS-1$
        final String empty = ""; //$NON-NLS-1$

        String diskTypePostfix = diskViewType != null ?
                ((DiskStorageType) diskViewType).name().toLowerCase() + space : null;
        String diskType = diskTypePostfix != null ?
                diskTypeSearchPrefix + diskTypePostfix : empty;

        String searchConjunction = !diskType.equals(empty) ?
                searchConjunctionAnd : empty;

        String searchStringPrefixRaw = commonModel.getSearchStringPrefix().replaceAll(
                diskTypeSearchPrefix + searchRegexAll, empty).replaceAll(
                searchConjunctionAnd + searchRegexAll, empty).trim();

        String searchStringPrefix;
        if (diskType.equals(empty)) {
            searchStringPrefix = searchStringPrefixRaw;
        }
        else if (searchStringPrefixRaw.equals(disksSearchPrefix)) {
            searchStringPrefix = disksSearchPrefix + space + diskType;
        }
        else {
            searchStringPrefix = searchStringPrefixRaw.isEmpty() ?
                    disksSearchPrefix + space + diskType :
                    searchStringPrefixRaw + searchConjunction + diskType;
        }

        String searchString;
        if (!commonModel.getSearchString().contains(":")) { //$NON-NLS-1$
            searchString = searchStringPrefix.isEmpty() ?
                    disksSearchPrefix + space + commonModel.getSearchString() :
                    commonModel.getSearchString().replace(disksSearchPrefix, empty);
        }
        else {
            searchString = searchStringPrefix.isEmpty() ? disksSearchPrefix : empty;
        }

        commonModel.setSearchStringPrefix(searchStringPrefix);
        commonModel.setSearchString(searchString);

        getTable().getSelectionModel().clear();
        getMainModel().setItems(null);
        getMainModel().setSearchString(commonModel.getEffectiveSearchString());
        getMainModel().Search();
    }
}
