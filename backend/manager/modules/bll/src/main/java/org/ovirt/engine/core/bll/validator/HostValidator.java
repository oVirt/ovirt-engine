package org.ovirt.engine.core.bll.validator;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

public class HostValidator {

    private final VdsDAO hostDao;
    private final StoragePoolDAO storagePoolDao;
    private final VdsStaticDAO hostStaticDao;
    private final VDS host;

    public HostValidator(DbFacade dbFacade, VDS host) {
        this.hostDao = dbFacade.getVdsDao();
        this.storagePoolDao = dbFacade.getStoragePoolDao();
        this.hostStaticDao = dbFacade.getVdsStaticDao();
        this.host = host;
    }

    public ValidationResult nameNotEmpty() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY)
                .when(StringUtils.isEmpty(host.getName()));
    }

    public ValidationResult nameLengthIsLegal() {
        int maxHostNameLength = Config.<Integer> getValue(ConfigValues.MaxVdsNameLength);
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG)
                .when(host.getName().length() > maxHostNameLength);
    }

    public ValidationResult hostNameIsValid() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_VDS_HOSTNAME)
                .unless(ValidationUtils.validHostname(host.getHostName()));
    }

    public ValidationResult nameNotUsed() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED)
                .when(hostDao.getByName(host.getName()) != null);
    }

    public ValidationResult hostNameNotUsed() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST)
                .unless(hostDao.getAllForHostname(host.getHostName()).isEmpty());
    }

    public ValidationResult portIsValid() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_PORT)
                .unless(ValidationUtils.validatePort(host.getSshPort()));
    }

    public ValidationResult sshUserNameNotEmpty() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_USERNAME)
                .when(StringUtils.isBlank(host.getSshUsername()));
    }

    public ValidationResult validateSingleHostAttachedToLocalStorage() {
        StoragePool storagePool = storagePoolDao.getForVdsGroup(host.getVdsGroupId());
        if (storagePool == null || !storagePool.isLocal()) {
            return ValidationResult.VALID;
        }

        return ValidationResult.failWith(VdcBllMessages.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE)
                .unless(hostStaticDao.getAllForVdsGroup(host.getVdsGroupId()).isEmpty());
    }

    public ValidationResult securityKeysExists() {
        return ValidationResult.failWith(VdcBllMessages.VDS_TRY_CREATE_SECURE_CERTIFICATE_NOT_FOUND)
                .when(Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication) && !haveSecurityKey());
    }

    protected boolean haveSecurityKey() {
        return EngineEncryptionUtils.haveKey();
    }

    /**
     *  We block vds installations if it's not a RHEV-H and password is empty.
     *  Note that this may override local host SSH policy. See BZ#688718.
     */
    public ValidationResult passwordNotEmpty(boolean addPending, AuthenticationMethod authMethod, String password) {
        return ValidationResult.failWith(VdcBllMessages.VDS_CANNOT_INSTALL_EMPTY_PASSWORD)
                .when(!addPending && authMethod == AuthenticationMethod.Password && StringUtils.isEmpty(password));
    }
}
