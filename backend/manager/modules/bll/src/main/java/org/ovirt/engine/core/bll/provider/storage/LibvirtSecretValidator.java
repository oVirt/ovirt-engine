package org.ovirt.engine.core.bll.provider.storage;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.LibvirtSecretDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class LibvirtSecretValidator {

    private final LibvirtSecret libvirtSecret;
    private ProviderDao providerDao;
    private LibvirtSecretDao libvirtSecretDao;

    public LibvirtSecretValidator(LibvirtSecret libvirtSecret) {
        this.libvirtSecret = libvirtSecret;
        this.providerDao = getDbFacade().getProviderDao();
        this.libvirtSecretDao = getDbFacade().getLibvirtSecretDao();
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public ValidationResult uuidNotEmpty() {
        return ValidationResult.failWith(EngineMessage.LIBVIRT_SECRET_UUID_CANNOT_BE_EMPTY)
                .when(StringUtils.isEmpty(libvirtSecret.getId().toString()));
    }

    public ValidationResult uuidNotExist() {
        return ValidationResult.failWith(EngineMessage.LIBVIRT_SECRET_UUID_ALREADY_EXISTS)
                .unless(libvirtSecretDao.get(libvirtSecret.getId()) == null);
    }

    public ValidationResult uuidExist() {
        return ValidationResult.failWith(EngineMessage.LIBVIRT_SECRET_UUID_NOT_EXISTS)
                .unless(libvirtSecretDao.get(libvirtSecret.getId()) != null);
    }

    public ValidationResult valueNotEmpty() {
        return ValidationResult.failWith(EngineMessage.LIBVIRT_SECRET_VALUE_CANNOT_BE_EMPTY)
                .when(StringUtils.isEmpty(libvirtSecret.getValue()));
    }

    public ValidationResult providerExist() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST)
                .when(providerDao.get(libvirtSecret.getProviderId()) == null);
    }
}
