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

public abstract class VdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends BrokerCommandBase<P> {
    private final IVdsServer mVdsBroker;
    private VdsStatic vdsStatic;
    private VDS vds;
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

    private VdsStatic getAndSetVdsStatic() {
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
            PrintReturnValue();
            throw ex;
        } catch (XmlRpcRunTimeException ex) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            VDSNetworkException networkException = new VDSNetworkException(rootCause);
            networkException.setVdsError(new VDSError(VdcBllErrors.VDS_NETWORK_ERROR, rootCause.toString()));
            PrintReturnValue();
            throw networkException;
        }

        // TODO: look for invalid certificates error handling
        catch (RuntimeException e) {
            PrintReturnValue();
            if (getAndSetVdsStatic() == null) {
                log.errorFormat("Failed in {0} method, for vds id: {1}",
                        getCommandName(), getParameters().getVdsId());
            } else {
                log.errorFormat("Failed in {0} method, for vds: {1}; host: {2}",
                        getCommandName(), getAndSetVdsStatic().getName(), getAndSetVdsStatic().getHostName());
            }
            throw e;
        }

    }

    protected abstract void executeVdsBrokerCommand();
}
