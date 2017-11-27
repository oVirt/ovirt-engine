package org.ovirt.engine.ui.uicommonweb.models;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

public class SearchStringMapping {
    public static final String ERRATA_DEFAULT_SEARCH = "Errata"; //$NON-NLS-1$
    public static final String SESSION_DEFAULT_SEARCH = "Session"; //$NON-NLS-1$
    public static final String CLUSTER_DEFAULT_SEARCH = "Cluster"; //$NON-NLS-1$
    public static final String INSTANCE_TYPE_DEFAULT_SEARCH = "Instancetypes"; //$NON-NLS-1$
    public static final String DATACENTER_DEFAULT_SEARCH = "DataCenter"; //$NON-NLS-1$
    public static final String DISKS_DEFAULT_SEARCH = "Disks"; //$NON-NLS-1$
    public static final String EVENTS_DEFAULT_SEARCH = "Events"; //$NON-NLS-1$
    public static final String HOSTS_DEFAULT_SEARCH = "Host"; //$NON-NLS-1$
    public static final String NETWORK_DEFAULT_SEARCH = "Network"; //$NON-NLS-1$
    public static final String POOLS_DEFAULT_SEARCH = "Pools"; //$NON-NLS-1$
    public static final String VNIC_PROFILE_DEFAULT_SEARCH = "VnicProfile"; //$NON-NLS-1$
    public static final String PROVIDER_DEFAULT_SEARCH = "Provider"; //$NON-NLS-1$
    public static final String QUOTA_DEFAULT_SEARCH = "Quota"; //$NON-NLS-1$
    public static final String STORAGE_DEFAULT_SEARCH = "Storage"; //$NON-NLS-1$
    public static final String TEMPLATE_DEFAULT_SEARCH = "Template"; //$NON-NLS-1$
    public static final String USERS_DEFAULT_SEARCH = "Users"; //$NON-NLS-1$
    public static final String VMS_DEFAULT_SEARCH = "Vms"; //$NON-NLS-1$
    public static final String VOLUMES_DEFAULT_SEARCH = "Volumes"; //$NON-NLS-1$

    private static final Map<String, String> searchToPlaceMap = new HashMap<>();

    public static String getPlace(String defaultSearchString) {
        return searchToPlaceMap.get(defaultSearchString);
    }

    static {
        // Populate the map.
        searchToPlaceMap.put(ERRATA_DEFAULT_SEARCH, WebAdminApplicationPlaces.errataMainPlace);
        searchToPlaceMap.put(SESSION_DEFAULT_SEARCH, WebAdminApplicationPlaces.sessionMainPlace);
        searchToPlaceMap.put(CLUSTER_DEFAULT_SEARCH, WebAdminApplicationPlaces.clusterMainPlace);
        searchToPlaceMap.put(DATACENTER_DEFAULT_SEARCH, WebAdminApplicationPlaces.dataCenterMainPlace);
        searchToPlaceMap.put(DISKS_DEFAULT_SEARCH, WebAdminApplicationPlaces.diskMainPlace);
        searchToPlaceMap.put(EVENTS_DEFAULT_SEARCH, WebAdminApplicationPlaces.eventMainPlace);
        searchToPlaceMap.put(HOSTS_DEFAULT_SEARCH, WebAdminApplicationPlaces.hostMainPlace);
        searchToPlaceMap.put(NETWORK_DEFAULT_SEARCH, WebAdminApplicationPlaces.networkMainPlace);
        searchToPlaceMap.put(POOLS_DEFAULT_SEARCH, WebAdminApplicationPlaces.poolMainPlace);
        searchToPlaceMap.put(VNIC_PROFILE_DEFAULT_SEARCH, WebAdminApplicationPlaces.vnicProfileMainPlace);
        searchToPlaceMap.put(PROVIDER_DEFAULT_SEARCH, WebAdminApplicationPlaces.providerMainPlace);
        searchToPlaceMap.put(QUOTA_DEFAULT_SEARCH, WebAdminApplicationPlaces.quotaMainPlace);
        searchToPlaceMap.put(STORAGE_DEFAULT_SEARCH, WebAdminApplicationPlaces.storageMainPlace);
        searchToPlaceMap.put(TEMPLATE_DEFAULT_SEARCH, WebAdminApplicationPlaces.templateMainPlace);
        searchToPlaceMap.put(USERS_DEFAULT_SEARCH, WebAdminApplicationPlaces.userMainPlace);
        searchToPlaceMap.put(VMS_DEFAULT_SEARCH, WebAdminApplicationPlaces.virtualMachineMainPlace);
        searchToPlaceMap.put(VOLUMES_DEFAULT_SEARCH, WebAdminApplicationPlaces.volumeMainPlace);
    }
}
