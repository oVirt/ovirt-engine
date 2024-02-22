#!/bin/bash
set -e

# Execute SQL commands
psql -v ON_ERROR_STOP=1 --username "postgres" <<-EOSQL
    CREATE USER ovirt PASSWORD 'ovirt';
    DROP DATABASE IF EXISTS engine;
    CREATE DATABASE engine OWNER ovirt TEMPLATE template0 ENCODING 'UTF8' lc_collate 'en_US.UTF-8' lc_ctype 'en_US.UTF-8';
EOSQL

psql -v ON_ERROR_STOP=1 --username "postgres" -d engine <<-EOSQL
    CREATE EXTENSION "uuid-ossp";
EOSQL

echo "oVirt PostgreSQL database has been setup!"
