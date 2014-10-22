package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.SpmStatus;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNonOperationalException;

@Logged(executionLevel = LogLevel.DEBUG)
public class SpmStatusVDSCommand<P extends SpmStatusVDSCommandParameters> extends VdsBrokerCommand<P> {
    private SpmStatusReturnForXmlRpc _result;

    public SpmStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().spmStatus(getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        setReturnValue(ParseSpmStatusResult(_result.spmStatus));
        if (((SpmStatusResult) getReturnValue()).getSpmStatus() == SpmStatus.SPM_ERROR) {
            log.error("SPM '{}' status returned SPM_ERROR on VDS '{}'", getParameters().getStoragePoolId(),
                    getParameters().getVdsId());
            throw new IRSNonOperationalException("SPM status returned SPM_ERROR");
        }
    }

    @Override
    protected void proceedProxyReturnValue() {
        VdcBllErrors returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case StoragePoolUnknown:
            // ignore this, the parser can handle the empty result.
            break;

        default:
            super.proceedProxyReturnValue();
            initializeVdsError(returnStatus);
            break;
        }
    }

    private SpmStatusResult ParseSpmStatusResult(Map<String, Object> spmStatusResult) {
        try {
            // The parser handle an empty structure because of Mr. Frank
            // inability create a protocol that makes sense.
            SpmStatusResult statusResult = new SpmStatusResult();
            statusResult
                    .setSpmStatus((_result.spmStatus != null && _result.spmStatus.containsKey("spmStatus")) ? EnumUtils
                            .valueOf(SpmStatus.class, _result.spmStatus.get("spmStatus").toString(), true)
                            : SpmStatus.Unknown_Pool);

            // if no lver and id put -5 hardcoded because of VDSM inconsistency
            statusResult
                    .setSpmLVER((_result.spmStatus != null && _result.spmStatus.containsKey("spmLver")) ? _result.spmStatus
                            .get("spmLver").toString()
                            : "-5");
            statusResult.setSpmId((_result.spmStatus != null && _result.spmStatus.containsKey("spmId")) ? Integer
                    .parseInt(_result.spmStatus.get("spmId").toString()) : -5);
            return statusResult;
        } catch (RuntimeException exp) {
            log.error("Could not parse SPM Status: '{}'", _result.spmStatus);
            throw exp;
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
