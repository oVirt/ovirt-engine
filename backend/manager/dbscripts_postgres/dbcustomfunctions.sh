#!/bin/bash 

insert_initial_data() {
    printf "Inserting data  ...\n"
    execute_file "insert_data.sql" ${DATABASE} > /dev/null
    printf "Inserting configuration  ...\n"
    execute_file "fill_config.sql" ${DATABASE} > /dev/null
    printf "Inserting pre-defined roles ...\n"
    execute_file "insert_predefined_roles.sql" ${DATABASE} > /dev/null
}

set_defaults() {
    ME=$(basename $0)
    SERVERNAME="127.0.0.1"
    DATABASE="engine"
    USERNAME=""
    VERBOSE=false
    LOGFILE="$ME.log"
}

