#!/bin/sh
#
# This script is designed to run the oVirt Event Notification service.
# The script assumes all RPM dependencies were installed, so jar
# files can be found under /usr/share/java. The service's configuration
# should be under the /etc directory by default.
#

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

usage () {
    printf "engine-notifier: oVirt Event Notification Service\n"
    printf "USAGE:\n"
    printf "\tengine-notifier [configuration file]\n"
    return 0
}

die_no_propset() {
    # exit when property defined but not set then exit
    die "Error: $1 if defined can not be empty, please check for this in configuration file $CONF_FILE\n" 6
}

check_email_format() {
    #exit with a message if the property is not in user@domain format
    if [[ ${!1} != *?@?* ]] ;  then
        die "Error: $1 must be of the form user@domain"
    fi
}

check_port_number(){
    #exit with a message if the property is not a valid port number
    if ! [[ "${!1}" =~ ^-?[0-9]+$ ]] ; then
       die "Error: $1 must be a valid port number"
    elif [ ${!1} -lt 1 ]  ||  [ ${!1} -gt 65536 ] ;  then
       die "Error: $1 must be a number between 0 and 65536"
    fi


}

check_boolean(){
    #exit with a message if the property is not true or false
    if ! [[ ${!1} == "true" ]] ||  [[ ${!1} == "false" ]] ;  then
       die "Error: $1 must be true or false"
    fi

}

if [ "$1" == "--help" -o "$1" == "-h" ]; then
    usage
    exit 0
fi

if [ "$#" -gt 1 ]; then
    usage
    die "Error: wrong argument number: $#.\n" 2
fi

if [ "$#" -eq 1 ]; then
    if [ ! -r "$1" ]; then
        die "Error: configuration file does not exist or has no read permission: $1.\n" 6
    fi
    CONF_FILE="$1"
else
    CONF_FILE="${ENGINE_ETC}/notifier/notifier.conf"
fi

# Import configurations safely
old_IFS=$IFS
IFS=$'\n'
for line in `sed -e 's/[ \t]*#.*//' -e '/^[ \t]*$/d' $CONF_FILE`:
do
    declare "$line"
done
IFS=$old_IFS

# Do basic checking of properties in configuration file to ensure
# a) properties are defined
# b) when properties are defined and reference a file system resource, that the resource exists.

# MAIL_SERVER is required!
if [ -z "$MAIL_SERVER" ]; then
    die "Error: \$MAIL_SERVER is not defined, please check for this in configuration file $CONF_FILE\n" 6
fi

# Remove possible : at the end of the MAIL_SERVER address
MAIL_SERVER=`echo $MAIL_SERVER | cut -d ':' -f 1`
nslookup $MAIL_SERVER &>/dev/null
if [ $? -ne 0 ] ; then
  die "Error: \$MAIL_SERVER ($MAIL_SERVER) must contain resolvable address"
fi


# Now check for properties that if defined, can not be empty
# INTERVAL_IN_SECONDS if defined can not be empty
if [ "${INTERVAL_IN_SECONDS+x}" ]; then
    if [ -z "$INTERVAL_IN_SECONDS" ]; then
        die_no_propset \$INTERVAL_IN_SECONDS
    fi
fi

# MAIL_PORT if defined can not be empty
if [ "${MAIL_PORT+x}" ]; then
    if [ -z "$MAIL_PORT" ]; then
        die_no_propset \$MAIL_PORT
    fi
    check_port_number "MAIL_PORT"
fi

# MAIL_USER if defined can not be empty
if [ "${MAIL_USER+x}" ]; then
    if [ -z "$MAIL_USER" ]; then
        die_no_propset \$MAIL_USER
    fi
    check_email_format "MAIL_USER"
fi

# MAIL_PASSWORD if defined can not be empty
if [ "${MAIL_PASSWORD+x}" ]; then
    if [ -z "$MAIL_PASSWORD" ]; then
        die_no_propset \$MAIL_PASSWORD
    fi
fi

# MAIL_ENABLE_SSL if defined can not be empty
if [ "${MAIL_ENABLE_SSL+x}" ]; then
    if [ -z "$MAIL_ENABLE_SSL" ]; then
        die_no_propset \$MAIL_ENABLE_SSL
    else
        # MAIL_USER if can not be empty for SSL
        if [ -z "${MAIL_USER}" ]; then
            die "Error: \$MAIL_USER is not defined for SSL MAIL, please check for this in configuration file $CONF_FILE\n" 6
        fi
        # MAIL_PASSWORD can not be empty for SSL
        if [ -z "${MAIL_PASSWORD}" ]; then
            die "Error: \$MAIL_PASSWORD is not defined for SSL MAIL, please check for this in configuration file $CONF_FILE\n" 6
        fi
    fi
fi

# HTML_MESSAGE_FORMAT if defined can not be empty
if [ "${HTML_MESSAGE_FORMAT+x}" ]; then
    if [ -z "$HTML_MESSAGE_FORMAT" ]; then
        die_no_propset \$HTML_MESSAGE_FORMAT
    fi
    check_boolean "HTML_MESSAGE_FORMAT"
fi

# MAIL_FROM if defined can not be empty
if [ "${MAIL_FROM+x}" ]; then
    if [ -z "$MAIL_FROM" ]; then
        die_no_propset \$MAIL_FROM
    fi
    check_email_format "MAIL_FROM"
fi

# MAIL_REPLY_TO if defined can not be empty
if [ "${MAIL_REPLY_TO+x}" ]; then
    if [ -z "$MAIL_REPLY_TO" ]; then
        die_no_propset \$MAIL_REPLY_TO
    fi
fi

# DAYS_TO_KEEP_HISTORY if defined can not be empty
if [ "${DAYS_TO_KEEP_HISTORY+x}" ]; then
    if [ -z "$DAYS_TO_KEEP_HISTORY" ]; then
        die_no_propset \$DAYS_TO_KEEP_HISTORY
    fi
fi

# ENGINE_INTERVAL_IN_SECONDS if defined can not be empty
if [ "${ENGINE_INTERVAL_IN_SECONDS+x}" ]; then
    if [ -z "$ENGINE_INTERVAL_IN_SECONDS" ]; then
        die_no_propset \$ENGINE_INTERVAL_IN_SECONDS
    fi
fi

# ENGINE_MONITOR_RETRIES if defined can not be empty
if [ "${ENGINE_MONITOR_RETRIES+x}" ]; then
    if [ -z "$ENGINE_MONITOR_RETRIES" ]; then
        die_no_propset \$ENGINE_MONITOR_RETRIES
    fi
fi

# ENGINE_TIMEOUT_IN_SECONDS if defined can not be empty
if [ "${ENGINE_TIMEOUT_IN_SECONDS+x}" ]; then
    if [ -z "$ENGINE_TIMEOUT_IN_SECONDS" ]; then
        die_no_propset \$ENGINE_TIMEOUT_IN_SECONDS
    fi
fi

# IS_HTTPS_PROTOCOL if defined can not be empty
if [ "${IS_HTTPS_PROTOCOL+x}" ]; then
    if [ -z "$IS_HTTPS_PROTOCOL" ]; then
        die_no_propset \$IS_HTTPS_PROTOCOL
    fi
fi

# IS_NONREPEATED_NOTIFICATION if defined can not be empty
if [ "${IS_NONREPEATED_NOTIFICATION+x}" ]; then
    if [ -z "$IS_NONREPEATED_NOTIFICATION" ]; then
        die_no_propset \$IS_NONREPEATED_NOTIFICATION
    fi
fi

if [ -z "$NOTIFIER_PID" ]
then
    NOTIFIER_PID=/dev/null
fi

#
# Add this option to the java command line to enable remote debugging in
# all IP addresses and port 8787:
#
# -Xrunjdwp:transport=dt_socket,address=0.0.0.0:8787,server=y,suspend=y
#
# Note that the "suspend=y" options is needed to suspend the execution
# of the JVM till you connect with the debugger, otherwise it is
# not possible to debug the execution of the main method.
#

"${JAVA_HOME}/bin/java" \
  -Dlog4j.configuration="file:${ENGINE_ETC}/notifier/log4j.xml" \
  -Djboss.modules.write-indexes=false \
  -jar "${JBOSS_HOME}/jboss-modules.jar" \
  -dependencies org.ovirt.engine.core.tools \
  -class org.ovirt.engine.core.notifier.Notifier \
  "${CONF_FILE}" \
  2>/dev/null &

echo $! >$NOTIFIER_PID
