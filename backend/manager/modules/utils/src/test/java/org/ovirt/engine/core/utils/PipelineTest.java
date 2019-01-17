package org.ovirt.engine.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

class PipelineTest {

    @Test
    public void testFilter() {
        List<Integer> values = Arrays.asList(1, 2, 3, 4);
        assertThat(collectToList(
                Pipeline.create(Arrays.asList(1, 2, 3, 4).iterator())
                        .filter(val -> val % 2 == 0)
                        .iterator()))
                .containsExactly(2, 4);
    }

    @Test
    public void testMap() {
        List<Integer> values = Arrays.asList(1, 2, 3, 4);
        assertThat(collectToList(
                Pipeline.create(values.iterator())
                        .map(val -> val * 2.0)
                        .iterator()))
                .containsExactly(2.0, 4.0, 6.0, 8.0);
    }

    @Test
    public void testDistinct() {
        List<Integer> values = Arrays.asList(1, 3, 2, 4, 2, 1, 1, 5, 3);
        assertThat(collectToList(
                Pipeline.create(values.iterator())
                        .distinct()
                        .iterator()))
                .containsExactly(1, 3, 2, 4, 5);
    }

    @Test
    public void testLazy() {
        List<Integer> values = Arrays.asList(1, 1, 3, 3);
        Iterator<Integer> iterator = Pipeline.create(values.iterator())
                .append(() -> {
                    throw new AssertionError("Pipeline is not lazy");
                })
                .distinct()
                .iterator();

        assertEquals((int) iterator.next(), 1);
        assertEquals((int) iterator.next(), 3);
    }

    private <T> List<T> collectToList(Iterator<T> iterator) {
        List<T> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result;
    }
}
