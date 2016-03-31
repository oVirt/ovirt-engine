package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetStorageDomainDefaultWipeAfterDeleteParameters;
import org.ovirt.engine.core.common.utils.MockConfigRule;

@RunWith(Theories.class)
public class GetStorageDomainDefaultWipeAfterDeleteQueryTest
        extends AbstractQueryTest<GetStorageDomainDefaultWipeAfterDeleteParameters,
        GetStorageDomainDefaultWipeAfterDeleteQuery<GetStorageDomainDefaultWipeAfterDeleteParameters>> {

    @DataPoints
    public static StorageType[] types = StorageType.values();

    @Override
    public Set<MockConfigRule.MockConfigDescriptor<Boolean>> getExtraConfigDescriptors() {
        return Collections.singleton(MockConfigRule.mockConfig(ConfigValues.SANWipeAfterDelete, true));
    }

    @Theory
    public void testExecuteQueryForType(StorageType type) {
        assumeTrue(type + " is not a concrete type, skipping", type.isConcreteStorageType());

        when(getQueryParameters().getStorageType()).thenReturn(type);
        getQuery().executeQueryCommand();

        assertEquals("Wrong 'Wipe After Delete' value returned for Storage Domain type " + type,
                type.isBlockDomain(), getQuery().getQueryReturnValue().getReturnValue());
    }
}
