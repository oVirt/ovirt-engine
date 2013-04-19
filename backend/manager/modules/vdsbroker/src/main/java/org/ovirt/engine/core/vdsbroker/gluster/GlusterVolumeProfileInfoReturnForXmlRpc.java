package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.BlockStats;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProfileDetails;
import org.ovirt.engine.core.common.businessentities.gluster.FopStats;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileInfo;
import org.ovirt.engine.core.common.businessentities.gluster.ProfileStatsType;
import org.ovirt.engine.core.common.businessentities.gluster.StatsInfo;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

@SuppressWarnings("unchecked")
public final class GlusterVolumeProfileInfoReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String STATUS = "status";
    private static final String PROFILE_INFO = "profileInfo";
    private static final String VOLUME_NAME = "volumeName";
    private static final String BRICKS = "bricks";
    private static final String BRICK = "brick";
    private static final String CUMULATIVE_STATS = "cumulativeStats";
    private static final String BLOCK_STATS = "blockStats";
    private static final String FOP_STATS = "fopStats";
    private static final String DURATION = "duration";
    private static final String TOTAL_READ = "totalRead";
    private static final String TOTAL_WRITE = "totalWrite";
    private static final String INTERVAL_STATS = "intervalStats";
    private static final String SIZE = "size";
    private static final String READ = "read";
    private static final String WRITE = "write";
    private static final String NAME = "name";
    private static final String HITS = "hits";
    private static final String LATENCY_AVG = "latencyAvg";
    private static final String LATENCY_MIN = "latencyMin";
    private static final String LATENCY_MAX = "latencyMax";

    private StatusForXmlRpc status;

    private GlusterVolumeProfileInfo glusterVolumeProfileInfo = new GlusterVolumeProfileInfo();

    public GlusterVolumeProfileInfo getGlusterVolumeProfileInfo() {
        return glusterVolumeProfileInfo;
    }

    public GlusterVolumeProfileInfoReturnForXmlRpc(Guid clusterId, Map<String, Object> innerMap) {
        super(innerMap);
        status = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));

        Map<String, Object> profileInfo = (Map<String, Object>) innerMap.get(PROFILE_INFO);
        if (profileInfo != null) {
            String volumeName = (String) profileInfo.get(VOLUME_NAME);
            GlusterVolumeEntity volume = getGlusterVolumeDao().getByName(clusterId, volumeName);

            glusterVolumeProfileInfo.setVolumeId(volume.getId());
            glusterVolumeProfileInfo.setBrickProfileDetails(prepareBrickProfileDetails(volume,
                    (Object[]) profileInfo.get(BRICKS)));
        }
    }

    private List<BrickProfileDetails> prepareBrickProfileDetails(GlusterVolumeEntity volume,
            Object[] brickProfileDetails) {
        List<BrickProfileDetails> brickProfileDetailsList = new ArrayList<BrickProfileDetails>();
        for (Object brickProfileObj : brickProfileDetails) {
            BrickProfileDetails brickProfileDetail = new BrickProfileDetails();
            Map<String, Object> brickProfile = (Map<String, Object>) brickProfileObj;
            GlusterBrickEntity brick =
                    GlusterCoreUtil.getBrickByQualifiedName(volume.getBricks(), (String) brickProfile.get(BRICK));
            if (brick != null) {
                brickProfileDetail.setBrickId(brick.getId());
            }

            List<StatsInfo> statsInfo = new ArrayList<StatsInfo>();
            statsInfo.add(getStatInfo((Map<String, Object>) brickProfile.get(CUMULATIVE_STATS), CUMULATIVE_STATS));
            statsInfo.add(getStatInfo((Map<String, Object>) brickProfile.get(INTERVAL_STATS), INTERVAL_STATS));
            brickProfileDetail.setStatsInfo(statsInfo);
            brickProfileDetailsList.add(brickProfileDetail);
        }
        return brickProfileDetailsList;
    }

    private StatsInfo getStatInfo(Map<String, Object> statsInfoMap, String statType) {
        StatsInfo statsInfo = new StatsInfo();
        statsInfo.setDuration(Integer.valueOf((String) statsInfoMap.get(DURATION)));
        statsInfo.setTotalWrite(Integer.valueOf((String) statsInfoMap.get(TOTAL_WRITE)));
        statsInfo.setTotalRead(Integer.valueOf((String) statsInfoMap.get(TOTAL_READ)));
        statsInfo.setBlockStats(getBlockStats((Object[]) statsInfoMap.get(BLOCK_STATS)));
        statsInfo.setFopStats(getFopStats((Object[]) statsInfoMap.get(FOP_STATS)));
        statsInfo.setProfileStatsType((statType.equals(CUMULATIVE_STATS) ? ProfileStatsType.CUMULATIVE
                : ProfileStatsType.INTERVAL));
        return statsInfo;
    }

    private List<FopStats> getFopStats(Object[] fopStatsObjects) {
        List<FopStats> fopStatsList = new ArrayList<FopStats>();
        for (Object fopStatsObj : fopStatsObjects) {
            FopStats fopStats = new FopStats();
            Map<String, Object> fopStatsMap = (Map<String, Object>) fopStatsObj;
            fopStats.setHits(Integer.valueOf((String) fopStatsMap.get(HITS)));
            fopStats.setName((String) fopStatsMap.get(NAME));
            fopStats.setMinLatency(Double.valueOf((String) fopStatsMap.get(LATENCY_MIN)));
            fopStats.setMaxLatency(Double.valueOf((String) fopStatsMap.get(LATENCY_MAX)));
            fopStats.setAvgLatency(Double.valueOf((String) fopStatsMap.get(LATENCY_AVG)));
            fopStatsList.add(fopStats);
        }
        return fopStatsList;
    }

    private List<BlockStats> getBlockStats(Object[] blockStatsObjects) {
        List<BlockStats> blockStatsList = new ArrayList<BlockStats>();
        for (Object blockStatsObj : blockStatsObjects) {
            BlockStats blockStats = new BlockStats();
            Map<String, Object> blockStatsMap = (Map<String, Object>) blockStatsObj;
            blockStats.setSize(Double.valueOf((String) blockStatsMap.get(SIZE)));
            blockStats.setBlockRead(Double.valueOf((String) blockStatsMap.get(READ)));
            blockStats.setBlockWrite(Double.valueOf((String) blockStatsMap.get(WRITE)));
            blockStatsList.add(blockStats);
        }
        return blockStatsList;
    }

    protected GlusterVolumeDao getGlusterVolumeDao() {
        return DbFacade.getInstance().getGlusterVolumeDao();
    }

    public StatusForXmlRpc getStatus() {
        return status;
    }

    public void setStatus(StatusForXmlRpc status) {
        this.status = status;
    }
}
