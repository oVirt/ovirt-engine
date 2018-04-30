package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.compat.Guid;

/**
 * The following test checks multi-threading issues with Dao usage.
 * The test uses the TagsDao, but any other Dao can be used
 */
public class MultiThreadedDaoTest extends BaseDaoTestCase<TagDao> {
    private static final Guid[] EXISTING_TAGS_IDS = {
            new Guid("d3ec3e01-ca89-48e1-8b43-a9b38f873b0c"),
            new Guid("d3ec3e01-ca89-48e1-8b43-a9b38f873b0d"),
            new Guid("d3ec3e01-ca89-48e1-8b43-a9b38f873b0e") };

    private CountDownLatch latch = null;

    @Test
    public void testGetSameID() throws Exception {
        final Tags existing = dao.get(EXISTING_TAGS_IDS[0]);

        createAndRunThreadsForRunner(() -> {
            Tags result = dao.get(existing.getTagId());
            assertEquals(existing, result);
        }, 100);
    }

    @Test
    public void testGetDifferentID() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final Tags[] existingTags = new Tags[EXISTING_TAGS_IDS.length];
        for (int i = 0; i < EXISTING_TAGS_IDS.length; i++) {
            existingTags[i] = dao.get(EXISTING_TAGS_IDS[i]);
        }

        createAndRunThreadsForRunner(() -> {
            int val = counter.incrementAndGet();
            int index = val % EXISTING_TAGS_IDS.length;
            Tags result = dao.get(EXISTING_TAGS_IDS[index]);
            assertEquals(existingTags[index], result);
        }, 100);
    }

    @Test
    public void testReadWriteDelete() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        createAndRunThreadsForRunner(() -> {
            int val = counter.incrementAndGet();
            Tags tag = createTag("tag" + val, "desc" + val);
            dao.save(tag);
            Tags fromDb = dao.get(tag.getTagId());
            assertEquals(tag, fromDb);
            dao.remove(tag.getTagId());
            fromDb = dao.get(tag.getTagId());
            assertNull(fromDb);
        }, 100);
    }

    private Tags createTag(String name, String desc) {
        Tags tag = new Tags();
        tag.setChildren(new ArrayList<>());
        tag.setDescription(desc);
        tag.setTagName(name);
        tag.setIsReadonly(true);
        tag.setParentId(Guid.Empty);
        tag.setType(TagsType.GeneralTag);
        return tag;
    }

    private void createAndRunThreadsForRunner(Runnable runnable, int numOfThreads) throws Exception {
        if (runnable == null) {
            return;
        }
        latch = new CountDownLatch(numOfThreads);
        ExecutorService threadPool = Executors.newFixedThreadPool(numOfThreads);
        for (int counter = 0; counter < numOfThreads; counter++) {
            threadPool.execute(new LatchedRunnableWrapper(runnable, latch));
        }
        latch.await();
    }
}
