package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ObjectCounterTest {

    @Test
    public void testIncreaseNoDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(false);


        assertThat(objectCounter.increase(1), is(true));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.increase(1), is(false));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.increase(2), is(true));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.increase(-3), is(true));

        assertThat(objectCounter.contains(0), is(false));
        assertThat(objectCounter.contains(1), is(true));
        assertThat(objectCounter.contains(2), is(true));
        assertThat(objectCounter.contains(-3), is(true));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
    }

    @Test
    public void testIncreaseWithDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(true);


        assertThat(objectCounter.increase(1), is(true));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.increase(1), is(true));
        assertThat(objectCounter.containsDuplicates(), is(true));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.increase(2), is(true));
        assertThat(objectCounter.containsDuplicates(), is(true));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.increase(-3), is(true));

        assertThat(objectCounter.contains(0), is(false));
        assertThat(objectCounter.contains(1), is(true));
        assertThat(objectCounter.contains(2), is(true));
        assertThat(objectCounter.contains(-3), is(true));
        assertThat(objectCounter.containsDuplicates(), is(true));
        assertThat(objectCounter.containsCounts(), is(true));
    }

    @Test
    public void testDecreaseNoDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(false);

        objectCounter.increase(1);
        objectCounter.increase(2);
        assertThat(objectCounter.contains(0), is(false));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));

        objectCounter.decrease(0);
        assertThat(objectCounter.contains(0), is(false));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.contains(1), is(true));

        objectCounter.decrease(1);
        assertThat(objectCounter.contains(1), is(false));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
    }

    @Test
    public void testDecreaseWithDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(true);

        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(false));
        objectCounter.increase(1);
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
        objectCounter.increase(1);
        assertThat(objectCounter.containsDuplicates(), is(true));
        assertThat(objectCounter.containsCounts(), is(true));
        objectCounter.increase(2);
        assertThat(objectCounter.containsDuplicates(), is(true));
        assertThat(objectCounter.containsCounts(), is(true));

        assertThat(objectCounter.contains(0), is(false));
        objectCounter.decrease(0);
        assertThat(objectCounter.contains(0), is(false));
        assertThat(objectCounter.containsDuplicates(), is(true));
        assertThat(objectCounter.containsCounts(), is(true));

        assertThat(objectCounter.contains(1), is(true));
        objectCounter.decrease(1);
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));
        assertThat(objectCounter.contains(1), is(true));
        objectCounter.decrease(1);
        assertThat(objectCounter.contains(1), is(false));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(true));

        assertThat(objectCounter.contains(2), is(true));
        objectCounter.decrease(2);
        assertThat(objectCounter.contains(2), is(false));
        assertThat(objectCounter.containsDuplicates(), is(false));
        assertThat(objectCounter.containsCounts(), is(false));

    }
}
