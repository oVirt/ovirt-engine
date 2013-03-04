#!/bin/bash

pushd $(dirname ${0})>/dev/null
#include db general functions
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

CERTIFICATE="/etc/pki/ovirt-engine/certs/engine.cer"
FIXIT=false
CONFIG_SELECT_CMD="select a.option_name as key , a.option_value as val  from vdc_options a where (option_name ilike '%%password' or option_name ilike '%%pass') and length(option_value) > 0 and length(option_value)  < 100;"
HOST_PM_PRIMARY_SELECT_CMD="select a.vds_id as key, a.pm_password as val from vds_static a where pm_enabled and length(pm_password) > 0 and length(pm_password) < 100;"
HOST_PM_SECONDARY_SELECT_CMD="select a.vds_id as key, a.pm_secondary_password as val from vds_static a where pm_enabled and length(pm_secondary_password) > 0 and length(pm_secondary_password) < 100;"
STORAGE_SELECT_CMD="select a.connection as key , a.password as val from storage_server_connections a where length(password) > 0 and length(password) < 100;"
CONFIG_UPDATE_CMD="update vdc_options set option_value = '%s' where option_name =  '%s' and version = 'general';"
HOST_PM_PRIMARY_UPDATE_CMD="update vds_static set pm_password = '%s' where vds_id = '%s';"
HOST_PM_SECONDARY_UPDATE_CMD="update vds_static set pm_secondary_password = '%s' where vds_id = '%s';"
STORAGE_UPDATE_CMD="update storage_server_connections set password = '%s' where connection='%s';"

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME [-p PORT]] [-d DATABASE] [-u USERNAME] [-l LOGFILE] [-c CERTIFICATE] [-f] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database        (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                         (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The admin username for the database.\n"
    printf "\t-l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})\n"
    printf "\t-c CERTIFICATE- The certificate file to use for the encryption.(def /etc/pki/ovirt-engine/certs/engine.cer)\n"
    printf "\t-f            - Fix the non encrypted data in DB.\n"
    printf "\t-v            - Turn on verbosity                         (WARNING: lots of output)\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    popd>/dev/null
    exit $ret
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

encryptkey() {
    key=${1}
    passwd="${2}"
    command="${3}"
    certificate="${4}"
    msg="Failed to encrypt key ${key}."
    encryptedPasswd=$(echo -n ${passwd} | /usr/bin/openssl rsautl -certin -inkey ${certificate} -encrypt -pkcs | /usr/bin/openssl enc -a)

    if [ $? -ne 0  -o -z "${encryptedPasswd}" ]; then
          echo ${msg}
      else
          # update the password
          command=$(printf "${command}" "${encryptedPasswd}" ${key})
          execute_command "${command}" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    fi
}

encryptall() {
    selectcommand="${1}"
    updatecommand="${2}"
    certificate="${3}"
    filename=$(mktemp)
    execute_command "${selectcommand}" ${DATABASE} ${SERVERNAME} ${PORT} > ${filename}
    while read line
    do
      # extracting the relevant fields values from each record.
      if [ $(echo $line | grep "|" |wc -l) -eq 0 ]; then
          continue
      fi
      key=$(echo "${line}" | cut -d "|" -f1 | sed 's/^ *//g' | tr -d ' ')
      val=$(echo "${line}" | cut -d "|" -f2 | sed 's/^ *//g' | tr -d ' ')
      if [ "$key" != "" -a "$val" != "" ]; then
          encryptkey "${key}" "${val}" "${updatecommand}" "${certificate}"
      fi
    done < ${filename}
    rm -f ${filename}
}

while getopts hs:d:u:p:l:c:fv option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        c) CERTIFICATE=$OPTARG;;
        f) FIXIT=true;;
        v) VERBOSE=true;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

if [ "${FIXIT}" = "true" ]; then
    echo "Caution, this operation should be used with care. Please contact support prior to running this command"
    echo "Are you sure you want to proceed? [y/n]"
    read answer

    if [ "${answer}" = "n" ]; then
       echo "Please contact support for further assistance."
       popd>/dev/null
       exit 1
    fi
else
   echo "Checking configuration values..."
   psql -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${CONFIG_SELECT_CMD}" "${DATABASE}"
   echo "Checking host primary power management values..."
   psql -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${HOST_PM_PRIMARY_SELECT_CMD}" "${DATABASE}"
   echo "Checking host secondary power management values..."
   psql -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${HOST_PM_SECONDARY_SELECT_CMD}" "${DATABASE}"
   echo "Checking storage connections values..."
   psql -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${STORAGE_SELECT_CMD}" "${DATABASE}"
   popd>/dev/null
   exit 1
fi


encryptall "${CONFIG_SELECT_CMD}" "${CONFIG_UPDATE_CMD}" "${CERTIFICATE}"
encryptall "${HOST_PM_PRIMARY_SELECT_CMD}" "${HOST_PM_PRIMARY_UPDATE_CMD}" "${CERTIFICATE}"
encryptall "${HOST_PM_SECONDARY_SELECT_CMD}" "${HOST_PM_SECONDARY_UPDATE_CMD}" "${CERTIFICATE}"
encryptall "${STORAGE_SELECT_CMD}" "${STORAGE_UPDATE_CMD}" "${CERTIFICATE}"

