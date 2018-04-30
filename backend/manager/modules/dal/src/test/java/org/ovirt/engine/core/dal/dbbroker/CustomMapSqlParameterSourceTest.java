package org.ovirt.engine.core.dal.dbbroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

/** A test case for the {@link CustomMapSqlParameterSource} */
public class CustomMapSqlParameterSourceTest {

    private Random random;

    /** The {@link CustomMapSqlParameterSourceTest} to be tested */
    private CustomMapSqlParameterSource paramSource;

    /** Name of the parameter to pass */
    private String paramName;

    public static enum TestEnum {
        ONE,
        TWO,
        THREE
    }

    public static enum TestEnumWithGetValue {
        ONE,
        TWO,
        THREE;

        public int getValue() {
            return ordinal() * -1;
        }
    }

    @BeforeEach
    public void setUp() {
        DbEngineDialect dialectMock = mock(DbEngineDialect.class);
        when(dialectMock.getParamNamePrefix()).thenReturn("");
        paramSource = new CustomMapSqlParameterSource(dialectMock);

        paramName = RandomStringUtils.randomAlphabetic(10);

        this.random = new Random();
    }

    @Test
    public void testAddValuePrimitive() {
        int paramValue = this.random.nextInt();

        paramSource.addValue(paramName, paramValue);
        assertEquals(
                paramValue,
                paramSource.getValue(paramName), "wrong value returned from parameter source");
    }

    @Test
    public void testAddValueEnum() {
        paramSource.addValue(paramName, TestEnum.TWO);
        assertEquals(
                TestEnum.TWO.ordinal(),
                paramSource.getValue(paramName), "wrong value returned from parameter source");
    }

    @Test
    public void testAddValueEnumWithGetValue() {
        paramSource.addValue(paramName, TestEnumWithGetValue.TWO);
        assertEquals(
                TestEnumWithGetValue.TWO.getValue(),
                paramSource.getValue(paramName), "wrong value returned from parameter source");
    }

    @Test
    public void testAddValueGuid() {
        Guid guid = new Guid(UUID.randomUUID());

        paramSource.addValue(paramName, guid);
        assertEquals(
                guid.getUuid(),
                paramSource.getValue(paramName), "wrong value returned from parameter source");
    }

    @Test
    public void testAddValueVersion() {
        Version verision = new Version(this.random.nextInt(), this.random.nextInt());

        paramSource.addValue(paramName, verision);
        assertEquals(
                verision.toString(),
                paramSource.getValue(paramName), "wrong value returned from parameter source");
    }

    @Test
    public void testAddValueNull() {
        paramSource.addValue(paramName, null);
        assertNull(
                paramSource.getValue(paramName), "wrong value returned from parameter source");
    }
}
