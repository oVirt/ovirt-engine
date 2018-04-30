package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetStorageDomainDefaultWipeAfterDeleteParameters;
import org.ovirt.engine.core.utils.MockConfigDescriptor;

public class GetStorageDomainDefaultWipeAfterDeleteQueryTest
        extends AbstractQueryTest<GetStorageDomainDefaultWipeAfterDeleteParameters,
        GetStorageDomainDefaultWipeAfterDeleteQuery<GetStorageDomainDefaultWipeAfterDeleteParameters>> {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.concat(AbstractQueryTest.mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.SANWipeAfterDelete, true)));
    }

    @ParameterizedTest
    @EnumSource(value = StorageType.class, names = "UNKNOWN", mode = EnumSource.Mode.EXCLUDE)
    public void testExecuteQueryForType(StorageType type) {
        when(getQueryParameters().getStorageType()).thenReturn(type);
        getQuery().executeQueryCommand();

        assertEquals(type.isBlockDomain(), getQuery().getQueryReturnValue().getReturnValue(),
                "Wrong 'Wipe After Delete' value returned for Storage Domain type " + type);
    }
}
