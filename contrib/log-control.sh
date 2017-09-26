#!/bin/sh

function usage() {
    echo "
    USAGE
        $0 [LOG_CATEGORY] [LOG_LEVEL]

        LOG_CATEGORY
            Logging category (or namespace)

        LOG_LEVEL
            The desired log level


    EXAMPLE
       Debug all ovirt related loggers - $0 org.ovirt DEBUG
       Debug DB related stuff          - $0 org.ovirt.engine.core.dal DEBUG
        "
    exit 1
}

if [[ -z "${JBOSS_HOME}" ]]; then
    JBOSS_HOME=/usr/share/ovirt-engine-wildfly
fi

if [[ ! -x ${JBOSS_HOME}/bin/jboss-cli.sh ]]; then
    echo "jboss-cli.sh is missing. It should be reachable under "
    echo "JBOSS_HOME/bin which is pointing to the installation "
    echo "directory of the application server, typically "
    echo "/usr/share/ovirt-engine-wildfly."
    exit 1
fi


[[ $# -eq 2 ]] || usage

read  -s -p "Password: " PASS

# Add the log category, ignore failures
${JBOSS_HOME}/bin/jboss-cli.sh --controller=remote+http://127.0.0.1:8706 --connect --user=admin@internal -p=$PASS \
   --commands="
       /subsystem=logging/logger=$1:add
   "

# Set the logging level on the specified category
 ${JBOSS_HOME}/bin/jboss-cli.sh --controller=remote+http://127.0.0.1:8706 --connect --user=admin@internal -p=$PASS \
   --commands="
       /subsystem=logging/logger=$1:write-attribute(name=level,value=$2)
   "
