package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.CommonModelManager;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewColumns;
import org.ovirt.engine.ui.common.widget.uicommon.disks.DisksViewRadioGroup;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.regexp.shared.RegExp;
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

    private final ApplicationConstants constants;
    private final CommonModel commonModel;
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
                "120px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.idColumn, constants.idDisk(), all || images || luns,
                "120px"); //$NON-NLS-1$

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
                "180px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.sizeColumn, constants.provisionedSizeDisk(), all || images || luns,
                "110px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.allocationColumn, constants.allocationDisk(), images,
                "130px"); //$NON-NLS-1$

        getTable().ensureColumnPresent(
                DisksViewColumns.dateCreatedColumn, constants.creationDateDisk(), images,
                "130px"); //$NON-NLS-1$

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
                "90px"); //$NON-NLS-1$
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

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.getDiskAlignment(),
                CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getScanAlignmentCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<Disk>(constants.exportDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getExportCommand();
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
        final String searchConjunctionAnd = "and "; //$NON-NLS-1$
        final String searchRegexDisksSearchPrefix = "^\\s*(disk(s)?\\s*(:)+)+\\s*"; //$NON-NLS-1$
        final String searchRegexDiskTypeClause = "\\s*((and|or)\\s+)?disk_type\\s*=\\s*\\S+"; //$NON-NLS-1$
        final String searchRegexStartConjunction = "^\\s*(and|or)\\s*"; //$NON-NLS-1$
        final String searchRegexFlags = "ig"; //$NON-NLS-1$

        final String space = " "; //$NON-NLS-1$
        final String empty = ""; //$NON-NLS-1$
        final String colon = ":"; //$NON-NLS-1$

        RegExp searchPatternDisksSearchPrefix = RegExp.compile(searchRegexDisksSearchPrefix, searchRegexFlags);
        RegExp searchPatternDiskTypeClause = RegExp.compile(searchRegexDiskTypeClause, searchRegexFlags);
        RegExp searchPatternStartConjunction = RegExp.compile(searchRegexStartConjunction, searchRegexFlags);

        String diskTypePostfix = diskViewType != null ?
                ((DiskStorageType) diskViewType).name().toLowerCase() + space : null;
        String diskTypeClause = diskTypePostfix != null ?
                diskTypeSearchPrefix + diskTypePostfix : empty;

        String inputSearchString = commonModel.getSearchString().trim();
        String inputSearchStringPrefix = commonModel.getSearchStringPrefix().trim();

        if (!inputSearchString.isEmpty() && inputSearchStringPrefix.isEmpty()) {
            int indexOfColon = inputSearchString.indexOf(colon);
            inputSearchStringPrefix = inputSearchString.substring(0, indexOfColon + 1).trim();
            inputSearchString = inputSearchString.substring(indexOfColon + 1).trim();
        }
        if (inputSearchStringPrefix.isEmpty()) {
            inputSearchStringPrefix = disksSearchPrefix;
            inputSearchString = empty;
        }

        String searchStringPrefixRaw = searchPatternDiskTypeClause
                .replace(inputSearchStringPrefix, empty).trim();

        String searchStringPrefix;
        if (diskTypeClause.equals(empty)) {
            searchStringPrefix = searchStringPrefixRaw + space;
        }
        else {
            searchStringPrefix = searchStringPrefixRaw + space
                    + (searchPatternDisksSearchPrefix.test(searchStringPrefixRaw) ? empty : searchConjunctionAnd)
                    + diskTypeClause;
        }

        inputSearchString = searchPatternDiskTypeClause
                .replace(inputSearchString, empty);
        inputSearchString = searchPatternStartConjunction
                .replace(inputSearchString, empty);

        String searchString;
        if (searchPatternDisksSearchPrefix.test(searchStringPrefix) || inputSearchString.isEmpty()) {
            searchString = inputSearchString;
        }
        else {
            searchString = searchConjunctionAnd + inputSearchString;
        }
        commonModel.setSearchStringPrefix(searchStringPrefix);
        commonModel.setSearchString(searchString);

        getTable().getSelectionModel().clear();
        getMainModel().setItems(null);
        getMainModel().setSearchString(commonModel.getEffectiveSearchString());
        getMainModel().search();
    }
}
