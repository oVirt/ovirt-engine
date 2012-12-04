package org.ovirt.engine.core.utils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ovirt.engine.core.compat.LongCompat;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * A rule to improve the work with {@link RandomUtils} in unit tests.
 *
 * First, it logs the random seed used in the test.
 * Second, it allows setting it externally by setting the environment variable test.random.seed.
 * E.g., you could run:
 * <code>
 * mvn clean install -Dtest.random.seed=123
 * </code>
 */
public class RandomUtilsSeedingRule extends TestWatcher {
    private static final String RANDOM_SEED_PROPERTY = "test.random.seed";

    @Override
    public void starting(Description description) {
        String seedProperty = System.getProperty(RANDOM_SEED_PROPERTY);
        Long seed = LongCompat.tryParse(seedProperty);
        if (seed == null) {
            log.infoFormat
                    ("Property \"{0}\" was not set, using System.currentTimeMillis() as a seed.",
                            RANDOM_SEED_PROPERTY);
            seed = System.currentTimeMillis();
        }
        RandomUtils.instance().setSeed(seed);
        log.info("Running test with random seed: " + RandomUtils.instance().getSeed());
    }

    private static final Log log = LogFactory.getLog(RandomUtilsSeedingRule.class);
}
