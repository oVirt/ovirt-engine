package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendInstanceTypesResourceTest
    extends BackendTemplatesBasedResourceTest<InstanceType, org.ovirt.engine.core.common.businessentities.InstanceType, BackendInstanceTypesResource> {

    public BackendInstanceTypesResourceTest() {
        super(new BackendInstanceTypesResource(), SearchType.InstanceType, "Instancetypes : ");
    }

    public void testAdd() throws Exception {
        setUpGetGraphicsExpectations(1);
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
    public void testAddIncompleteParameters() {
        InstanceType model = new InstanceType();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "InstanceType", "add", "name");
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.InstanceType getEntity(int index) {
        return setUpEntityExpectations(mock(VmTemplate.class), index);
    }

    static org.ovirt.engine.core.common.businessentities.InstanceType setUpEntityExpectations(VmTemplate entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getNumOfCpus()).thenReturn(8);
        when(entity.getNumOfSockets()).thenReturn(2);
        when(entity.getThreadsPerCpu()).thenReturn(1);
        when(entity.getCpuPerSocket()).thenReturn(4);
        when(entity.isBaseTemplate()).thenReturn(true);
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
    protected void setUpGetEntityExpectations(int index) {
        setUpGetEntityExpectations(QueryType.GetInstanceType,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[index] },
                getEntity(index));
    }

    @Override
    protected void setUpCreationExpectations() {
        setUpCreationExpectations(ActionType.AddVmTemplate,
                AddVmTemplateParameters.class,
                new String[] { "Name", "Description" },
                new Object[] { NAMES[0], DESCRIPTIONS[0] },
                true,
                true,
                GUIDS[0],
                asList(GUIDS[2]),
                asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                QueryType.GetInstanceType,
                GetVmTemplateParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

}
