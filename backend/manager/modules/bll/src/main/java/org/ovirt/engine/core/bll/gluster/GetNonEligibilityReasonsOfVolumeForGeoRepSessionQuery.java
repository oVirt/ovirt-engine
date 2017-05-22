package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.GlusterGeoRepUtil;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeGeoRepEligibilityParameters;
import org.ovirt.engine.core.di.Injector;

public class GetNonEligibilityReasonsOfVolumeForGeoRepSessionQuery<P extends GlusterVolumeGeoRepEligibilityParameters> extends GlusterQueriesCommandBase<P> {

    public GetNonEligibilityReasonsOfVolumeForGeoRepSessionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        GlusterVolumeEntity masterVolume = glusterVolumeDao.getById(getParameters().getId());
        GlusterVolumeEntity slaveVolume = glusterVolumeDao.getById(getParameters().getSlaveVolumeId());
        getQueryReturnValue().setReturnValue(getNonEligibilityReasons(masterVolume, slaveVolume));
    }

    protected List<GlusterGeoRepNonEligibilityReason> getNonEligibilityReasons(GlusterVolumeEntity masterVolume, GlusterVolumeEntity slaveVolume) {
        List<GlusterGeoRepNonEligibilityReason> nonEligibilityreasons = new ArrayList<>();
        Map<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> eligibilityPredicateMap = getGeoRepUtilInstance().getEligibilityPredicates(masterVolume);
        for(Map.Entry<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> eligibilityPredicateMapEntries : eligibilityPredicateMap.entrySet()) {
            if(!eligibilityPredicateMapEntries.getValue().test(slaveVolume)) {
                nonEligibilityreasons.add(eligibilityPredicateMapEntries.getKey());
            }
        }
        return nonEligibilityreasons;
    }

    protected GlusterGeoRepUtil getGeoRepUtilInstance() {
        return Injector.get(GlusterGeoRepUtil.class);
    }
}
