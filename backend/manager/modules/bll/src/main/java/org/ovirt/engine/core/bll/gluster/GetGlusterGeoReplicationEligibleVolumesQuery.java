package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.context.EngineContext;
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

    public GetGlusterGeoReplicationEligibleVolumesQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid masterVolumeId = getParameters().getId();
        GlusterVolumeEntity masterVolume = glusterVolumeDao.getById(masterVolumeId);
        getQueryReturnValue().setReturnValue(getEligibleVolumes(masterVolume));
    }

    public List<GlusterVolumeEntity> getEligibleVolumes(GlusterVolumeEntity masterVolume) {
        List<GlusterVolumeEntity> possiblyEligibleVolumes = getAllGlusterVolumesWithMasterCompatibleVersion(masterVolume.getId());
        Map<GlusterGeoRepNonEligibilityReason, Predicate<GlusterVolumeEntity>> eligibilityPredicateMap = getGeoRepUtilInstance().getEligibilityPredicates(masterVolume);
        Predicate<GlusterVolumeEntity> andPredicate = eligibilityPredicateMap.values().stream().reduce(Predicate::and).orElse(t->true);
        return possiblyEligibleVolumes.stream().filter(andPredicate).collect(Collectors.toList());
    }

    protected List<GlusterVolumeEntity> getAllGlusterVolumesWithMasterCompatibleVersion(Guid masterVolumeId) {
        GlusterVolumeEntity masterVolume = glusterVolumeDao.getById(masterVolumeId);
        Cluster masterCluster = clusterDao.get(masterVolume.getClusterId());
        List<Cluster> clusters = clusterDao.getClustersByServiceAndCompatibilityVersion(true, false, masterCluster.getCompatibilityVersion().getValue());
        List<GlusterVolumeEntity> volumes = new ArrayList<>();
        if(clusters != null) {
            for(Cluster currentCluster : clusters) {
                if(!currentCluster.getId().equals(masterCluster.getId())) {
                    volumes.addAll(glusterVolumeDao.getByClusterId(currentCluster.getId()));
                }
            }
        }
        return volumes;
    }

}
