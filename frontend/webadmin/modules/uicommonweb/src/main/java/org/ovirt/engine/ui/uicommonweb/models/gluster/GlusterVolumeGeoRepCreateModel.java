package org.ovirt.engine.ui.uicommonweb.models.gluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class GlusterVolumeGeoRepCreateModel extends Model{

    private EntityModel<Boolean> showEligibleVolumes;
    private EntityModel<String> slaveUserName;
    private ListModel<GlusterVolumeEntity> slaveVolumes;
    private ListModel<String> slaveClusters;
    private ListModel<Pair<String, Guid>> slaveHosts;
    private EntityModel<Boolean> startSession;
    private String queryFailureMessage;
    private Collection<GlusterVolumeEntity> volumeList = new ArrayList<>();
    private final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final UIMessages messages = ConstantsManager.getInstance().getMessages();
    private GlusterVolumeEntity masterVolume;
    private String recommendationViolations;
    private EntityModel<String> slaveUserGroupName;

    public GlusterVolumeGeoRepCreateModel(GlusterVolumeEntity masterVolume) {
        this.masterVolume = masterVolume;
        init();
        initValueChangeListeners();
    }

    private void initValueChangeListeners() {
        getShowEligibleVolumes().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (!getShowEligibleVolumes().getEntity()) {
                getVolumesForForceSessionCreate();
            } else {
                getEligibleVolumes();
                setRecommendationViolations(null);
            }
        });

        IEventListener<EventArgs> clusterEventListener = (ev, sender, args) -> {
            String selectedCluster = getSlaveClusters().getSelectedItem();
            List<GlusterVolumeEntity> volumesInCurrentCluster = new ArrayList<>();
            if (selectedCluster != null) {
                volumesInCurrentCluster = getVolumesInCluster(selectedCluster, getVolumeList());
            }
            if (volumesInCurrentCluster.size() > 0) {
                getSlaveVolumes().setItems(volumesInCurrentCluster, volumesInCurrentCluster.get(0));
            } else {
                getSlaveVolumes().setItems(volumesInCurrentCluster);
            }
        };
        getSlaveClusters().getSelectedItemChangedEvent().addListener(clusterEventListener);

        IEventListener<EventArgs> slaveVolumeEventListener = (ev, sender, args) -> {
            GlusterVolumeEntity selectedSlaveVolume = getSlaveVolumes().getSelectedItem();
            Set<Pair<String, Guid>> hostsInCurrentVolume = new HashSet<>();
            if (!getShowEligibleVolumes().getEntity() && selectedSlaveVolume != null) {
                updateRecommendatonViolations();
            }
            if (selectedSlaveVolume != null) {
                hostsInCurrentVolume = getHostNamesForVolume(selectedSlaveVolume);
            }
            getSlaveHosts().setItems(hostsInCurrentVolume);
        };
        getSlaveVolumes().getSelectedItemChangedEvent().addListener(slaveVolumeEventListener);

        getSlaveUserName().getEntityChangedEvent().addListener((ev, sender, args) -> {
            String slaveUser = getSlaveUserName().getEntity();
            getSlaveUserGroupName().setIsChangeable(
                    slaveUser != null && !slaveUser.equalsIgnoreCase(ConstantsManager.getInstance()
                            .getConstants()
                            .rootUser()));
        });
    }

    public void getVolumesForForceSessionCreate() {
        GlusterVolumeGeoRepCreateModel.this.startProgress(constants.fetchingDataMessage());
        SearchParameters volumesSearchParameters = new SearchParameters("Volumes:", SearchType.GlusterVolume, false);//$NON-NLS-1$
        volumesSearchParameters.setRefresh(true);

        Frontend.getInstance().runQuery(QueryType.Search, volumesSearchParameters, new AsyncQuery<QueryReturnValue>(returnValue -> showAvailableVolumes(returnValue)));
    }

    private void showAvailableVolumes(QueryReturnValue returnValue) {
        stopProgress();
        if(!returnValue.getSucceeded()) {
            setQueryFailureMessage(returnValue.getExceptionString());
        } else {
            setVolumeList((Collection) returnValue.getReturnValue());
            Set<String> clusterForVolumes = getClusterForVolumes(getVolumeList());
            getSlaveClusters().setItems(clusterForVolumes,
                    clusterForVolumes.isEmpty() ? null : clusterForVolumes.iterator().next());
        }
    }

    public void getEligibleVolumes() {
        this.startProgress(constants.fetchingDataMessage());

        Frontend.getInstance().runQuery(QueryType.GetGlusterGeoReplicationEligibleVolumes, new IdQueryParameters(masterVolume.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> showAvailableVolumes(returnValue)));
    }

    protected Set<String> getClusterForVolumes(Collection<GlusterVolumeEntity> eligibleVolumes) {
        Set<String> clusters = new HashSet<>();
        for(GlusterVolumeEntity currentVolume : eligibleVolumes) {
            clusters.add(currentVolume.getClusterName());
        }
        return clusters;
    }

    public List<GlusterVolumeEntity> getVolumesInCluster(String cluster, Collection<GlusterVolumeEntity> volumes) {
        List<GlusterVolumeEntity> volumesInCurrentCluster= new ArrayList<>();
        for(GlusterVolumeEntity currentVolume : volumes) {
            if(currentVolume.getClusterName().equals(cluster)) {
                volumesInCurrentCluster.add(currentVolume);
            }
        }
        return volumesInCurrentCluster;
    }

    public Set<Pair<String, Guid>> getHostNamesForVolume(GlusterVolumeEntity volume) {
        Set<Pair<String, Guid>> hosts = new HashSet<>();
        for(GlusterBrickEntity currentBrick : volume.getBricks()) {
            hosts.add(new Pair<>(currentBrick.getServerName(), currentBrick.getServerId()));
        }
        return hosts;
    }

    public ListModel<String> getSlaveClusters() {
        return slaveClusters;
    }

    public void setSlaveClusters(ListModel<String> slaveClusters) {
        this.slaveClusters = slaveClusters;
    }

    public ListModel<Pair<String, Guid>> getSlaveHosts() {
        return slaveHosts;
    }

    public void setSlaveHosts(ListModel<Pair<String, Guid>> slaveHosts) {
        this.slaveHosts = slaveHosts;
    }

    public Collection<GlusterVolumeEntity> getVolumeList() {
        return volumeList;
    }

    public void setVolumeList(Collection<GlusterVolumeEntity> volumeList) {
        this.volumeList = volumeList;
        onPropertyChanged(new PropertyChangedEventArgs("RecommendationViolations"));//$NON-NLS-1$
    }

    private void init() {
        setTitle(constants.newGeoRepSessionTitle());
        setHelpTag(HelpTag.volume_geo_rep_create);
        setHashName("volume_geo_rep_create");//$NON-NLS-1$

        setShowEligibleVolumes(new EntityModel<Boolean>());
        setSlaveClusters(new ListModel<String>());
        setSlaveVolumes(new ListModel<GlusterVolumeEntity>());
        setSlaveHosts(new ListModel<Pair<String, Guid>>());

        setStartSession(new EntityModel<Boolean>());
        setSlaveUserName(new EntityModel<>(constants.emptyString()));
        setSlaveUserGroupName(new EntityModel<String>());
    }

    public EntityModel<String> getSlaveUserName() {
        return slaveUserName;
    }

    public void setSlaveUserName(EntityModel<String> slaveUserName) {
        this.slaveUserName = slaveUserName;
    }

    public ListModel<GlusterVolumeEntity> getSlaveVolumes() {
        return slaveVolumes;
    }

    public void setSlaveVolumes(ListModel<GlusterVolumeEntity> slaveVolumeSelected) {
        this.slaveVolumes = slaveVolumeSelected;
    }

    public EntityModel<Boolean> getStartSession() {
        return startSession;
    }

    public void setStartSession(EntityModel<Boolean> startSession) {
        this.startSession = startSession;
    }

    public EntityModel<Boolean> getShowEligibleVolumes() {
        return showEligibleVolumes;
    }

    public void setShowEligibleVolumes(EntityModel<Boolean> showEligibleVolumes) {
        this.showEligibleVolumes = showEligibleVolumes;
    }

    public GlusterVolumeEntity getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(GlusterVolumeEntity masterVolume) {
        this.masterVolume = masterVolume;
    }

    public String getRecommendationViolations() {
        return recommendationViolations;
    }

    public void setRecommendationViolations(String recommendationViolations) {
        this.recommendationViolations = recommendationViolations;
        onPropertyChanged(new PropertyChangedEventArgs("RecommendationViolations"));//$NON-NLS-1$
    }

    public void updateRecommendatonViolations() {
        startProgress(constants.fetchingDataMessage());
        AsyncDataProvider.getInstance().getGlusterVolumeGeoRepRecommendationViolations(new AsyncQuery<>(
                eligibilityViolators -> {
                    stopProgress();
                    if(eligibilityViolators.size() > 0) {
                        StringBuilder configViolations = new StringBuilder(constants.geoReplicationRecommendedConfigViolation());
                        for(GlusterGeoRepNonEligibilityReason currentViolator : eligibilityViolators) {
                            configViolations.append("\n* ");//$NON-NLS-1$
                            configViolations.append(EnumTranslator.getInstance().translate(currentViolator));
                        }
                        setRecommendationViolations(configViolations.toString());
                    } else {
                        setRecommendationViolations(null);
                    }
                }), masterVolume.getId(), getSlaveVolumes().getSelectedItem().getId());
    }

    public boolean validate() {
        getSlaveVolumes().validateSelectedItem(new IValidation[] { new NotEmptyValidation(), new LengthValidation(128)});
        getSlaveHosts().validateSelectedItem(new IValidation[] { new NotEmptyValidation(), new LengthValidation(128)});
        return getSlaveVolumes().getIsValid() && getSlaveHosts().getIsValid();
    }

    public String getQueryFailureMessage() {
        return queryFailureMessage;
    }

    public void setQueryFailureMessage(String queryFailureMessage) {
        this.queryFailureMessage = queryFailureMessage;
        onPropertyChanged(new PropertyChangedEventArgs("QueryFailed"));//$NON-NLS-1$
    }

    public EntityModel<String> getSlaveUserGroupName() {
        return slaveUserGroupName;
    }

    public void setSlaveUserGroupName(EntityModel<String> slaveUserGroupName) {
        this.slaveUserGroupName = slaveUserGroupName;
    }
}
