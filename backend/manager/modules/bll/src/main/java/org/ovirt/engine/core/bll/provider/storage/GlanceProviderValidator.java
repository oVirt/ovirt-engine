package org.ovirt.engine.core.bll.provider.storage;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

public class GlanceProviderValidator extends ProviderValidator {

    StorageDomain glanceStorageDomain;

    public GlanceProviderValidator(Provider<?> provider) {
        super(provider);
    }

    @Override
    public ValidationResult validateRemoveProvider() {
        if (getAsyncTaskDao().getAsyncTaskIdsByEntity(getStorageDomain().getId()).size() > 0) {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_TASKS);
        }
        return ValidationResult.VALID;
    }

    private StorageDomain getStorageDomain() {
        if (glanceStorageDomain == null) {
            List<StorageDomain> providerStorageList = getStorageDomainDao().getAllByConnectionId(provider.getId());
            if (!providerStorageList.isEmpty()) {
                glanceStorageDomain = providerStorageList.get(0);
            }
        }
        return glanceStorageDomain;
    }

    protected StorageDomainDao getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    protected AsyncTaskDao getAsyncTaskDao() {
        return DbFacade.getInstance().getAsyncTaskDao();
    }
}
