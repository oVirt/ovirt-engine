package org.ovirt.engine.ui.frontend.server.dashboard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ovirt.engine.ui.frontend.server.dashboard.dao.ClusterDwhDAO;
import org.ovirt.engine.ui.frontend.server.dashboard.dao.StorageDomainDwhDAO;
import org.ovirt.engine.ui.frontend.server.dashboard.models.ClusterResourceAverage;
import org.ovirt.engine.ui.frontend.server.dashboard.models.StorageDomainAverage;

public class HeatMapHelper {


    public static void getCpuAndMemory(HeatMapData utilization, DataSource dataSource) throws SQLException,
            DashboardDataException {
        ClusterDwhDAO dao = new ClusterDwhDAO(dataSource);
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

    public static List<HeatMapBlock> getStorage(DataSource dwhDataSource) throws SQLException, DashboardDataException {
        List<HeatMapBlock> nodes = new ArrayList<>();
        StorageDomainDwhDAO dao = new StorageDomainDwhDAO(dwhDataSource);
        for (StorageDomainAverage data: dao.getStorageAverage()) {
            nodes.add(new HeatMapBlock(data.getName(), data.getValue()));
        }
        return nodes;
    }
}
