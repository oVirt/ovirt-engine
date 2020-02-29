package org.ovirt.engine.core.bll.storage.imageio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.storage.disk.image.ImageioClient;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicket;
import org.ovirt.engine.core.common.businessentities.storage.ImageTicketInformation;
import org.ovirt.engine.core.compat.Guid;

/*
These are *functional* tests - skipped by default.
Can be used when the imageio server is running.
For testing, use following steps:
- Clone ovirt-imageio:
    $ git clone https://github.com/oVirt/ovirt-imageio.git
    $ cd ovirt-imageio/daemon
- Run ovirt-image with test conf folder
    $ ./ovirt-imageio -c \
        ~/git/ovirt-engine/backend/manager/modules/bll/src/test/java/org/ovirt/engine/core/bll/storage/imageio/conf
 */
class ImageioClientTest {
    public static String IMAGEIO_HOSTNAME = "127.0.0.1";
    public static int IMAGEIO_PORT = 54324;

    private ImageioClient imageioClient = new ImageioClient(IMAGEIO_HOSTNAME, IMAGEIO_PORT);
    private ImageTicket imageTicket = getTestTicket();

    @BeforeAll
    static void ensureSocketExists() {
        boolean imageioIsRunning;
        try (Socket ignored = new Socket(IMAGEIO_HOSTNAME, IMAGEIO_PORT)) {
            imageioIsRunning = true;
        } catch (IOException ex) {
            imageioIsRunning = false;
        }
        assumeTrue(imageioIsRunning);
    }

    @Test
    void putTicket() {
        assertDoesNotThrow(() -> imageioClient.putTicket(imageTicket));
        ImageTicketInformation ticket = imageioClient.getTicket(imageTicket.getId());
        assertEquals(imageTicket.getId(), ticket.getId());
    }

    @Test
    void extendTicket() {
        imageioClient.putTicket(imageTicket);
        ImageTicketInformation oldTicket = imageioClient.getTicket(imageTicket.getId());
        assertDoesNotThrow(() -> imageioClient.extendTicket(imageTicket.getId(), 600));
        ImageTicketInformation newTicket = imageioClient.getTicket(imageTicket.getId());
        assertTrue(newTicket.getExpires() >= oldTicket.getExpires() + oldTicket.getTimeout());
        assertTrue(oldTicket.getExpires() + oldTicket.getTimeout() + 1 >= newTicket.getExpires());
    }

    @Test
    void deleteTicket() {
        imageioClient.putTicket(imageTicket);
        imageioClient.deleteTicket(imageTicket.getId());
        Throwable exception = assertThrows(
                RuntimeException.class, () -> imageioClient.getTicket(imageTicket.getId()));
        assertTrue(exception.getMessage().contains("Not Found"));
    }

    private ImageTicket getTestTicket() {
        ImageTicket ticket = new ImageTicket();
        ticket.setId(Guid.createGuidFromString("799030aa-97c3-4354-871e-0adf0556fcbf"));
        ticket.setSize(1073741824L);
        ticket.setUrl("https://server:port/images/799030aa-97c3-4354-871e-0adf0556fcbf");
        ticket.setTimeout(300);
        ticket.setOps(new String[]{"read", "write"});
        ticket.setTransferId("ad3980d3-2cc1-4e48-b734-67f956791574");
        return ticket;
    }
}
