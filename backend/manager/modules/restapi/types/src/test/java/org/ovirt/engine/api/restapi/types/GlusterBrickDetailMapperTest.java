package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterBrickAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.core.common.businessentities.gluster.MemoryStatus;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;
import org.ovirt.engine.core.utils.RandomUtils;

public class GlusterBrickDetailMapperTest extends AbstractInvertibleMappingTest<GlusterBrick, GlusterVolumeAdvancedDetails, GlusterVolumeAdvancedDetails> {

     public GlusterBrickDetailMapperTest() {
        super(GlusterBrick.class, GlusterVolumeAdvancedDetails.class,
                GlusterVolumeAdvancedDetails.class);
    }

    @Override
    protected void verify(GlusterBrick model, GlusterBrick transform) {
       assertNotNull(transform);
    }

    @Test
    public void testWithAllDetails() {
        GlusterVolumeAdvancedDetails volDetailsEntity = new GlusterVolumeAdvancedDetails();
        volDetailsEntity.setBrickDetails(getBrickDetails(1, 2, 4));
        GlusterBrickAdvancedDetails model = GlusterBrickDetailMapper.map(volDetailsEntity, null);
        assertNotNull(model);
        assertEquals(2, model.getGlusterClients().getGlusterClients().size());
        assertEquals(model.getMntOptions(), volDetailsEntity.getBrickDetails().get(0).getBrickProperties().getMntOptions());
        assertEquals(model.getMemoryPools().getGlusterMemoryPools().size(), volDetailsEntity.getBrickDetails().get(0).getMemoryStatus().getMemPools().size());

    }

    @Test
    public void testWithNoLists() {
        GlusterVolumeAdvancedDetails volDetailsEntity = new GlusterVolumeAdvancedDetails();
        volDetailsEntity.setBrickDetails(getBrickDetails(0, 0, 0));
        GlusterBrickAdvancedDetails model = GlusterBrickDetailMapper.map(volDetailsEntity, null);
        assertNotNull(model);
        assertNull(model.getGlusterClients());
        assertNull(model.getMntOptions());
    }

    @Test
    public void testWithMultipleClientLists() {
        GlusterVolumeAdvancedDetails volDetailsEntity = new GlusterVolumeAdvancedDetails();
        volDetailsEntity.setBrickDetails(getBrickDetails(2, 2, 2));

        GlusterBrickAdvancedDetails model = GlusterBrickDetailMapper.map(volDetailsEntity, null);
        assertEquals(2, model.getGlusterClients().getGlusterClients().size());
        assertEquals(model.getMntOptions(), volDetailsEntity.getBrickDetails().get(0).getBrickProperties().getMntOptions());
        assertEquals(model.getMemoryPools().getGlusterMemoryPools().size(), volDetailsEntity.getBrickDetails().get(0).getMemoryStatus().getMemPools().size());

    }

    @Test
    public void testWithNullChildObjects() {
        GlusterVolumeAdvancedDetails volDetailsEntity = new GlusterVolumeAdvancedDetails();
        volDetailsEntity.setBrickDetails(getBrickDetails(1, 0, 0));
        volDetailsEntity.getBrickDetails().get(0).setClients(null);
        volDetailsEntity.getBrickDetails().get(0).setMemoryStatus(null);

        GlusterBrickAdvancedDetails model = GlusterBrickDetailMapper.map(volDetailsEntity, null);
        assertNotNull(model);
        assertNull(model.getGlusterClients());
        assertEquals(model.getMntOptions(), volDetailsEntity.getBrickDetails().get(0).getBrickProperties().getMntOptions());
        assertNull(model.getMemoryPools());
    }

    private List<BrickDetails> getBrickDetails(int size, int clientListSize, int memPoolSize) {
       ArrayList<BrickDetails> list = new ArrayList<>();
       for(int i=0; i < size; i++) {
           BrickDetails details = new BrickDetails();
           BrickProperties props = new BrickProperties();
           props.setBlockSize(14556);
           props.setPid(88888);
           props.setMntOptions("rw");
           details.setBrickProperties(props);
           details.setClients(getClientList(clientListSize));
           details.setMemoryStatus(getMemoryStatus(memPoolSize));
           list.add(details);
       }
       return list;
    }

    private MemoryStatus getMemoryStatus(int listSize) {
        MemoryStatus memStatus = new MemoryStatus();
        memStatus.setMallInfo(new MallInfo());

        memStatus.getMallInfo().setArena(RandomUtils.instance().nextInt());
        memStatus.getMallInfo().setUordblks(RandomUtils.instance().nextInt());
        ArrayList<Mempool> memPoolsList = new ArrayList<>();
        for(int i=0; i < listSize; i++) {
            Mempool pool = new Mempool();
            pool.setAllocCount(RandomUtils.instance().nextInt());
            pool.setHotCount(0);
            pool.setName(RandomUtils.instance().nextString(5));
            memPoolsList.add(pool);
        }
        memStatus.setMemPools(memPoolsList);
        return memStatus;
    }

    private List<GlusterClientInfo> getClientList(int listSize) {
        ArrayList<GlusterClientInfo> list = new ArrayList<>();
        for(int i=0; i < listSize; i++) {
            GlusterClientInfo clientInfo = new GlusterClientInfo();
            clientInfo.setBytesRead(RandomUtils.instance().nextLong());
            clientInfo.setBytesWritten(RandomUtils.instance().nextLong());
            clientInfo.setClientPort(RandomUtils.instance().nextInt());
            clientInfo.setHostname(RandomUtils.instance().nextString(7));
            list.add(clientInfo);
        }
        return list;
    }

}
