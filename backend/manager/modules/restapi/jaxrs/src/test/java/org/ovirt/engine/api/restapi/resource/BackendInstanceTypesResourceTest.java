package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendInstanceTypesResourceTest
    extends BackendTemplatesBasedResourceTest<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType, BackendInstanceTypesResource> {

    public BackendInstanceTypesResourceTest() {
        super(new BackendInstanceTypesResource(), SearchType.InstanceType, "Instancetypes : ");
    }

    public void testAdd() throws Exception {
        setUpGetGraphicsExpectations(1);
        setUpGetBallooningExpectations(0, 0);
        setUpGetConsoleExpectations(0, 0);

        super.testAdd();
    }

    @Override
    protected Response doAdd(InstanceType model) {
        return collection.add(model);
    }

    @Override
    protected InstanceType getRestModel(int index) {
        return getModel(index);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        InstanceType model = new InstanceType();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "InstanceType", "add", "name");
        }
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.InstanceType getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmTemplate.class), index);
    }

    static org.ovirt.engine.core.common.businessentities.InstanceType setUpEntityExpectations(VmTemplate entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getNumOfCpus()).andReturn(8).anyTimes();
        expect(entity.getNumOfSockets()).andReturn(2).anyTimes();
        expect(entity.getThreadsPerCpu()).andReturn(1).anyTimes();
        expect(entity.getCpuPerSocket()).andReturn(4).anyTimes();
        expect(entity.isBaseTemplate()).andReturn(true).anyTimes();
        return entity;
    }

    static InstanceType getModel(int index) {
        InstanceType model = new InstanceType();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    @Override
    protected List<InstanceType> getCollection() {
        return collection.list().getInstanceTypes();
    }

    @Override
    protected void verifyModel(InstanceType model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model);
    }

    static void verifyModelSpecific(InstanceType model) {
        assertNotNull(model.getCpu());
        assertNotNull(model.getCpu().getTopology());
        assertEquals(4, model.getCpu().getTopology().getCores().intValue());
        assertEquals(2, model.getCpu().getTopology().getSockets().intValue());
        assertEquals(1, model.getCpu().getTopology().getThreads().intValue());
    }

    @Override
    public void testQuery() throws Exception {
        setUpGetGraphicsExpectations(3);
        super.testQuery();
    }

    @Override
    public void testList() throws Exception {
        setUpGetGraphicsExpectations(3);
        super.testList();
    }

    @Override
    protected void setUpGetEntityExpectations(int index) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetInstanceType,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[index] },
                getEntity(index));
    }

    @Override
    protected void setUpCreationExpectations() {
        setUpCreationExpectations(VdcActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[] { "Name", "Description" },
                new Object[] { NAMES[0], DESCRIPTIONS[0] },
                true,
                true,
                GUIDS[0],
                asList(GUIDS[2]),
                asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                VdcQueryType.GetInstanceType,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

}
