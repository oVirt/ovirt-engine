package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.vdscommands.UserOverriddenNicValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class HostNetworkInterfacesPersisterImpl implements HostNetworkInterfacesPersister {

    private final InterfaceDao interfaceDao;
    private final List<VdsNetworkInterface> reportedNics;
    private final Map<String, VdsNetworkInterface> reportedNicsByNames;
    private final List<VdsNetworkInterface> dbNics;
    private List<VdsNetworkInterface> nicsForUpdate;
    private final Map<String, UserOverriddenNicValues> userOverriddenNicValuesByNicName;

    public HostNetworkInterfacesPersisterImpl(InterfaceDao interfaceDao,
            List<VdsNetworkInterface> reportedNics,
            List<VdsNetworkInterface> dbNics,
            Map<String, UserOverriddenNicValues> userOverriddenNicValuesByNicName) {
        this.interfaceDao = interfaceDao;
        this.reportedNics = reportedNics;
        this.reportedNicsByNames = Entities.entitiesByName(reportedNics);
        this.dbNics = dbNics;
        this.userOverriddenNicValuesByNicName = userOverriddenNicValuesByNicName;
    }

    @Override
    public void persistTopology() {
        removeUnreportedInterfaces();
        updateModifiedInterfaces();
        createNewInterfaces();
    }

    private void removeUnreportedInterfaces() {
        for (VdsNetworkInterface dbNic : dbNics) {
            if (nicShouldBeRemoved(dbNic.getName())) {
                interfaceDao.removeInterfaceFromVds(dbNic.getId());
                interfaceDao.removeStatisticsForVds(dbNic.getId());
            }
        }
    }

    private boolean nicShouldBeRemoved(String nicName) {
        return !reportedNicsByNames.containsKey(nicName);
    }

    private void updateModifiedInterfaces() {
        List<VdsNetworkInterface> nicsForUpdate = getNicsForUpdate();
        List<Guid> updateNicsIds = nicsForUpdate.stream().map(VdsNetworkInterface::getId).collect(Collectors.toList());

        if (!nicsForUpdate.isEmpty()) {
            interfaceDao.massClearNetworkFromNics(updateNicsIds);
            interfaceDao.massUpdateInterfacesForVds(nicsForUpdate);
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
        // nics for update contains (altered) subset of reportedNics, those related to existing dbNics, so if some
        // reported nic is not present there, it must be reported nic to create.
        List<VdsNetworkInterface> nicsForUpdate = getNicsForUpdate();

        Set<String> nicsNamesForUpdate =
                nicsForUpdate.stream().map(VdsNetworkInterface::getName).collect(Collectors.toSet());
        for (VdsNetworkInterface reportedNic : reportedNics) {
            String nicName = reportedNic.getName();
            if (!nicsNamesForUpdate.contains(nicName)) {
                reportedNic.overrideEngineManagedAttributes(userOverriddenNicValuesByNicName.get(nicName));
                nicsForCreate.add(reportedNic);
            }
        }

        return nicsForCreate;
    }

    /**
     * @return subset of reportedNics, only nics with corresponding db record(dbNic) are returned. Each such reported
     * nic is altered: id is overridden to one of corresponding db record
     */
    private List<VdsNetworkInterface> prepareNicsForUpdate() {
        return dbNics.stream()
                .filter(dbNic -> reportedNicsByNames.containsKey(dbNic.getName()))
                .map(this::mapDbNicToNicForUpdate)
                .collect(toList());
    }

    /**
     * @param dbNic dbNic used to find reportedNic.
     * @return reportedNic of same name as dbNic has, with id taken from dbNic, and with user configuration taken
     * from userOverriddenNicValuesByNicName if it exist or from db nic.
     */
    private VdsNetworkInterface mapDbNicToNicForUpdate(VdsNetworkInterface dbNic) {
        String nicName = dbNic.getName();

        VdsNetworkInterface reportedNic = reportedNicsByNames.get(nicName);
        boolean hasUserOverridingValues = userOverriddenNicValuesByNicName.containsKey(nicName);

        reportedNic.setId(dbNic.getId());

        if (hasUserOverridingValues) {
            reportedNic.overrideEngineManagedAttributes(userOverriddenNicValuesByNicName.get(nicName));
        } else {
            reportedNic.overrideEngineManagedAttributes(dbNic);
        }

        return reportedNic;
    }

    private List<VdsNetworkInterface> getNicsForUpdate() {
        if (nicsForUpdate == null) {
            nicsForUpdate = prepareNicsForUpdate();
        }

        return nicsForUpdate;
    }
}
