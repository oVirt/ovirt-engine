#!/bin/bash -xe

PGENGINE=/usr/bin
PGDATA=/var/lib/pgsql/data
PGENV=""

HOST=localhost
PORT=$(seq 65000 65535 | sort -R | head -n 1)
while netstat -tulpn | grep -q "$PORT"; do
    sleep 1
    PORT=$(seq 65000 65535 | sort -R | head -n 1)
done

function cleanup {
  su -l postgres -c "$PGENV $PGENGINE/pg_ctl stop -D '$PGDATA' -s -m fast"
}

trap cleanup EXIT

# Build pgdata folder
su -l postgres -c "$PGENV $PGENGINE/initdb --pgdata='$PGDATA' --auth='ident'"

echo "host    all     all     127.0.0.1/0     trust" > $PGDATA/pg_hba.conf
echo >> $PGDATA/pg_hba.conf
echo "host    all     all     ::1/128 trust" >> $PGDATA/pg_hba.conf
echo >> $PGDATA/pg_hba.conf
echo "local   all     all             trust" >> $PGDATA/pg_hba.conf
echo >> $PGDATA/pg_hba.conf
echo "host    engine  engine  0.0.0.0/0       md5" >> $PGDATA/pg_hba.conf
echo >> $PGDATA/pg_hba.conf
echo "host    engine  engine  ::0/0   md5" >> $PGDATA/pg_hba.conf

# Start postgres server
su -l postgres -c "$PGENV $PGENGINE/pg_ctl start -D ${PGDATA} -s -o \"-h $HOST -p $PORT\" -w -t 300"

if [ $? -ne 0 ]; then
    exit 1
fi

CI_MAVEN_SETTINGS=$1
DB_NAME=ovirt_engine_dao_unit_tests

su - postgres -c "$PGENV psql -h $HOST -p $PORT -d template1 -c \"create role engine;\" || \:"
su - postgres -c "$PGENV psql -h $HOST -p $PORT -d template1 -c \"ALTER ROLE engine WITH login\" || \:"
su - postgres -c "$PGENV dropdb -h $HOST -p $PORT engine || \:"
su - postgres -c "$PGENV psql -h $HOST -p $PORT -d template1 -c \"create database ${DB_NAME} owner engine;\""
su - postgres -c "$PGENV psql -h $HOST -p $PORT -d ${DB_NAME} << __EOF__
create extension \"uuid-ossp\";
__EOF__
"

$PGENV /usr/bin/bash -c "PGPASSWORD=engine ./packaging/dbscripts/schema.sh -c apply -d ${DB_NAME} -u engine -s $HOST -p $PORT"

if [ $? -ne 0 ]; then
    exit 1
fi

EXTRA_BUILD_FLAGS=(
    -P enable-dao-tests
    -D engine.db.username=engine
    -D engine.db.password=engine
    ${CI_MAVEN_SETTINGS}
    -D "engine.db.url=jdbc:postgresql://$HOST:$PORT/${DB_NAME}"
)

make maven BUILD_GWT=0 BUILD_UT=1 \
    EXTRA_BUILD_FLAGS="${EXTRA_BUILD_FLAGS[*]}"

if [ $? -ne 0 ]; then
    echo "maven failed"
    exit 1
fi
