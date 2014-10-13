package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class HostNetworkInterfacesPersisterImpl implements HostNetworkInterfacesPersister {

    private final InterfaceDao interfaceDao;
    private final List<VdsNetworkInterface> reportedNics;
    private final Map<String, VdsNetworkInterface> reportedNicsByNames;
    private final List<VdsNetworkInterface> dbNics;
    private List<VdsNetworkInterface> nicsForUpdate;
    private final Map<String, VdsNetworkInterface> userConfiguredNicsByName;

    public HostNetworkInterfacesPersisterImpl(InterfaceDao interfaceDao,
            List<VdsNetworkInterface> reportedNics,
            List<VdsNetworkInterface> dbNics,
            List<VdsNetworkInterface> userConfiguredNics) {
        this.interfaceDao = interfaceDao;
        this.reportedNics = reportedNics;
        this.reportedNicsByNames = Entities.entitiesByName(reportedNics);
        this.dbNics = dbNics;
        this.userConfiguredNicsByName = Entities.entitiesByName(userConfiguredNics);
    }

    @Override
    public void persistTopology() {
        removeUnreportedInterfaces();
        updateModifiedInterfaces();
        createNewInterfaces();
    }

    private void removeUnreportedInterfaces() {
        for (VdsNetworkInterface dbNic : dbNics) {
            if (!reportedNicsByNames.containsKey(dbNic.getName())) {
                interfaceDao.removeInterfaceFromVds(dbNic.getId());
                interfaceDao.removeStatisticsForVds(dbNic.getId());
            }
        }
    }

    private void updateModifiedInterfaces() {
        List<VdsNetworkInterface> nicsForUpdate = prepareNicsForUpdate();
        if (!nicsForUpdate.isEmpty()) {
            interfaceDao.massUpdateInterfacesForVds(getNicsForUpdate());
        }
    }

    private void createNewInterfaces() {
        List<VdsNetworkInterface> nicsForCreate = prepareNicsForCreate();
        for (VdsNetworkInterface nicForCreate : nicsForCreate) {
            interfaceDao.saveInterfaceForVds(nicForCreate);
            interfaceDao.saveStatisticsForVds(nicForCreate.getStatistics());
        }
    }

    private List<VdsNetworkInterface> prepareNicsForCreate() {
        List<VdsNetworkInterface> nicsForCreate = new ArrayList<>();
        Set<String> nicsNamesForUpdate = Entities.objectNames(getNicsForUpdate());
        for (VdsNetworkInterface reportedNic : reportedNics) {
            if (!nicsNamesForUpdate.contains(reportedNic.getName())) {
                overrideNicWithUserConfiguration(reportedNic, userConfiguredNicsByName);
                nicsForCreate.add(reportedNic);
            }
        }

        return nicsForCreate;
    }

    private List<VdsNetworkInterface> prepareNicsForUpdate() {
        List<VdsNetworkInterface> nicsForUpdate = new ArrayList<>();

        for (VdsNetworkInterface dbNic : dbNics) {
            if (reportedNicsByNames.containsKey(dbNic.getName())) {
                VdsNetworkInterface reportedNic = reportedNicsByNames.get(dbNic.getName());
                reportedNic.setId(dbNic.getId());
                if (!overrideNicWithUserConfiguration(reportedNic, userConfiguredNicsByName)) {
                    reportedNic.overrideEngineManagedAttributes(dbNic);
                }

                nicsForUpdate.add(reportedNic);
            }
        }

        return nicsForUpdate;
    }

    private boolean overrideNicWithUserConfiguration(VdsNetworkInterface nicForOverride,
            Map<String, VdsNetworkInterface> userConfiguredNicsByName) {
        if (userConfiguredNicsByName.containsKey(nicForOverride.getName())) {
            VdsNetworkInterface nic = userConfiguredNicsByName.get(nicForOverride.getName());
            nicForOverride.overrideEngineManagedAttributes(nic);
            return true;
        }

        return false;
    }

    private List<VdsNetworkInterface> getNicsForUpdate() {
        if (nicsForUpdate == null) {
            nicsForUpdate = prepareNicsForUpdate();
        }

        return nicsForUpdate;
    }
}
