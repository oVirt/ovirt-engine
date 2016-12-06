package org.ovirt.engine.core.bll.validator.storage;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.domain.BlockStorageDomainHelper;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@Singleton
public class BlockStorageDomainValidator {
    @Inject
    private LunDao lunDao;

    @Inject
    private BlockStorageDomainHelper helper;

    private BlockStorageDomainValidator() {
    }

    private static final String VAR_LUN_IDS = "lunIds";

    /**
     * This method receives a collection of luns and validates they are part of the storage domain.
     */
    public ValidationResult lunsInDomain(StorageDomain storageDomain, Collection<String> luns) {
        Collection<String> notInDomain = CollectionUtils.removeAll(luns,
                lunDao.getAllForVolumeGroup(storageDomain.getStorage()).stream().map(LUNs::getId).collect(toSet()));
        if (notInDomain.isEmpty()) {
            return ValidationResult.VALID;
        }

        return prepareValidationError(notInDomain,
                storageDomain,
                EngineMessage.ACTION_TYPE_FAILED_DEVICE_NOT_IN_STORAGE_DOMAIN);
    }

    /**
     * This method receives a collection of luns and validates that operations can be performed on it.
     */
    public ValidationResult lunsEligibleForOperation(StorageDomain storageDomain, Collection<String> luns) {
        List<String> metadataLuns = helper.findMetadataDevices(storageDomain, luns);

        if (!metadataLuns.isEmpty()) {
            return prepareValidationError(metadataLuns,
                    storageDomain,
                    EngineMessage.ACTION_TYPE_FAILED_OPERATION_ON_METADATA_DEVICES);
        }

        return ValidationResult.VALID;
    }

    private ValidationResult prepareValidationError(Collection<String> luns,
            StorageDomain storageDomain,
            EngineMessage message) {
        Collection<String> replacements = ReplacementUtils.replaceWith(VAR_LUN_IDS, luns, ",");
        replacements.add(String.format("$storageDomainName %s", storageDomain.getName()));
        return new ValidationResult(message, replacements);
    }
}
