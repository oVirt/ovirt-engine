package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.dao.ClusterDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.GlusterVolumeEngineDao;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.StorageDomainDwhDao;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ClusterResourceAverage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.StorageDomainAverage;

public class HeatMapHelper {

    public static void getCpuAndMemory(HeatMapData utilization, DataSource dataSource) throws DashboardDataException {
        ClusterDwhDao dao = new ClusterDwhDao(dataSource);
        List<ClusterResourceAverage> averages = dao.getClusterCpuAndMemoryAverage();
        List<HeatMapBlock> cpu = new ArrayList<>();
        List<HeatMapBlock> memory = new ArrayList<>();
        for (ClusterResourceAverage data: averages) {
            cpu.add(new HeatMapBlock(data.getName(), data.getCpuAverage()));
            memory.add(new HeatMapBlock(data.getName(), data.getMemoryAverage()));
        }
        utilization.setCpu(cpu);
        utilization.setMemory(memory);
    }

    public static List<HeatMapBlock> getStorage(DataSource dwhDataSource) throws DashboardDataException {
        List<HeatMapBlock> nodes = new ArrayList<>();
        StorageDomainDwhDao dao = new StorageDomainDwhDao(dwhDataSource);
        for (StorageDomainAverage data: dao.getStorageAverage()) {
            nodes.add(new HeatMapBlock(data.getName(), data.getValue()));
        }
        return nodes;
    }

    public static List<HeatMapBlock> getVdoSavings(DataSource engineDataSource) throws DashboardDataException {
        List<HeatMapBlock> vols = new ArrayList<>();
        GlusterVolumeEngineDao dao = new GlusterVolumeEngineDao(engineDataSource);
        for (VDOVolumeDetails vdo : dao.getVdoVolumesSavingsList()) {
            vols.add(new HeatMapBlock(vdo.getVolumeName(), vdo.getVdoSavings()));
        }
        return vols;
    }
}
