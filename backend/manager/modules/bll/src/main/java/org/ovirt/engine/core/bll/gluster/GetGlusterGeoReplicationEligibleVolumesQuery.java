package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.utils.GlusterGeoRepUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;

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
        Predicate<GlusterVolumeEntity> andPredicate = eligibilityPredicateMap.values().stream().reduce(Predicate::and).orElse(t->true);
        return possiblyEligibleVolumes.stream().filter(andPredicate).collect(Collectors.toList());
    }

    protected List<GlusterVolumeEntity> getAllGlusterVolumesWithMasterCompatibleVersion(Guid masterVolumeId) {
        GlusterVolumeEntity masterVolume = getGlusterVolumeDao().getById(masterVolumeId);
        Cluster masterCluster = getClusterDao().get(masterVolume.getClusterId());
        List<Cluster> clusters = getClusterDao().getClustersByServiceAndCompatibilityVersion(true, false, masterCluster.getCompatibilityVersion().getValue());
        List<GlusterVolumeEntity> volumes = new ArrayList<>();
        if(clusters != null) {
            for(Cluster currentCluster : clusters) {
                if(!currentCluster.getId().equals(masterCluster.getId())) {
                    volumes.addAll(getGlusterVolumeDao().getByClusterId(currentCluster.getId()));
                }
            }
        }
        return volumes;
    }

}
