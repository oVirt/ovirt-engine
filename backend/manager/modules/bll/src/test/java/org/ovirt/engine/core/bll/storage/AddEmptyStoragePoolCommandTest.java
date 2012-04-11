package org.ovirt.engine.core.bll.storage;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.QuotaHelper;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link AddEmptyStoragePoolCommand} class. */
@RunWith(Parameterized.class)
public class AddEmptyStoragePoolCommandTest {

    /** The command to test */
    private AddEmptyStoragePoolCommand<StoragePoolManagementParameter> command;

    /** The command's parameters */
    private StoragePoolManagementParameter parameters;

    /** The storage pool to create */
    private storage_pool poolToAdd;

    /** The enforcement type to check */
    private QuotaEnforcementTypeEnum type;

    /** The {@link QuotaHelper} mock used by the command */
    @Mock
    private QuotaHelper quotaHelperMock;

    /** The {@link StoragePoolDAO} mock used by the command */
    @Mock
    private StoragePoolDAO storagePoolDAOMock;

    public AddEmptyStoragePoolCommandTest(QuotaEnforcementTypeEnum type) {
        this.type = type;
    }

    @Before
    public void setUp() {
        initMocks(this);
        setUpPoolToAdd();
        setUpCommandWithMocks();
    }

    @Parameters
    public static Collection<Object[]> data() {
        QuotaEnforcementTypeEnum[] enforcementTypes = QuotaEnforcementTypeEnum.values();

        List<Object[]> data = new ArrayList<Object[]>(enforcementTypes.length);

        for (QuotaEnforcementTypeEnum enforcementType : enforcementTypes) {
            data.add(new Object[] { enforcementType });
        }

        return data;
    }

    protected void setUpPoolToAdd() {
        poolToAdd = new storage_pool();
        poolToAdd.setname(RandomUtils.instance().nextString(10));
        poolToAdd.setId(Guid.NewGuid());
        poolToAdd.setQuotaEnforcementType(type);
    }

    protected void setUpCommandWithMocks() {
        parameters = new StoragePoolManagementParameter(poolToAdd);

        command = spy(new AddEmptyStoragePoolCommand<StoragePoolManagementParameter>(parameters));
        doReturn(quotaHelperMock).when(command).getQuotaHelper();
        doReturn(storagePoolDAOMock).when(command).getStoragePoolDAO();
    }

    @Test
    public void testAddStoragePoolToDbDefaultQuota() {
        Quota unlimited = new Quota();
        when(quotaHelperMock.getUnlimitedQuota(poolToAdd, true)).thenReturn(unlimited);
        command.AddStoragePoolToDb();

        verify(quotaHelperMock).saveQuotaForUser(unlimited, Guid.EVERYONE);
    }
}
