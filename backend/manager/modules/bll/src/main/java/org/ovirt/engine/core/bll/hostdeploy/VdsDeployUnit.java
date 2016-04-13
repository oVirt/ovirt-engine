package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ovirt.otopi.dialog.Event;

public interface VdsDeployUnit {

    /**
     * Special annotation to specify when the customization is necessary.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface CallWhen {
        /**
         * @return A condition that determines if the customization should run.
         */
        String[] value();
    }

    void setVdsDeploy(VdsDeployBase deploy);
    void init();

    /**
     * This method is called in reaction to event from host that was not processed by the infrastructure.
     * @param bevent event
     * @return true ~ the event was not processed and should be offered for processing to next unit, <br>
     *         false ~ the event was processed
     */
    boolean processEvent(Event.Base bevent) throws IOException;
}
