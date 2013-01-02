package org.ovirt.engine.core.bll.network.template;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.VmTemplateDAO;

/**
 * A test for the {@link GetVmTemplatesAndNetworkInterfacesByNetworkIdQuery} class. It tests the flow (i.e., that the query
 * delegates properly to the DAO}). The internal workings of the DAO are not tested.
 */
public class GetVmTemplatesAndNetworkInterfacesByNetworkIdQueryTest
extends AbstractQueryTest<NetworkIdParameters,
GetVmTemplatesAndNetworkInterfacesByNetworkIdQuery<NetworkIdParameters>> {

    private Guid networkId = Guid.NewGuid();
    private Guid vmTemplateId = Guid.NewGuid();
    private VmTemplate vmTemplate = new VmTemplate();
    private VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();

    @Test
    public void testExecuteQueryCommand() {
        // Setup the query parameters
        when(params.getNetworkId()).thenReturn(networkId);

        vmTemplate.setId(vmTemplateId);
        vmNetworkInterface.setVmTemplateId(vmTemplateId);

        // Setup the DAOs
        setupVmTemplateDao();
        setupVmNetworkInterfaceDao();

        PairQueryable<VmNetworkInterface, VmTemplate> vmInterfaceVmTemplatePair =
                new PairQueryable<VmNetworkInterface, VmTemplate>(vmNetworkInterface, vmTemplate);
        List<PairQueryable<VmNetworkInterface, VmTemplate>> expected =
                Collections.singletonList(vmInterfaceVmTemplatePair);

        // Run the query
        getQuery().executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    private void setupVmTemplateDao() {
        List<VmTemplate> expectedVmTemplate = Collections.singletonList(vmTemplate);
        VmTemplateDAO vmTemplateDao = mock(VmTemplateDAO.class);
        when(vmTemplateDao.getAllForNetwork(networkId)).thenReturn(expectedVmTemplate);
        when(getDbFacadeMockInstance().getVmTemplateDao()).thenReturn(vmTemplateDao);
    }

    private void setupVmNetworkInterfaceDao() {
        List<VmNetworkInterface> expectedVmNetworkInterface = Collections.singletonList(vmNetworkInterface);
        VmNetworkInterfaceDao vmNetworkInterfaceDaoMock = mock(VmNetworkInterfaceDao.class);
        when(vmNetworkInterfaceDaoMock.getAllForTemplatesByNetwork(networkId)).thenReturn(expectedVmNetworkInterface);
        when(getDbFacadeMockInstance().getVmNetworkInterfaceDao()).thenReturn(vmNetworkInterfaceDaoMock);
    }
}
