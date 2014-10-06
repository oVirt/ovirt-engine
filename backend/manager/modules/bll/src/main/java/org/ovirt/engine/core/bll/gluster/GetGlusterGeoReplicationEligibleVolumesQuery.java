package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.GlusterGeoRepUtil;
import org.ovirt.engine.core.bll.utils.Injector;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetGlusterGeoReplicationEligibleVolumesQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<IdQueryParameters> {

    public GlusterGeoRepUtil getGeoRepUtilInstance() {
        return Injector.get(GlusterGeoRepUtil.class);
    }

    public GetGlusterGeoReplicationEligibleVolumesQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid masterVolumeId = getParameters().getId();
        GlusterVolumeEntity masterVolume = getGlusterVolumeDao().getById(masterVolumeId);
        getQueryReturnValue().setReturnValue(getEligibleVolumes(masterVolume));
    }

    public List<GlusterVolumeEntity> getEligibleVolumes(GlusterVolumeEntity masterVolume) {
        List<GlusterVolumeEntity> possiblyEligibleVolumes = getAllGlusterVolumesWithMasterCompatibleVersion(masterVolume.getId());
        Map<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> eligibilityPredicateMap = getGeoRepUtilInstance().getEligibilityPredicates(masterVolume);
        for(Map.Entry<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> eligibilityPredicateMapEntries : eligibilityPredicateMap.entrySet()) {
            possiblyEligibleVolumes = LinqUtils.filter(possiblyEligibleVolumes, eligibilityPredicateMapEntries.getValue());
        }
        return possiblyEligibleVolumes;
    }

    private List<GlusterVolumeEntity> getAllGlusterVolumesWithMasterCompatibleVersion(Guid masterVolumeId) {
        GlusterVolumeEntity masterVolume = getGlusterVolumeDao().getById(masterVolumeId);
        VDSGroup masterCluster = getVdsGroupDao().get(masterVolume.getClusterId());
        List<VDSGroup> clusters = getVdsGroupDao().getClustersByServiceAndCompatibilityVersion(true, false, masterCluster.getcompatibility_version().getValue());
        List<GlusterVolumeEntity> volumes = new ArrayList<>();
        if(clusters != null) {
            for(VDSGroup currentCluster : clusters) {
                if(!currentCluster.getId().equals(masterCluster.getId())) {
                    volumes.addAll(getGlusterVolumeDao().getByClusterId(currentCluster.getId()));
                }
            }
        }
        return volumes;
    }

}
