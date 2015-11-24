package org.ovirt.engine.core.bll.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.migration.ConvergenceConfig;
import org.ovirt.engine.core.common.migration.ConvergenceItem;
import org.ovirt.engine.core.common.migration.ConvergenceItemWithStallingLimit;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.JsonHelper;

public class ConvergenceConfigProviderTest {

    private ConvergenceConfigProvider provider = new ConvergenceConfigProvider();

    @Test
    public void jsonInvalidJson() throws IOException {
        provider.initMigrationPolicies("this is not a valid json");
        MigrationPolicy policy = provider.getMigrationPolicy(Guid.newGuid());
        assertTrue(policy instanceof NoMigrationPolicy);
    }

    @Test
    public void jsonProperlyFilled() throws IOException {
        Guid id = Guid.newGuid();

        List<MigrationPolicy> policies = new ArrayList<>();

        ConvergenceConfig config = new ConvergenceConfig();
        config.setInitialItems(Arrays.asList(new ConvergenceItem("setDowntime2", 1)));
        config.setLastItems(Arrays.asList(new ConvergenceItem("setDowntime3", 15)));
        config.setConvergenceItems(Arrays.asList(new ConvergenceItemWithStallingLimit(1, "someAction", 12)));

        MigrationPolicy policy = new MigrationPolicy(id, "some name", "some long description", config);
        policies.add(policy);

        String json = JsonHelper.objectToJson(policies, false);

        provider.initMigrationPolicies(json);

        // just basic sanity check
        assertEquals(policy.getName(), provider.getMigrationPolicy(id).getName());
        assertEquals(policy.getDescription(), provider.getMigrationPolicy(id).getDescription());
    }
}

