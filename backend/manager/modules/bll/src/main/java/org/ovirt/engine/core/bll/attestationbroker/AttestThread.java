package org.ovirt.engine.core.bll.attestationbroker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.businessentities.AttestationResultEnum;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.attestation.AttestationService;
import org.ovirt.engine.core.vdsbroker.attestation.AttestationValue;

public class AttestThread extends Thread {

    private List<String> trustedHostsNames;
    private final int FIRST_STAGE_QUERY_SIZE = Config.<Integer> getValue(ConfigValues.AttestationFirstStageSize);
    private final int SECOND_STAGE_QUERY_SIZE = Config.<Integer> getValue(ConfigValues.AttestationSecondStageSize);
    private static final Set<Guid> trustedVdses = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public AttestThread(List<String> trustedHostsNames) {
        this.trustedHostsNames = trustedHostsNames;
    }

    @Override
    public void run() {
        List<AttestationValue> values;
        if (trustedHostsNames.size() > FIRST_STAGE_QUERY_SIZE) {
            values = AttestationService.INSTANCE.attestHosts(trustedHostsNames.subList(0, FIRST_STAGE_QUERY_SIZE));
            handleValues(values);
            for (int from = FIRST_STAGE_QUERY_SIZE; from < trustedHostsNames.size(); from+= SECOND_STAGE_QUERY_SIZE) {
                int to = (from + SECOND_STAGE_QUERY_SIZE < trustedHostsNames.size()) ?
                        from + SECOND_STAGE_QUERY_SIZE : trustedHostsNames.size();
                values = AttestationService.INSTANCE. attestHosts(trustedHostsNames.subList(from, to));
                handleValues(values);
            }
        } else {
            values = AttestationService.INSTANCE.attestHosts(trustedHostsNames);
            handleValues(values);
        }
    }


    private void handleValues(List<AttestationValue> values) {
        for (AttestationValue value : values) {
            List<VDS> vdses = Injector.get(VdsDao.class).getAllForHostname(value.getHostName());
            if (vdses != null && vdses.size() > 0) {
                VDS vds = vdses.get(0);
                if (value.getTrustLevel().equals(AttestationResultEnum.TRUSTED)) {
                    moveVdsToUp(vds);
                } else {
                    setNonOperational(NonOperationalReason.UNTRUSTED, vds, null);
                }
            }
        }
    }

    private void setNonOperational(NonOperationalReason reason, VDS vds, Map<String, String> customLogValues) {
        SetNonOperationalVdsParameters tempVar =
                new SetNonOperationalVdsParameters(vds.getId(), reason, customLogValues);
        Injector.get(BackendInternal.class).runInternalAction(ActionType.SetNonOperationalVds, tempVar,
                ExecutionHandler.createInternalJobContext());
    }

    private void moveVdsToUp(VDS vds) {
        trustedVdses.add(vds.getId());
        vds.setStatus(VDSStatus.Up);
        Injector.get(VdsDynamicDao.class).update(vds.getDynamicData());
    }

    public static boolean isTrustedVds(Guid vdsId) {
        return trustedVdses.remove(vdsId);
    }
}
