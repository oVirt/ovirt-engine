package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.EventNotificationHist;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.compat.Guid;

public class EventDaoTest extends BaseDaoTestCase<EventDao> {
    private static final int FREE_AUDIT_LOG_ID = 44295;
    private Guid existingSubscriber;
    private Guid newSubscriber;
    private EventSubscriber newSubscription;
    private EventNotificationHist newHistory;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingSubscriber = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
        newSubscriber = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
        newSubscription = new EventSubscriber();
        newSubscription.setSubscriberId(newSubscriber);
        newSubscription.setEventNotificationMethod(EventNotificationMethod.SMTP);
        newSubscription.setEventUpName("TestRun");
        newSubscription.setTagName("farkle");

        newHistory = new EventNotificationHist();
        newHistory.setAuditLogId(FREE_AUDIT_LOG_ID);
        newHistory.setEventName("Failure");
        newHistory.setMethodType("Email");
        newHistory.setReason("Dunno");
        newHistory.setSentAt(new Date());
        newHistory.setStatus(false);
        newHistory.setSubscriberId(existingSubscriber);
    }

    /**
     * Ensures an empty collection is returned when the user has no subscriptions.
     */
    @Test
    public void testGetAllForSubscriberWithNoSubscriptions() {
        List<EventSubscriber> result = dao.getAllForSubscriber(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all subscriptions are returned.
     */
    @Test
    public void testGetAllForSubscriber() {
        List<EventSubscriber> result = dao
                .getAllForSubscriber(existingSubscriber);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (EventSubscriber subscription : result) {
            assertEquals(existingSubscriber,
                    subscription.getSubscriberId());
        }
    }

    /**
     * Ensures that subscribing a user works as expected.
     */
    @Test
    public void testSubscribe() {
        dao.subscribe(newSubscription);

        List<EventSubscriber> result = dao.getAllForSubscriber(newSubscription
                .getSubscriberId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (EventSubscriber subscription : result) {
            assertEquals(newSubscriber, subscription.getSubscriberId());
        }
    }

    /**
     * Ensures that unsubscribing a user works as expected.
     */
    @Test
    public void testUnsubscribe() {
        List<EventSubscriber> before = dao
                .getAllForSubscriber(existingSubscriber);

        // ensure we have subscriptions
        assertFalse(before.isEmpty());
        for (EventSubscriber subscriber : before) {
            dao.unsubscribe(subscriber);
        }

        List<EventSubscriber> after = dao.getAllForSubscriber(existingSubscriber);

        assertNotNull(after);
        assertTrue(after.isEmpty());
    }
}
