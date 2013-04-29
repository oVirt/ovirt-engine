#!/bin/bash

#include db general functions
source ./dbfunctions.sh

if [[ -z "${ENGINE_CERTIFICATE}" ]]; then
    echo "Engine certificate was not set, skipping"
    exit 0
fi

# change password column to text to fit the encrypted password.
CMD="select fn_db_change_column_type('storage_server_connections','password','VARCHAR','text');"
execute_command "${CMD}" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null

# get all connections that have a password configured
CMD="select id, connection||' '||coalesce(iqn, '') as name, password from storage_server_connections where password is not null;"
execute_command "${CMD}" "${DATABASE}" "${SERVERNAME}" "${PORT}" | \
        sed -e 's/^ *//' -e 's/ *$//' -e 's/ *| */|/g' | \
        while read line; do
    # filter lines that don't look like tuples: uuid | name | password
    if echo "${line}" | grep -vq '^ *[a-fA-F0-9-]\{32,\}|[^|]\+|.\+'; then
        continue
    fi

    connId="$(echo "${line}" | cut -d'|' -f1)"
    connName="$(echo "${line}" | cut -d'|' -f2)"
    connPasswd="$(echo "${line}" | cut -d'|' -f3-)"

    # encrypt the password
    encryptedPasswd="$(echo -n "${connPasswd}" | /usr/bin/openssl rsautl -certin -inkey "${ENGINE_CERTIFICATE}" -encrypt -pkcs | /usr/bin/openssl enc -a)"
    if [ $? -ne 0 -o -z "${encryptedPasswd}" ]; then
        # note that an empty password here indicates failure to encrypt
        echo "Failed to encrypt connection ${connName} password. The password will remain unencrypted in the database until this is complete."
    else
        # update the password field for the given connection
        CMD="update storage_server_connections set password = '${encryptedPasswd}' where id = '${connId}';"
        execute_command "${CMD}" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
    fi
done

# execute_command exits early if a query fails, otherwise this
# should exit successfully (possibly with warnings echoed)
exit 0
