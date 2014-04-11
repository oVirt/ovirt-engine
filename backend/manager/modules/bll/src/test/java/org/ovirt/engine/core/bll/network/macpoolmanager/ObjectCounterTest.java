package org.ovirt.engine.core.bll.network.macpoolmanager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ObjectCounterTest {

    @Test
    public void testAddNoDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(false);


        assertThat(objectCounter.add(1), is(true));
        assertThat(objectCounter.add(1), is(false));
        assertThat(objectCounter.add(2), is(true));
        assertThat(objectCounter.add(-3), is(true));

        assertThat(objectCounter.contains(0), is(false));
        assertThat(objectCounter.contains(1), is(true));
        assertThat(objectCounter.contains(2), is(true));
        assertThat(objectCounter.contains(-3), is(true));
    }

    @Test
    public void testAddWithDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(true);


        assertThat(objectCounter.add(1), is(true));
        assertThat(objectCounter.add(1), is(true));
        assertThat(objectCounter.add(2), is(true));
        assertThat(objectCounter.add(-3), is(true));

        assertThat(objectCounter.contains(0), is(false));
        assertThat(objectCounter.contains(1), is(true));
        assertThat(objectCounter.contains(2), is(true));
        assertThat(objectCounter.contains(-3), is(true));
    }

    @Test
    public void testRemoveNoDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(false);

        objectCounter.add(1);
        objectCounter.add(2);

        assertThat(objectCounter.contains(0), is(false));
        objectCounter.remove(0);
        assertThat(objectCounter.contains(0), is(false));

        assertThat(objectCounter.contains(1), is(true));
        objectCounter.remove(1);
        assertThat(objectCounter.contains(1), is(false));
    }

    @Test
    public void testRemoveWithDuplicates() throws Exception {
        final ObjectCounter<Integer> objectCounter = new ObjectCounter<>(true);

        objectCounter.add(1);
        objectCounter.add(1);
        objectCounter.add(2);

        assertThat(objectCounter.contains(0), is(false));
        objectCounter.remove(0);
        assertThat(objectCounter.contains(0), is(false));

        assertThat(objectCounter.contains(1), is(true));
        objectCounter.remove(1);
        assertThat(objectCounter.contains(1), is(true));
        objectCounter.remove(1);
        assertThat(objectCounter.contains(1), is(false));

        assertThat(objectCounter.contains(2), is(true));
        objectCounter.remove(2);
        assertThat(objectCounter.contains(2), is(false));

    }
}
