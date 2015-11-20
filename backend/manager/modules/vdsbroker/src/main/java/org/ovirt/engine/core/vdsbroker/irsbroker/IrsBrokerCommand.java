package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BrokerCommandBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Logged(errorLevel = LogLevel.ERROR)
public abstract class IrsBrokerCommand<P extends IrsBaseVDSCommandParameters> extends BrokerCommandBase<P> {

    @Inject
    private EventQueue eventQueue;
    private static Map<Guid, IrsProxyData> _irsProxyData = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(IrsBrokerCommand.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private IVdsEventListener eventListener;

    /**
     * process received domain monitoring information from a given vds if necessary (according to it's status
     * and if it's a virtualization node).
     */
    public static void updateVdsDomainsData(VDS vds, Guid storagePoolId,
            ArrayList<VDSDomainsData> vdsDomainData) {
        IrsProxyData proxy = _irsProxyData.get(storagePoolId);
        if (proxy != null) {
            proxy.updateVdsDomainsData(vds, vdsDomainData);
        }
    }

    public static List<Guid> fetchDomainsReportedAsProblematic(List<VDSDomainsData> vdsDomainsData, StoragePool storagePool) {
        IrsProxyData proxy = _irsProxyData.get(storagePool.getId());
        if (proxy != null) {
            return proxy.obtainDomainsReportedAsProblematic(vdsDomainsData);
        }
        return Collections.emptyList();
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IRSErrorException(errorMessage);
    }

    public static void init() {
        for (StoragePool sp : DbFacade.getInstance().getStoragePoolDao().getAll()) {
            if (!_irsProxyData.containsKey(sp.getId())) {
                _irsProxyData.put(sp.getId(), new IrsProxyData(sp.getId()));
            }
        }
    }

    public void removeIrsProxy() {
        _irsProxyData.get(getParameters().getStoragePoolId()).dispose();
        _irsProxyData.remove(getParameters().getStoragePoolId());
    }

    /**
     * Remove a VDS entry from the pool's IRS Proxy cache, clearing the problematic domains for this VDS and their
     * timers if they need to be cleaned. This is for a case when the VDS is switched to maintenance by the user, since
     * we need to clear it's cache data and timers, or else the cache will contain stale data (since the VDS is not
     * active anymore, it won't be connected to any of the domains).
     *
     * @param storagePoolId
     *            The ID of the storage pool to clean the IRS Proxy's cache for.
     * @param vdsId
     *            The ID of the VDS to remove from the cache.
     * @param vdsName
     *            The name of the VDS (for logging).
     */
    public static void clearVdsFromCache(Guid storagePoolId, Guid vdsId, String vdsName) {
        IrsProxyData irsProxyData = getIrsProxyData(storagePoolId);
        if (irsProxyData != null) {
            irsProxyData.clearVdsFromCache(vdsId, vdsName);
        }
    }

    /**
     * Return the IRS Proxy object for the given pool id. If there's no proxy data available, since there's no SPM
     * for the pool, then returns <code>null</code>.
     * @param storagePoolId The ID of the storage pool to get the IRS proxy for.
     * @return The IRS Proxy object, on <code>null</code> if no proxy data is available.
     */
    protected static IrsProxyData getIrsProxyData(Guid storagePoolId) {
        return _irsProxyData.get(storagePoolId);
    }

    protected IrsProxyData getCurrentIrsProxyData() {
        if (!_irsProxyData.containsKey(getParameters().getStoragePoolId())) {
            _irsProxyData.put(getParameters().getStoragePoolId(), new IrsProxyData(getParameters().getStoragePoolId()));
        }
        return _irsProxyData.get(getParameters().getStoragePoolId());
    }

    private int _failoverCounter;

    private void failover() {
        if ((getParameters().getIgnoreFailoverLimit() || _failoverCounter < Config
                .<Integer> getValue(ConfigValues.SpmCommandFailOverRetries) - 1)
                && getCurrentIrsProxyData().getHasVdssForSpmSelection() && getCurrentIrsProxyData().failover()) {
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
        return getCurrentIrsProxyData().getIrsProxy();
    }

    @Override
    protected void executeVDSCommand() {
        AtomicBoolean isStartReconstruct = new AtomicBoolean(false);
        getCurrentIrsProxyData().runInControlledConcurrency(() -> {
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
            } catch (XmlRpcRunTimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex.isNetworkError()) {
                    log.error("IrsBroker::Failed::{} - network exception.", getCommandName());
                    getVDSReturnValue().setSucceeded(false);
                } else {
                    log.error("IrsBroker::Failed::{}", getCommandName());
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                    throw new IRSProtocolException(ex);
                }
            } catch (IRSNoMasterDomainException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                log.error("IrsBroker::Failed::{}: {}", getCommandName(), ex.getMessage());
                log.debug("Exception", ex);

                if ((ex.getVdsError() == null || ex.getVdsError().getCode() != EngineError.StoragePoolWrongMaster)
                        && getCurrentIrsProxyData().getHasVdssForSpmSelection()) {
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
                    getCurrentIrsProxyData().setCurrentVdsId(Guid.Empty);
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
                getCurrentIrsProxyData().getTriedVdssList().clear();
            }
        });
        if (isStartReconstruct.get()) {
            startReconstruct();
        }
    }

    private void startReconstruct() {
        StorageDomainStatic masterDomain = null;
        List<StorageDomainStatic> storageDomainStaticList = DbFacade.getInstance()
                .getStorageDomainStaticDao().getAllForStoragePool(getParameters().getStoragePoolId());
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
        AuditLogableBase auditLogableBase = new AuditLogableBase();
        auditLogableBase.addCustomValue("message", getReturnStatus().message);

        auditLogDirector.log(auditLogableBase, AuditLogType.IRS_BROKER_COMMAND_FAILURE);
    }
}
