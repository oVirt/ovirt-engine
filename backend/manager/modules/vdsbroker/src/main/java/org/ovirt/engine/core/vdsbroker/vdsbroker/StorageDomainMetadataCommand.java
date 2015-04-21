package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.util.Collections;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.StorageDomainVdsCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSGenericException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNonOperationalException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSProtocolException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSStoragePoolStatusException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSUnicodeArgumentException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsOperationFailedNoFailoverException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;

public abstract class StorageDomainMetadataCommand<P extends StorageDomainVdsCommandParameters> extends VdsBrokerCommand<P> {

    public StorageDomainMetadataCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        LockManager lockManager = LockManagerFactory.getLockManager();
        EngineLock domainLock = buildLock(getParameters().getStorageDomainId(), LockingGroup.DOMAIN_EXECUTION);
        try {
            getLockManager().acquireLockWait(domainLock);
            executeDomainCommand();
        } finally {
            lockManager.releaseLock(domainLock);
        }
    }

    private void logException(Throwable ex) {
        log.error("StorageDomainMetadataCommand::Failed::{}: {}", getCommandName(), ex.getMessage());
        log.debug("Exception", ex);
    }

    @Override
    protected void executeVDSCommand() {
        try {
            executeVdsBrokerCommand();
            }
        catch (UndeclaredThrowableException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(new VDSError(EngineError.VDS_NETWORK_ERROR, ex.getMessage()));
                if (ExceptionUtils.getRootCause(ex) != null) {
                    logException(ExceptionUtils.getRootCause(ex));
                } else {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                getVDSReturnValue().setCanTryOnDifferentVds(true);
        } catch (XmlRpcRunTimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex.isNetworkError()) {
                    log.error("StorageDomainMetadataCommand::Failed::{} - network exception.", getCommandName());
                    getVDSReturnValue().setSucceeded(false);
                } else {
                    log.error("StorageDomainMetadataCommand::Failed::{}", getCommandName());
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                    throw new IRSProtocolException(ex);
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
                getVDSReturnValue().setCanTryOnDifferentVds(true);
        } catch (IRSErrorException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                if (log.isDebugEnabled()) {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                getVDSReturnValue().setCanTryOnDifferentVds(true);
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
                getVDSReturnValue().setCanTryOnDifferentVds(true);
            }
    }

    private EngineLock buildLock(Guid id, LockingGroup lockingGroup) {
        return new EngineLock(
                Collections.singletonMap(id.toString(), new Pair<>(lockingGroup.toString(), EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.toString())), null);
    }

    private LockManager getLockManager() {
        return LockManagerFactory.getLockManager();
    }

    protected abstract void executeDomainCommand();
}
