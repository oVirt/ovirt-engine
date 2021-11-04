package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VdcOption;

public class VdcOptionDaoTest extends BaseDaoTestCase<VdcOptionDao> {
    private static final int INVALID_ID = -1;
    private static final int OPTION_COUNT = 6;
    private VdcOption existingOption;
    private VdcOption newOption;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingOption = dao.getByNameAndVersion("UserDefinedVmPropertiesKey1", "general");

        newOption = new VdcOption();
        newOption.setOptionName("option_name");
        newOption.setOptionValue("option_value");
        newOption.setOptionDefaultValue("default_value");
        newOption.setVersion("general");
    }

    /**
     * Ensures the ID must be valid.
     */
    @Test
    public void testGetWithInvalidId() {
        VdcOption result = dao.get(INVALID_ID);

        assertNull(result);
    }

    /**
     * Ensures retrieving an option works as expected.
     */
    @Test
    public void testGet() {
        VdcOption result = dao.get(existingOption.getOptionId());

        assertNotNull(result);
        assertEquals(existingOption, result);
    }

    /**
     * Ensures the name must be valid.
     */
    @Test
    public void testGetByNameAndVersionWithInvalidName() {
        VdcOption result = dao.getByNameAndVersion("farkle", existingOption.getVersion());

        assertNull(result);
    }

    /**
     * Ensures the version must be valid.
     */
    @Test
    public void testGetByNameAndVersionWithInvalidVersion() {
        VdcOption result = dao.getByNameAndVersion(existingOption.getOptionName(), "farkle");

        assertNull(result);
    }

    /**
     * Ensures retrieving an option by name and version works.
     */
    @Test
    public void testGetBynameAndVersion() {
        VdcOption result = dao.getByNameAndVersion(existingOption.getOptionName(), existingOption.getVersion());

        assertNotNull(result);
        assertEquals(existingOption, result);
    }

    /**
     * Ensures that all options are returned.
     */
    @Test
    public void testGetAll() {
        List<VdcOption> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(OPTION_COUNT, result.size());
    }

    /**
     * Ensure saving an option works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newOption);

        VdcOption result = dao.getByNameAndVersion(newOption.getOptionName(), newOption.getVersion());

        assertNotNull(result);
        assertEquals(newOption, result);
    }

    /**
     * Ensures updating an option works as expected.
     */
    @Test
    public void testUpdate() {
        existingOption.setOptionValue("this is a new value");

        dao.update(existingOption);

        VdcOption result = dao.get(existingOption.getOptionId());

        assertEquals(existingOption, result);
    }

    /**
     * Ensures removing an option works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingOption.getOptionId());

        VdcOption result = dao.get(existingOption.getOptionId());

        assertNull(result);
    }
}
