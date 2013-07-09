#!/bin/bash

set_defaults() {
    ME=$(basename $0)
    SERVERNAME="localhost"
    PORT="5432"
    DATABASE="engine"
    USERNAME="engine"
    VERBOSE=false
    LOGFILE="$ME.log"

    # When running in development environments the .pgpass file may not
    # exist or might not be readable, so we should try to use the file
    # stored in the home directory of the user instead:
    PGPASSFILE="/etc/ovirt-engine/.pgpass"
    if [ ! -r "${PGPASSFILE}" ]
    then
        PGPASSFILE="${HOME}/.pgpass"
    fi
    export PGPASSFILE
}

