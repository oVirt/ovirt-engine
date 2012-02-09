package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.SetConfigurationValueParametes;
import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;

public class SetConfigurationValueCommand<T extends SetConfigurationValueParametes> extends ConfigCommandBase<T> {
    public SetConfigurationValueCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        // if version not sent, use default
        if (StringHelper.isNullOrEmpty(getParameters().getOption().getversion())) {
            getParameters().getOption().setversion(Config.DefaultConfigurationVersion);
        }
        VdcOption option =
                DbFacade.getInstance()
                        .getVdcOptionDAO()
                        .getByNameAndVersion(getParameters().getOption().getoption_name(),
                                getParameters().getOption().getversion());
        boolean retValue = (option != null);
        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.CONFIG_UNKNOWN_KEY);
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        VdcOption option =
                DbFacade.getInstance()
                        .getVdcOptionDAO()
                        .getByNameAndVersion(getParameters().getOption().getoption_name(),
                                getParameters().getOption().getversion());
        option.setoption_value(getParameters().getOption().getoption_value());
        if (EncryptionUtils.IsPassword(option.getoption_name())) {
            try {
                String keyFile = Config.<String> GetValue(ConfigValues.keystoreUrl);
                String passwd = Config.<String> GetValue(ConfigValues.keystorePass);
                String alias = Config.<String> GetValue(ConfigValues.CertAlias);
                option.setoption_value(EncryptionUtils.encrypt(option.getoption_value(), keyFile, passwd, alias));
            } catch (Exception e) {
                log.errorFormat("Failed to encrypt {0}\n{1}", option.getoption_name(), e.getMessage());
            }
        }
        DbFacade.getInstance().getVdcOptionDAO().update(option);
        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(SetConfigurationValueCommand.class);

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID, VdcObjectType.System);
    }

}
