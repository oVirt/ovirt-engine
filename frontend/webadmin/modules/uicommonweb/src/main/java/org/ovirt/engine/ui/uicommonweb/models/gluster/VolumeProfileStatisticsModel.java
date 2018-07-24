package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.BrickProfileDetails;
import org.ovirt.engine.core.common.businessentities.gluster.FopStats;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeProfileStats;
import org.ovirt.engine.core.common.businessentities.gluster.StatsInfo;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIMessages;

import com.google.gwt.i18n.client.NumberFormat;

public class VolumeProfileStatisticsModel extends Model {
    private ListModel cumulativeStatistics;
    private ListModel<BrickProfileDetails> bricks;
    private String profileRunTime;
    private String bytesRead;
    private String bytesWritten;

    private ListModel nfsServerProfileStats;
    private ListModel<GlusterVolumeProfileStats> nfsServers;
    private String nfsProfileRunTime;
    private String nfsBytesRead;
    private String nfsBytesWritten;

    private Guid clusterId;
    private Guid volumeId;
    private String volumeName;

    private String profileExportUrl;

    private GlusterVolumeProfileInfo profileInfo;

    private boolean successfulProfileStatsFetch;

    public ListModel getCumulativeStatistics() {
        return cumulativeStatistics;
    }
    public void setCumulativeStatistics(ListModel cumulativeStatistics) {
        this.cumulativeStatistics = cumulativeStatistics;
    }
    public ListModel<BrickProfileDetails> getBricks() {
        return bricks;
    }
    public void setBricks(ListModel<BrickProfileDetails> bricks) {
        this.bricks = bricks;
    }
    public String getProfileRunTime() {
        return profileRunTime;
    }
    public void setProfileRunTime(String profileRunTime) {
        this.profileRunTime = profileRunTime;
    }
    public String getBytesRead() {
        return bytesRead;
    }
    public void setBytesRead(String bytesRead) {
        this.bytesRead = bytesRead;
    }
    public String getBytesWritten() {
        return bytesWritten;
    }
    public void setBytesWritten(String bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public ListModel getNfsServerProfileStats() {
        return nfsServerProfileStats;
    }
    public void setNfsServerProfileStats(ListModel nfsServerProfileStats) {
        this.nfsServerProfileStats = nfsServerProfileStats;
    }
    public ListModel<GlusterVolumeProfileStats> getNfsServers() {
        return nfsServers;
    }
    public void setNfsServers(ListModel<GlusterVolumeProfileStats> nfsServers) {
        this.nfsServers = nfsServers;
    }
    public String getNfsProfileRunTime() {
        return nfsProfileRunTime;
    }
    public void setNfsProfileRunTime(String nfsProfileRunTime) {
        this.nfsProfileRunTime = nfsProfileRunTime;
    }
    public String getNfsBytesRead() {
        return nfsBytesRead;
    }
    public void setNfsBytesRead(String nfsBytesRead) {
        this.nfsBytesRead = nfsBytesRead;
    }
    public String getNfsBytesWritten() {
        return nfsBytesWritten;
    }
    public void setNfsBytesWritten(String nfsBytesWritten) {
        this.nfsBytesWritten = nfsBytesWritten;
    }

    public GlusterVolumeProfileInfo getProfileInfo() {
        return profileInfo;
    }

    public void setProfileInfo(GlusterVolumeProfileInfo profileInfo) {
        this.profileInfo = profileInfo;
    }

    public boolean isSuccessfulProfileStatsFetch() {
        return successfulProfileStatsFetch;
    }

    public void setSuccessfulProfileStatsFetch(boolean successfulProfileStatsFetch) {
        this.successfulProfileStatsFetch = successfulProfileStatsFetch;
        onPropertyChanged(new PropertyChangedEventArgs("statusOfFetchingProfileStats"));//$NON-NLS-1$
    }

    public VolumeProfileStatisticsModel(Guid clusterId, Guid volumeId, String volumeName) {
        this.clusterId = clusterId;
        this.volumeId = volumeId;
        this.volumeName = volumeName;
        setProfileInfo(new GlusterVolumeProfileInfo());
        setCumulativeStatistics(new ListModel());
        setNfsServerProfileStats(new ListModel());
        setBricks(new ListModel<BrickProfileDetails>());
        setNfsServers(new ListModel<GlusterVolumeProfileStats>());
        final UIMessages messages = ConstantsManager.getInstance().getMessages();
        getBricks().getSelectedItemChangedEvent().addListener((ev, sender, args) -> onBrickSelectionChange(messages));
        getNfsServers().getSelectedItemChangedEvent().addListener((ev, sender, args) -> onNfsServerSelectionChange(messages));
        setProfileRunTime("");//$NON-NLS-1$
        setNfsProfileRunTime("");//$NON-NLS-1$
        setBytesRead("");//$NON-NLS-1$
        setNfsBytesRead("");//$NON-NLS-1$
        setBytesWritten("");//$NON-NLS-1$
        setNfsBytesWritten("");//$NON-NLS-1$
    }

    private void onNfsServerSelectionChange(UIMessages messages) {
        if(getNfsServers().getSelectedItem() == null) {
            return;
        }
        int index = getProfileInfo().getNfsProfileDetails().indexOf(getNfsServers().getSelectedItem());
        if(index < 0) {
            return;
        }
        List<GlusterVolumeProfileStats> nfsProfileStats = getProfileInfo().getNfsProfileDetails();
        StatsInfo selectedNfsServerCummulativeProfile = nfsProfileStats.get(index).getProfileStats().get(0);
        StatsInfo selectedNfsServerIntervalProfile = nfsProfileStats.get(index).getProfileStats().get(1);
        populateCummulativeStatistics(selectedNfsServerCummulativeProfile.getFopStats(), getNfsServerProfileStats());

        nfsProfileRunTime = formatRunTime(messages, selectedNfsServerCummulativeProfile.getDurationFormatted(), selectedNfsServerIntervalProfile.getDurationFormatted());
        onPropertyChanged(new PropertyChangedEventArgs("nfsProfileRunTimeChanged"));//$NON-NLS-1$

        nfsBytesRead = formatDataRead(messages, selectedNfsServerCummulativeProfile.getTotalRead(), selectedNfsServerIntervalProfile.getTotalRead());
        onPropertyChanged(new PropertyChangedEventArgs("nfsProfileDataRead"));//$NON-NLS-1$

        nfsBytesWritten = formatDataWritten(messages, selectedNfsServerCummulativeProfile.getTotalWrite(), selectedNfsServerIntervalProfile.getTotalWrite());
        onPropertyChanged(new PropertyChangedEventArgs("nfsProfileDataWritten"));//$NON-NLS-1$
    }

    private void onBrickSelectionChange(UIMessages messages) {
        if(getBricks().getSelectedItem() == null) {
            return;
        }
        int index = getProfileInfo().getBrickProfileDetails().indexOf(getBricks().getSelectedItem());
        if(index < 0) {
            return;
        }
        List<BrickProfileDetails> profileStats = getProfileInfo().getBrickProfileDetails();
        StatsInfo selectedBrickProfileCummulativeStats = profileStats.get(index).getProfileStats().get(0);
        StatsInfo selectedBrickProfileIntervalStats = profileStats.get(index).getProfileStats().get(1);

        populateCummulativeStatistics(selectedBrickProfileCummulativeStats.getFopStats(), getCumulativeStatistics());

        profileRunTime = formatRunTime(messages, selectedBrickProfileCummulativeStats.getDurationFormatted(), selectedBrickProfileIntervalStats.getDurationFormatted());
        onPropertyChanged(new PropertyChangedEventArgs("brickProfileRunTimeChanged"));//$NON-NLS-1$

        bytesRead = formatDataRead(messages, selectedBrickProfileCummulativeStats.getTotalRead(), selectedBrickProfileIntervalStats.getTotalRead());
        onPropertyChanged(new PropertyChangedEventArgs("brickProfileDataRead"));//$NON-NLS-1$

        bytesWritten = formatDataWritten(messages, selectedBrickProfileCummulativeStats.getTotalWrite(), selectedBrickProfileIntervalStats.getTotalWrite());
        onPropertyChanged(new PropertyChangedEventArgs("brickProfileDataWritten"));//$NON-NLS-1$
    }

    public void queryBackend(final boolean isNfs) {
        startProgress(ConstantsManager.getInstance().getConstants().fetchingDataMessage());

        AsyncDataProvider.getInstance().getGlusterVolumeProfilingStatistics(new AsyncQuery<>(returnValue -> {
            stopProgress();
            GlusterVolumeProfileInfo profileInfoEntity =returnValue.getReturnValue();
            if((profileInfoEntity == null) || !returnValue.getSucceeded()) {
                setSuccessfulProfileStatsFetch(false);
                if (isNfs) {
                    showNfsProfileStats(profileInfoEntity);
                } else {
                    showProfileStats(profileInfoEntity);
                }
            } else {
                GlusterVolumeProfileInfo aggregatedProfileInfo = new GlusterVolumeProfileInfo();
                aggregatedProfileInfo.setBrickProfileDetails((profileInfoEntity.getBrickProfileDetails() != null) ? profileInfoEntity.getBrickProfileDetails() : getProfileInfo().getBrickProfileDetails());
                aggregatedProfileInfo.setNfsProfileDetails((profileInfoEntity.getNfsProfileDetails() != null) ? profileInfoEntity.getNfsProfileDetails() : getProfileInfo().getNfsProfileDetails());
                setProfileExportUrl(formProfileUrl(clusterId.toString(), volumeId.toString(), isNfs));
                setProfileInfo(aggregatedProfileInfo);
                setSuccessfulProfileStatsFetch(true);
                setTitle(ConstantsManager.getInstance().getMessages().volumeProfilingStatsTitle(volumeName));
                if (isNfs) {
                    showNfsProfileStats(profileInfoEntity);
                } else {
                    showProfileStats(profileInfoEntity);
                }
            }
        }), clusterId, volumeId, isNfs);
    }

    public void showProfileStats(GlusterVolumeProfileInfo entity) {
        if (entity != null) {
            final List<BrickProfileDetails> brickProfileDetails = entity.getBrickProfileDetails();
            getBricks().setItems(brickProfileDetails);
            if(brickProfileDetails.size() > 0) {
                getBricks().setSelectedItem(brickProfileDetails.get(0));
            }
        }
    }

    private void populateCummulativeStatistics(List<FopStats> fopStats, ListModel profileStats) {
        List<EntityModel<FopStats>> fopStatsEntities = new ArrayList<>();
        for(int i = 0;i < fopStats.size();i++) {
            EntityModel<FopStats> fopStatEntity = new EntityModel<>(fopStats.get(i));
            fopStatsEntities.add(fopStatEntity);
        }
        profileStats.setItems(fopStatsEntities);
    }

    public void showNfsProfileStats(GlusterVolumeProfileInfo entity) {
        if(entity != null) {
            List<GlusterVolumeProfileStats> nfsProfileDetails = entity.getNfsProfileDetails();
            getNfsServers().setItems(nfsProfileDetails);
            if(nfsProfileDetails.size() > 0) {
                getNfsServers().setSelectedItem(nfsProfileDetails.get(0));
            }
        }
    }

    private String formatRunTime(UIMessages messages, Pair<Integer, String> runTimeConverted, Pair<Integer, String> intervalRunTimeConverted) {
        return messages.glusterVolumeCurrentProfileRunTime(intervalRunTimeConverted.getFirst(), intervalRunTimeConverted.getSecond(), runTimeConverted.getFirst(), runTimeConverted.getSecond());
    }

    private String formatDataRead(UIMessages messages, long totalBytesRead, long bytesReadInCurrentInterval) {
        Pair<SizeUnit, Double> dataReadInCurrentInterval = SizeConverter.autoConvert(bytesReadInCurrentInterval, SizeUnit.BYTES);
        Pair<SizeUnit, Double> dataRead = SizeConverter.autoConvert(totalBytesRead, SizeUnit.BYTES);
        return messages.bytesReadInCurrentProfileInterval(formatSize(dataReadInCurrentInterval.getSecond()), dataReadInCurrentInterval.getFirst().name(), formatSize(dataRead.getSecond()), dataRead.getFirst().toString());
    }

    private String formatDataWritten(UIMessages messages, long totalBytesWritten, long bytesWrittenInCurrentInterval) {
        Pair<SizeUnit, Double> dataWrittenInCurrentInterval = SizeConverter.autoConvert(bytesWrittenInCurrentInterval, SizeUnit.BYTES);
        Pair<SizeUnit, Double> dataWritten = SizeConverter.autoConvert(totalBytesWritten, SizeUnit.BYTES);
        return messages.bytesWrittenInCurrentProfileInterval(formatSize(dataWrittenInCurrentInterval.getSecond()), dataWrittenInCurrentInterval.getFirst().toString(), formatSize(dataWritten.getSecond()), dataWritten.getFirst().toString());
    }

    private String formProfileUrl(String clusterId, String volumeId, boolean isBrickProfileSelected) {
        String apiMatrixParam = !isBrickProfileSelected ? ";nfsStatistics=true" : "";//$NON-NLS-1$//$NON-NLS-2$
        return StringFormat.format("/ovirt-engine/api/clusters/%s/glustervolumes/%s/profilestatistics%s?accept=application/json", clusterId, volumeId, apiMatrixParam);//$NON-NLS-1$
    }

    public String getProfileExportUrl() {
        return profileExportUrl;
    }

    public void setProfileExportUrl(String profileExportUrl) {
        this.profileExportUrl = profileExportUrl;
    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }
}
