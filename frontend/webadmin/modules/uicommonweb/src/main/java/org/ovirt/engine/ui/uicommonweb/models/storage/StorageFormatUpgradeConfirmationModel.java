package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class StorageFormatUpgradeConfirmationModel extends ConfirmationModel {

    // Returns true if the confirmation window should be displayed; Otherwise, returns false.
    // Displays multiple storage domains message variant when specified more than one domain.
    public boolean initialize(List<StorageDomain> sds,
            StoragePool dc,
            String okCommandName,
            String cancelCommandName,
            ICommandTarget target) {
        List<StorageDomain> sdsToUpgrade = sds.stream().filter(getComparePredicate(dc)).collect(Collectors.toList());

        if (sdsToUpgrade.isEmpty()) {
            return false;
        }

        UICommand okCommand = UICommand.createDefaultOkUiCommand(okCommandName, target);
        UICommand cancelCommand = UICommand.createCancelUiCommand(cancelCommandName, target);
        getCommands().add(okCommand);
        getCommands().add(cancelCommand);
        setTitle(ConstantsManager.getInstance().getConstants().updatingStorageDomainTitle());
        setMessage(getFormatWarningMessage(dc, sdsToUpgrade));
        return true;
    }

    private String getFormatWarningMessage(StoragePool dc, List<StorageDomain> sdsToUpgrade) {
        if (sdsToUpgrade.size() > 1) {
            return ConstantsManager.getInstance()
                    .getMessages()
                    .compareMultipleStorageFormatsToDataCenterWarningMessage(
                            dc.getName(),
                            sdsToUpgrade.stream()
                                    .map(StorageDomain::getName)
                                    .collect(Collectors.joining(", "))); //$NON-NLS-1$
        }
        return ConstantsManager.getInstance()
            .getMessages()
            .compareStorageFormatToDataCenterWarningMessage(
                    dc.getName(),
                    sdsToUpgrade.get(0).getName(),
                    sdsToUpgrade.get(0).getStorageFormat().getValue(),
                    dc.getStoragePoolFormatType().getValue());
    }

    private Predicate<StorageDomain> getComparePredicate(StoragePool dc) {
        if (dc.getStoragePoolFormatType() == null) {
            // 'None' DC
            return storageDomain -> false;
        }
        // Checks if the DC level is greater than the storage domain level.
        // And is not an export domain.
        return sd -> (sd.getStorageFormat().compareTo(dc.getStoragePoolFormatType()) < 0)
                && !sd.getStorageDomainType().isIsoOrImportExportDomain();
    }
}
