package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.StorageJobVdsCommandParameters;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.ovirt.engine.core.vdsbroker.TransportRunTimeException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSGenericException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNonOperationalException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSProtocolException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSStoragePoolStatusException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSUnicodeArgumentException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsOperationFailedNoFailoverException;

public abstract class StorageJobVDSCommand<P extends StorageJobVdsCommandParameters> extends VdsBrokerCommand<P> {

    public StorageJobVDSCommand(P parameters) {
        super(parameters);
    }

    private void logException(Throwable ex) {
        log.error("StorageJobCommand::Failed::{}: {}", getCommandName(), ex.getMessage());
        log.debug("Exception", ex);
    }

    @Override
    protected void executeVDSCommand() {
        try {
            executeVdsBrokerCommand();
        } catch (UndeclaredThrowableException ex) {
            getVDSReturnValue().setExceptionString(ex.toString());
            getVDSReturnValue().setExceptionObject(ex);
            getVDSReturnValue().setVdsError(new VDSError(EngineError.VDS_NETWORK_ERROR, ex.getMessage()));
            if (ExceptionUtils.getRootCause(ex) != null) {
                logException(ExceptionUtils.getRootCause(ex));
            } else {
                LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
            }
        } catch (TransportRunTimeException ex) {
            getVDSReturnValue().setExceptionString(ex.toString());
            getVDSReturnValue().setExceptionObject(ex);
            if (ex.isNetworkError()) {
                log.error("StorageJobCommand::Failed::{} - network exception.", getCommandName());
                getVDSReturnValue().setSucceeded(false);
            } else {
                log.error("StorageJobCommand::Failed::{}", getCommandName());
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
}
