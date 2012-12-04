package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.IOException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.ApplicationException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;

public abstract class VdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends BrokerCommandBase<P> {
    private final IVdsServer mVdsBroker;
    private VDS mVds;
    private static final String msgFormat = "XML RPC error in command {0} ( {1} ), the error was: {2}, {3} ";

    /**
     * Construct the command using the parameters and the {@link VDS} which is loaded from the DB.
     *
     * @param parameters
     *            The parameters of the command.
     */
    public VdsBrokerCommand(P parameters) {
        super(parameters);
        mVdsBroker = initializeVdsBroker(parameters.getVdsId());
        mVds = getDbFacade().getVdsDao().get(parameters.getVdsId());
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
        mVdsBroker = initializeVdsBroker(parameters.getVdsId());
        mVds = vds;
    }

    protected IVdsServer initializeVdsBroker(Guid vdsId) {
        VdsManager vdsmanager = ResourceManager.getInstance().GetVdsManager(vdsId);
        if (vdsmanager == null) {
            throw new VdcBLLException(VdcBllErrors.RESOURCE_MANAGER_VDS_NOT_FOUND,
                    String.format("Vds with id: %1$s was not found", vdsId));
        }
        return vdsmanager.getVdsProxy();
    }

    protected IVdsServer getBroker() {
        return mVdsBroker;
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new VDSErrorException(errorMessage);
    }

    @Override
    protected String getAdditionalInformation() {
        if (getVds() != null) {
            return String.format("HostName = %1$s", getVds().getvds_name());
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

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
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
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            VDSNetworkException networkException = new VDSNetworkException(rootCause);
            if ((ex.isNetworkError() || rootCause instanceof IOException)) {
                log.debugFormat(msgFormat,
                        getCommandName(),
                        getAdditionalInformation(),
                        ex.getMessage(),
                        rootCause.getMessage());
            } else {
                log.errorFormat(msgFormat,
                        getCommandName(),
                        getAdditionalInformation(),
                        ex.getMessage(),
                        rootCause.getMessage());
                networkException.setVdsError(new VDSError(VdcBllErrors.PROTOCOL_ERROR, rootCause.toString()));
            }
            PrintReturnValue();
            throw networkException;
        }

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

    private static Log log = LogFactory.getLog(VdsBrokerCommand.class);
}
