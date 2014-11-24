package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public abstract class VdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends BrokerCommandBase<P> {
    private final IVdsServer mVdsBroker;
    private VdsStatic vdsStatic;
    private VDS vds;
    @Inject
    Event<VDSNetworkException> networkError;
    /**
     * Construct the command using the parameters and the {@link VDS} which is loaded from the DB.
     *
     * @param parameters
     *            The parameters of the command.
     */
    public VdsBrokerCommand(P parameters) {
        super(parameters);
        mVdsBroker = initializeVdsBroker(parameters.getVdsId());
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
        this.mVdsBroker = initializeVdsBroker(parameters.getVdsId());
        this.vds = vds;
        this.vdsStatic = vds.getStaticData();
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
        if (getAndSetVdsStatic() != null) {
            return String.format("HostName = %1$s", getAndSetVdsStatic().getName());
        } else {
            return super.getAdditionalInformation();
        }
    }

    protected VdsStatic getAndSetVdsStatic() {
        if (vdsStatic == null) {
            vdsStatic = getDbFacade().getVdsStaticDao().get(getParameters().getVdsId());
        }
        return vdsStatic;
    }

    protected VDS getVds() {
        return vds;
    }

    protected void setVdsAndVdsStatic(VDS vds) {
        this.vds = vds;
        this.vdsStatic = vds.getStaticData();
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    @Override
    protected void executeVDSCommand() {
        try {
            executeVdsBrokerCommand();
        } catch (VDSExceptionBase ex) {
            printReturnValue();
            throw ex;
        } catch (XmlRpcRunTimeException ex) {
            VDSNetworkException networkException = createNetworkException(ex);
            printReturnValue();
            networkError.fire(networkException);
            throw networkException;
        }

        // TODO: look for invalid certificates error handling
        catch (RuntimeException e) {
            printReturnValue();
            if (getAndSetVdsStatic() == null) {
                log.error("Failed in '{}' method, for vds id: '{}': {}",
                        getCommandName(), getParameters().getVdsId(), e.getMessage());
            } else {
                log.error("Failed in '{}' method, for vds: '{}'; host: '{}': {}",
                        getCommandName(), getAndSetVdsStatic().getName(), getAndSetVdsStatic().getHostName(),
                        e.getMessage());
            }
            throw e;
        }

    }

    protected VDSNetworkException createNetworkException(Exception ex) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        VDSNetworkException networkException;
        String message;
        if (rootCause != null) {
            networkException = new VDSNetworkException(rootCause);
            message = rootCause.toString();
        } else {
            networkException = new VDSNetworkException(ex);
            message = ex.getMessage();
        }
        VDSError value = new VDSError(VdcBllErrors.VDS_NETWORK_ERROR, message);
        value.setVdsId(getVds().getId());
        networkException.setVdsError(value);
        return networkException;
    }

    protected abstract void executeVdsBrokerCommand();
}
