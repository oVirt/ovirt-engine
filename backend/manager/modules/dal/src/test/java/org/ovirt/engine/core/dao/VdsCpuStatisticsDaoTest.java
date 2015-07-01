/**
 *
 */
package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.CpuStatistics;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 *
 */
public class VdsCpuStatisticsDaoTest extends BaseDaoTestCase {

    private static final Guid ANOTHER_EXISTING_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");

    private VdsCpuStatisticsDao vdsCpuStatisticsDao;
    private VdsStaticDao vdsStaticDao;
    private VdsStatic existingVds;
    private CpuStatistics newVdsCpuStatistics;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        vdsCpuStatisticsDao = dbFacade.getVdsCpuStatisticsDao();
        vdsStaticDao = dbFacade.getVdsStaticDao();
        existingVds = vdsStaticDao.get(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"));
        newVdsCpuStatistics = new CpuStatistics();
    }

    @Test
    public void testGetAllCpuStatisticsByVdsId() {
        List<CpuStatistics> result = vdsCpuStatisticsDao.getAllCpuStatisticsByVdsId(existingVds.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testMassSaveCpuStatistics() {
        List<CpuStatistics> result = vdsCpuStatisticsDao.getAllCpuStatisticsByVdsId(ANOTHER_EXISTING_VDS_ID);
        assertNotNull(result);
        assertEquals(0, result.size());

        List<CpuStatistics> newCpuStats = new ArrayList<>();
        newVdsCpuStatistics.setCpuId(0);
        newCpuStats.add(newVdsCpuStatistics);
        newVdsCpuStatistics.setCpuId(1);
        newCpuStats.add(newVdsCpuStatistics);
        newVdsCpuStatistics.setCpuId(2);
        newCpuStats.add(newVdsCpuStatistics);
        vdsCpuStatisticsDao.massSaveCpuStatistics(newCpuStats, ANOTHER_EXISTING_VDS_ID);
        result = vdsCpuStatisticsDao.getAllCpuStatisticsByVdsId(ANOTHER_EXISTING_VDS_ID);
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testMassUpdateCpuStatistics() {
        List<CpuStatistics> result = vdsCpuStatisticsDao.getAllCpuStatisticsByVdsId(existingVds.getId());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(20, result.get(0).getCpuUsagePercent());
        assertEquals(20, result.get(1).getCpuUsagePercent());

        result.get(0).setCpuUsagePercent(30);
        result.get(1).setCpuUsagePercent(30);
        vdsCpuStatisticsDao.massUpdateCpuStatistics(result, existingVds.getId());

        result = vdsCpuStatisticsDao.getAllCpuStatisticsByVdsId(existingVds.getId());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(30, result.get(0).getCpuUsagePercent());
        assertEquals(30, result.get(1).getCpuUsagePercent());
    }

    @Test
    public void testRemoveAllCpuStatisticsByVdsId() {
        List<CpuStatistics> newCpuStats = new ArrayList<>();
        newVdsCpuStatistics.setCpuId(0);
        newCpuStats.add(newVdsCpuStatistics);
        newVdsCpuStatistics.setCpuId(1);
        newCpuStats.add(newVdsCpuStatistics);
        newVdsCpuStatistics.setCpuId(2);
        newCpuStats.add(newVdsCpuStatistics);
        vdsCpuStatisticsDao.massSaveCpuStatistics(newCpuStats, ANOTHER_EXISTING_VDS_ID);

        List<CpuStatistics> result = vdsCpuStatisticsDao.getAllCpuStatisticsByVdsId(ANOTHER_EXISTING_VDS_ID);
        assertNotNull(result);
        assertEquals(3, result.size());

        vdsCpuStatisticsDao.removeAllCpuStatisticsByVdsId(ANOTHER_EXISTING_VDS_ID);

        result = vdsCpuStatisticsDao.getAllCpuStatisticsByVdsId(ANOTHER_EXISTING_VDS_ID);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

}
