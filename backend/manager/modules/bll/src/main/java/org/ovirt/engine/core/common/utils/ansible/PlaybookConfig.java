package org.ovirt.engine.core.common.utils.ansible;

/**
 * Read-only ansible configuration view
 */
public interface PlaybookConfig {

    /**
     * @return ansible playbook name
     */
    String playbook();
}
