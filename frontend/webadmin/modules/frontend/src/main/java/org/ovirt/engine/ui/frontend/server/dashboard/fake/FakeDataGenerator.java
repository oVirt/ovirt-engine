package org.ovirt.engine.ui.frontend.server.dashboard.fake;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.ovirt.engine.ui.frontend.server.dashboard.GlobalUtilization;
import org.ovirt.engine.ui.frontend.server.dashboard.GlobalUtilizationCpuSummary;
import org.ovirt.engine.ui.frontend.server.dashboard.GlobalUtilizationResourceSummary;
import org.ovirt.engine.ui.frontend.server.dashboard.HeatMapBlock;
import org.ovirt.engine.ui.frontend.server.dashboard.HeatMapData;
import org.ovirt.engine.ui.frontend.server.dashboard.HistoryNode;
import org.ovirt.engine.ui.frontend.server.dashboard.Inventory;
import org.ovirt.engine.ui.frontend.server.dashboard.InventoryStatus;
import org.ovirt.engine.ui.frontend.server.dashboard.ResourceUtilization;
import org.ovirt.engine.ui.frontend.server.dashboard.StorageUtilization;
import org.ovirt.engine.ui.frontend.server.dashboard.Utilization;
import org.ovirt.engine.ui.frontend.server.dashboard.UtilizedEntity;
import org.ovirt.engine.ui.frontend.server.dashboard.UtilizedEntity.Trend;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.VmStatusMap;

public class FakeDataGenerator {
    public static Inventory fakeInventory(Random random) {
        Inventory result = new Inventory();

        result.setCluster(new InventoryStatus());
        result.getCluster().setTotalCount(25);

        result.setDc(new InventoryStatus());
        createFakeValuesUpDownError(result.getDc(), 22, random);

        result.setHost(new InventoryStatus());
        createFakeValuesUpDownError(result.getHost(), 125, random);

        result.setStorage(new InventoryStatus());
        createFakeValuesUpDownError(result.getStorage(), 10, random);

        result.setVm(new InventoryStatus());
        createFakeValuesUpDownError(result.getVm(), 253, random);

        result.setEvent(new InventoryStatus());
        createFakeValuesAlertErrorWarning(result.getEvent(), 169, random);

        result.setVolume(new InventoryStatus());
        createFakeValuesUpDownError(result.getVolume(), 95, random);
        return result;
    }

    private static void createFakeValuesUpDownError(InventoryStatus inventory, int total, Random random) {
        inventory.setTotalCount(total);
        int count = total;
        int randomValue = random.nextInt(count);
        count -= randomValue;
        inventory.setStatusCount(VmStatusMap.WARNING.name().toLowerCase(), count);
        randomValue = random.nextInt(count);
        count -= randomValue;
        inventory.setStatusCount(VmStatusMap.DOWN.name().toLowerCase(), count);
        randomValue = random.nextInt(count);
        count -= randomValue;
        inventory.setStatusCount(VmStatusMap.UP.name().toLowerCase(), count);
    }

    private static void createFakeValuesAlertErrorWarning(InventoryStatus inventory, int total, Random random) {
        inventory.setTotalCount(total);
        int count = total;
        int randomValue = random.nextInt(count);
        count -= randomValue;
        inventory.setStatusCount("error", count); //$NON-NLS-1$
        randomValue = random.nextInt(count);
        count -= randomValue;
        inventory.setStatusCount("alert", count); //$NON-NLS-1$
        randomValue = random.nextInt(count);
        count -= randomValue;
        inventory.setStatusCount("warning", count); //$NON-NLS-1$
    }

    public static GlobalUtilization fakeGlobalUtilization(Random random) {
        GlobalUtilization utilization = getFakeMemAndCpuAndStorageUtilization(random);
        return utilization;
    }

    public static HeatMapData fakeHeatMapData(Random random) {
        HeatMapData utilization = new HeatMapData();

        List<HeatMapBlock> cpuNodes = new ArrayList<>();
        List<HeatMapBlock> memNodes = new ArrayList<>();
        List<HeatMapBlock> storageNodes = new ArrayList<>();

        int clusterCount = random.nextInt(30) + 1;
        for (int i = 0; i < clusterCount; i++) {
            cpuNodes.add(new HeatMapBlock("fake_cluster_" + i, random.nextDouble() * 100)); //$NON-NLS-1$
        }
        for (int i = 0; i < clusterCount; i++) {
            memNodes.add(new HeatMapBlock("fake_cluster_" + i, random.nextDouble() * 100)); //$NON-NLS-1$
        }
        for (int i = 0; i < random.nextInt(30) + 1; i++) {
            storageNodes.add(new HeatMapBlock("fake_storage_node_" + i, random.nextDouble() * 100)); //$NON-NLS-1$
        }

        utilization.setCpu(cpuNodes);
        utilization.setMemory(memNodes);
        utilization.setStorage(storageNodes);
        return utilization;
    }

    public static GlobalUtilization getFakeMemAndCpuAndStorageUtilization(Random random) {
        GlobalUtilization utilization = new GlobalUtilization();
        utilization.setCpu(new GlobalUtilizationCpuSummary());
        populateUtilizationObject(utilization.getCpu(), random);
        utilization.setMemory(new GlobalUtilizationResourceSummary());
        populateUtilizationObject(utilization.getMemory(), random);
        utilization.setStorage(new GlobalUtilizationResourceSummary(new StorageUtilization()));
        populateUtilizationObject(utilization.getStorage(), random);
        return utilization;
    }

    private static GlobalUtilizationResourceSummary populateUtilizationObject(
            GlobalUtilizationResourceSummary utilizationEntity, Random random) {
        long now = new Date().getTime();
        List<HistoryNode> cpuHistory = new ArrayList<>();
        for (long i = 0; i < 24; i++) {
            cpuHistory.add(new HistoryNode(now - ((24L - i) * 3600000L), random.nextDouble() * 100));
        }
        utilizationEntity.setHistory(cpuHistory);
        utilizationEntity.setPhysicalTotal(random.nextInt(100));
        int total = random.nextInt(1000);
        utilizationEntity.setVirtualTotal(total);
        utilizationEntity.setVirtualUsed(random.nextInt(total));
        utilizationEntity.setUsed(random.nextDouble() * utilizationEntity.getTotal());
        createFakeUtilization(utilizationEntity.getUtilization(), random);
        return utilizationEntity;
    }

    private static void createFakeUtilization(Utilization utilization, Random random) {
        int sendValues = random.nextInt(10);
        if (sendValues > 1) {
            for(int i = 0; i < random.nextInt(10) + 1; i++) {
                UtilizedEntity host = new UtilizedEntity();
                if (utilization instanceof ResourceUtilization) {
                    host.setName("fake_host_" + i); //$NON-NLS-1$
                } else {
                    host.setName("fake_storage_domain_" + i); //$NON-NLS-1$
                }
                host.setTotal(random.nextDouble() * 100 + 1);
                host.setUsed((double) random.nextInt(host.getTotal().intValue()));
                host.setTrend(Trend.values()[random.nextInt(3)]);
                utilization.addResource(host);
            }
        }
        sendValues = random.nextInt(10);
        if (sendValues > 1) {
            for(int i = 0; i < random.nextInt(10) + 1; i++) {
                UtilizedEntity vm = new UtilizedEntity();
                vm.setName("fake_vm" + i); //$NON-NLS-1$
                vm.setTotal(random.nextDouble() * 100 + 1);
                vm.setUsed((double) random.nextInt(vm.getTotal().intValue()));
                vm.setTrend(Trend.values()[random.nextInt(3)]);
                utilization.addVm(vm);
            }
        }
    }
}
