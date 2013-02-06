#!/bin/sh
#
# This shell script takes care of starting and stopping oVirt event notification service
#
# chkconfig: - 80 20
# description: The oVirt event notification service
# processname: notifierd
# pidfile: /var/run/ovirt-engine/notifier/engine-notifier.pid
# config:  /etc/ovirt-engine/notifier/notifier.conf
#

# Source function library.
. /etc/init.d/functions

prog=engine-notifier
RETVAL=0
PID_FOLDER=/var/run/ovirt-engine/notifier
ENGINE_USER="ovirt"

[ -r /etc/java/java.conf ] && . /etc/java/java.conf
export JAVA_HOME

# Path to the engine-notifier launch script
NOTIFIER_SCRIPT=/usr/share/ovirt-engine/bin/engine-notifier.sh

cd $(getent passwd $ENGINE_USER | cut -d: -f6)

if [ -z "$SHUTDOWN_WAIT" ]; then
    SHUTDOWN_WAIT=10
fi

if [ -z "$NOTIFIER_PID" ]; then
    mkdir -p $PID_FOLDER
    if [ $? -ne 0 ]; then
        echo "Error: Please check permissions, can not create PID folder: $PID_FOLDER. "
        exit 5
    fi
    NOTIFIER_PID=$PID_FOLDER/$prog.pid
fi

lock_file=/var/lock/subsys/$prog

start() {
    if [ -f $lock_file ] ; then
        if [ -f $NOTIFIER_PID ]; then
            read kpid < $NOTIFIER_PID
            if checkpid $kpid 2>&1; then
                echo "$prog process (pid $kpid) is already running"
                return 0
            else
                echo "lock file found but no process is running for pid $kpid, continuing"
            fi
        fi
    fi
    echo -n $"Starting $prog: at $(date)"

    daemon --user $ENGINE_USER NOTIFIER_PID=$NOTIFIER_PID $NOTIFIER_SCRIPT
    RETVAL=$?
    [ $RETVAL = 0 ] && touch $lock_file && success || failure
    echo
    return $RETVAL
}

stop() {

    if [ -f $lock_file ] ; then
        echo -n $"Stopping $prog: "
        killproc -p $NOTIFIER_PID -d $SHUTDOWN_WAIT
        RETVAL=$?
        echo
        if [ $RETVAL -eq 0 ]; then
            rm -f $lock_file $NOTIFIER_PID
        fi
    fi
}

status() {
    RETVAL="1"
    STOPPED="0"
    if [ -f "$NOTIFIER_PID" ]; then
        read kpid < $NOTIFIER_PID
        if checkpid $kpid 2>&1; then
            echo "$0 is running (${kpid})"
            RETVAL="0"
        else
            echo "Notifier service is not running for pid $kpid"
            rm -f $lock_file $NOTIFIER_PID
        fi
    else
        pid="$(pgrep -fu $ENGINE_USER org\.ovirt\.engine\.core\.notifier\.Notifier)"
        if [ -n "$pid" ]; then
            echo "Notifier service $0 running (${pid}) but no PID file exists"
            RETVAL="0"
        else
            echo "$0 is stopped"
            rm -f $lock_file
        fi
    fi
    return $RETVAL
}


# See how we were called.
case "$1" in
  start)
        start
        ;;
  stop)
        stop
        ;;
  status)
        status
        ;;
  restart)
        stop
        sleep 2
        start
        ;;
  condrestart)
        if [ -f $NOTIFIER_PID ] ; then
            stop
            start
        fi
        ;;
  *)
        echo "Usage: $0 {start|stop|status|restart|condrestart}"
        RETVAL=3
esac

exit $RETVAL

#
#
# end
