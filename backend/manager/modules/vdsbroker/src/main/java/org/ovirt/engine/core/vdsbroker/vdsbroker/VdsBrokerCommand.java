package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.net.ConnectException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.ApplicationException;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;

public abstract class VdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends BrokerCommandBase<P> {
    private final IVdsServer mVdsBroker;
    private VDS mVds;

    /**
     * Construct the command using the parameters and the {@link VDS} which is loaded from the DB.
     *
     * @param parameters
     *            The parameters of the command.
     */
    public VdsBrokerCommand(P parameters) {
        this(parameters, DbFacade.getInstance().getVdsDAO().get(parameters.getVdsId()));
    }

    /**
     * Construct the command using the parameters and the {@link VDS} which is passed.
     *
     * @param parameters
     *            The parameters of the command.
     * @param vds
     *            The host to use in the command.
     */
    protected VdsBrokerCommand(P parameters, VDS vds) {
        super(parameters);
        VdsManager vdsmanager = ResourceManager.getInstance().GetVdsManager(parameters.getVdsId());
        if (vdsmanager == null) {
            throw new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_VDS_NOT_FOUND,
                    String.format("Vds with id: %1$s was not found", parameters.getVdsId()));
        }
        mVdsBroker = vdsmanager.getVdsProxy();
        mVds = vds;
    }

    protected IVdsServer getBroker() {
        return mVdsBroker;
    }

    @Override
    protected String getAdditionalInformation() {
        if (getVds() != null) {
            return String.format("Vds: %1$s", getVds().getvds_name());
        } else {
            return super.getAdditionalInformation();
        }
    }

    protected VDS getVds() {
        return mVds;
    }

    protected void setVds(VDS value) {
        mVds = value;
    }

    @Override
    protected void ExecuteVDSCommand() {
        try {
            ExecuteVdsBrokerCommand();
        } catch (VDSExceptionBase ex) {
            PrintReturnValue();
            throw ex;
        } catch (ApplicationException ex) {
            log.errorFormat("Failed in {0} method", getCommandName());
            log.error("Exception", ex);
            PrintReturnValue();
            throw new VDSProtocolException(ex);
        } catch (XmlRpcRunTimeException ex) {
            final String msgFormat = "XML RPC error in command {0} ( {1} ), the error was: {2}, {3} ";
            if ((ExceptionUtils.getRootCause(ex) instanceof ConnectException)) {
                log.debugFormat(msgFormat,
                        getCommandName(),
                        getAdditionalInformation(),
                        ex.getMessage(),
                        ExceptionUtils.getRootCauseMessage(ex));
            } else {
                log.errorFormat(msgFormat,
                        getCommandName(),
                        getAdditionalInformation(),
                        ex.getMessage(),
                        ExceptionUtils.getRootCauseMessage(ex));
            }
            PrintReturnValue();
            throw new VDSNetworkException(ex);
        }
        // catch (WebException ex)
        // {
        // // log this exception in debug becaue it is being logged again later.
        // log.infoFormat("Failed in {0} method", getCommandName());
        // log.info("Exception", ex);
        // throw new VDSNetworkException(ex);
        // }

        // catch (NullReferenceException ex)
        // {
        // PrintReturnValue();
        // //This is a workaround a bug in the xml-rpc package
        // throw new VDSNetworkException(ex);
        // }

        // TODO: look for invalid certificates error handling
        catch (RuntimeException e) {
            PrintReturnValue();
            if (getVds() == null) {
                log.errorFormat("Failed in {0} method, for vds id: {1}",
                        getCommandName(), getParameters().getVdsId());
            } else {
                log.errorFormat("Failed in {0} method, for vds: {1}; host: {2}",
                        getCommandName(), getVds().getvds_name(), getVds().gethost_name());
            }
            throw e;
        }

    }

    protected abstract void ExecuteVdsBrokerCommand();

    private static LogCompat log = LogFactoryCompat.getLog(VdsBrokerCommand.class);
}
