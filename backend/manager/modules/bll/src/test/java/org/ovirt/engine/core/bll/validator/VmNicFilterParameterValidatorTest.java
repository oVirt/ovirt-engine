package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class VmNicFilterParameterValidatorTest {

    private final Guid unUsedId = Guid.newGuid();
    private final Guid usedId = Guid.newGuid();

    static final String VAR_INTERFACE_ID = "INTERFACE_ID";
    static final String VAR_VM_ID = "VM_ID";
    static final String VAR_FILTER_ID = "FILTER_ID";

    @Mock
    private VmNicFilterParameterDao vmNicFilterParameterDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private VmDao vmDao;

    private VmNicFilterParameterValidator validator;

    @Before
    public void setUp() {
        validator = new VmNicFilterParameterValidator(vmNicFilterParameterDao, vmNicDao);
        VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();
        vmNetworkInterface.setVmId(usedId);
        when(vmNicDao.get(usedId)).thenReturn(vmNetworkInterface);
        when(vmNicFilterParameterDao.get(usedId)).thenReturn(new VmNicFilterParameter());
        when(vmDao.get(usedId)).thenReturn(new VM());
    }

    @Test
    public void testIdIsInvalid() {
        assertThat(validator.parameterHavingIdExists(unUsedId),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NIC_FILTER_PARAMETER_ID_NOT_EXISTS,
                        ReplacementUtils.createSetVariableString(VAR_FILTER_ID, unUsedId)));
    }

    @Test
    public void testIdIsValid() {
        assertThat(validator.parameterHavingIdExists(usedId), isValid());
    }

    @Test
    public void testVmInterfaceIdIsInvalid() {
        assertThat(validator.vmInterfaceHavingIdExists(unUsedId),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE,
                        ReplacementUtils.createSetVariableString(VAR_INTERFACE_ID, unUsedId)));
    }

    @Test
    public void testVmInterfaceIdIsValid() {
        assertThat(validator.vmInterfaceHavingIdExists(usedId), isValid());
    }

    @Test
    public void testInterfaceExistsOnVm() {
        assertThat(validator.vmInterfaceHavingIdExistsOnVmHavingId(usedId, usedId), isValid());
    }

    @Test
    public void testInterfaceExistsNotOnVm() {
        assertThat(validator.vmInterfaceHavingIdExistsOnVmHavingId(usedId, unUsedId),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE_VM,
                ReplacementUtils.createSetVariableString(VAR_INTERFACE_ID, usedId),
                ReplacementUtils.createSetVariableString(VAR_VM_ID, unUsedId)));
    }
}
