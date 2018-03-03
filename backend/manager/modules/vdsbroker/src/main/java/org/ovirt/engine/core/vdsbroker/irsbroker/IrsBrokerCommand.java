package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.ovirt.engine.core.vdsbroker.TransportRunTimeException;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BrokerCommandBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Logged(errorLevel = LogLevel.ERROR)
public abstract class IrsBrokerCommand<P extends IrsBaseVDSCommandParameters> extends BrokerCommandBase<P> {

    @Inject
    private EventQueue eventQueue;

    @Inject
    private IrsProxyManager irsProxyManager;

    private static final Logger log = LoggerFactory.getLogger(IrsBrokerCommand.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private IVdsEventListener eventListener;

    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IRSErrorException(errorMessage);
    }
    protected IrsProxy getCurrentIrsProxy() {
        return irsProxyManager.getCurrentProxy(getParameters().getStoragePoolId());
    }

    protected IrsProxyManager getIrsProxyManager() {
        return irsProxyManager;
    }

    private int _failoverCounter;

    private void failover() {
        if ((getParameters().getIgnoreFailoverLimit() || _failoverCounter < Config
                .<Integer> getValue(ConfigValues.SpmCommandFailOverRetries) - 1)
                && getCurrentIrsProxy().getHasVdssForSpmSelection() && getCurrentIrsProxy().failover()) {
            _failoverCounter++;
            executeCommand();
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    public IrsBrokerCommand(P parameters) {
        super(parameters);

    }

    protected IIrsServer getIrsProxy() {
        return getCurrentIrsProxy().getIrsProxy();
    }

    @Override
    protected void executeVDSCommand() {
        AtomicBoolean isStartReconstruct = new AtomicBoolean(false);
        getCurrentIrsProxy().runInControlledConcurrency(() -> {
            try {
                if (getIrsProxy() != null) {
                    executeIrsBrokerCommand();
                } else {
                    if (getVDSReturnValue().getVdsError() == null) {
                        getVDSReturnValue().setExceptionString("Cannot allocate IRS server");
                        VDSError tempVar = new VDSError();
                        tempVar.setCode(EngineError.IRS_REPOSITORY_NOT_FOUND);
                        tempVar.setMessage("Cannot allocate IRS server");
                        getVDSReturnValue().setVdsError(tempVar);
                    }
                    getVDSReturnValue().setSucceeded(false);
                }
            } catch (UndeclaredThrowableException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(new VDSError(EngineError.VDS_NETWORK_ERROR, ex.getMessage()));
                if (ExceptionUtils.getRootCause(ex) != null) {
                    logException(ExceptionUtils.getRootCause(ex));
                } else {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                failover();
            } catch (TransportRunTimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex.isNetworkError()) {
                    log.error("IrsBroker::Failed::{} - network exception.", getCommandName());
                    getVDSReturnValue().setSucceeded(false);
                } else {
                    log.error("IrsBroker::Failed::{}", getCommandName());
                    log.debug(LoggedUtils.getObjectId(this), this, ex);
                    throw new IRSProtocolException(ex);
                }
            } catch (IRSNoMasterDomainException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                log.error("IrsBroker::Failed::{}: {}", getCommandName(), ex.getMessage());
                log.debug("Exception", ex);

                if ((ex.getVdsError() == null || ex.getVdsError().getCode() != EngineError.StoragePoolWrongMaster)
                        && getCurrentIrsProxy().getHasVdssForSpmSelection()) {
                    failover();
                } else {
                    isStartReconstruct.set(true);
                }
            } catch (IRSUnicodeArgumentException ex) {
                throw new IRSGenericException("UNICODE characters are not supported.", ex);
            } catch (IRSStoragePoolStatusException | IrsOperationFailedNoFailoverException ex) {
                throw ex;
            } catch (IRSNonOperationalException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                if (ex.getVdsError() != null && EngineError.SpmStatusError == ex.getVdsError().getCode()) {
                    getCurrentIrsProxy().setCurrentVdsId(Guid.Empty);
                }
                failover();
            } catch (IRSErrorException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                if (log.isDebugEnabled()) {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                failover();
            } catch (RuntimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex instanceof VDSExceptionBase) {
                    getVDSReturnValue().setVdsError(((VDSExceptionBase) ex).getVdsError());
                }
                if (ExceptionUtils.getRootCause(ex) != null &&
                        ExceptionUtils.getRootCause(ex) instanceof SocketException) {
                    logException(ExceptionUtils.getRootCause(ex));
                } else {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                // always failover because of changes in vdsm error, until we
                // realize what to do in each case:
                failover();
            } finally {
                getCurrentIrsProxy().getTriedVdssList().clear();
            }
        });
        if (isStartReconstruct.get()) {
            startReconstruct();
        }
    }

    private void startReconstruct() {
        StorageDomainStatic masterDomain = null;
        List<StorageDomainStatic> storageDomainStaticList =
                storageDomainStaticDao.getAllForStoragePool(getParameters().getStoragePoolId());
        for (StorageDomainStatic storageDomainStatic : storageDomainStaticList) {
            if (storageDomainStatic.getStorageDomainType() == StorageDomainType.Master) {
                masterDomain = storageDomainStatic;
                break;
            }
        }

        if (masterDomain != null) {
            final Guid masterDomainId = masterDomain.getId();
            eventQueue.submitEventAsync(new Event(getParameters().getStoragePoolId(),
                            masterDomainId, null, EventType.RECONSTRUCT, "IrsBrokerCommand.startReconstruct()"),
                    () -> eventListener.masterDomainNotOperational(
                                masterDomainId, getParameters().getStoragePoolId(), true,
                                getVDSReturnValue().getVdsError() != null
                                        && getVDSReturnValue().getVdsError().getCode()
                                        == EngineError.StoragePoolWrongMaster));
        } else {
            log.error(
                    "IrsBroker::IRSNoMasterDomainException:: Could not find master domain for pool '{}'",
                    getParameters().getStoragePoolId());
        }
    }

    protected void executeIrsBrokerCommand() {
    }

    /**
     * Write the exception to the system log.
     *
     * @param ex
     *            Exception to log.
     */
    private void logException(Throwable ex) {
        log.error("IrsBroker::Failed::{}: {}", getCommandName(), ex.getMessage());
        log.debug("Exception", ex);
    }

    public static Long assignLongValue(Map<String, Object> input, String name) {
        Long returnValue = null;
        if (input.containsKey(name)) {
            String stringValue = null;
            try {
                if (input.get(name) instanceof String) {
                    stringValue = (String) input.get(name);
                    returnValue = Long.parseLong(stringValue);
                }
            } catch (NumberFormatException nfe) {
                log.error("Failed to parse {} value {} to long", name, stringValue);
                returnValue = null;
            }
        }
        return returnValue;
    }

    @Override
    protected void logToAudit(){
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("CommandName", getCommandName());
        logable.addCustomValue("message", getReturnStatus().message);

        auditLogDirector.log(logable, AuditLogType.IRS_BROKER_COMMAND_FAILURE);
    }
}
