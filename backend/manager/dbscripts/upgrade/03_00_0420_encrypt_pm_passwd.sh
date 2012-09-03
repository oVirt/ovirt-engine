#!/bin/bash

#include db general functions
source ./dbfunctions.sh

# get configuration values needed for password encryption from DB

caBaseDir=$(get_config_value "CABaseDirectory" "general")
keystore=$(get_config_value "keystoreUrl" "general")
passwd=$(get_config_value "keystorePass" "general")
alias=$(get_config_value "CertAlias" "general")
ear=$(get_config_value "ENGINEEARLib" "general")

# change pm_password column to text to fit the encrypted password.
CMD="select fn_db_change_column_type('vds_static','pm_password','VARCHAR','text');"
execute_command "${CMD}" "${DATABASE}" ${SERVERNAME} ${PORT} > /dev/null

# get all hosts that have PM configured (vds_id and pm_password)
filename=$(mktemp)
CMD="select vds_id,vds_name,pm_password from vds_static where pm_enabled = true;"
execute_command "${CMD}" "${DATABASE}" ${SERVERNAME} ${PORT} > ${filename}
while read line
do
  # extracting the relevant fields values from each record.
  if [ $(echo $line | grep "|" |wc -l) -eq 0 ]; then
      continue
  fi
  hostId=$(echo "${line}" | cut -d "|" -f1 | sed 's/^ *//g' | tr -d ' ')
  hostName=$(echo "${line}" | cut -d "|" -f2 | sed 's/^ *//g' | tr -d ' ')
  pmPasswd=$(echo "${line}" | cut -d "|" -f3 | sed 's/^ *//g' | tr -d ' ')
  if [ "$hostId" != "" -a "$pmPasswd" != "" ]; then
      # encrypt the password
      encryptedPasswd=$(${caBaseDir}/store-utils.sh -enc ${keystore}  ${passwd} ${alias} ${pmPasswd} ${ear})
      if [ $? -ne 0 ]; then
          echo "Failed to encrypt host ${hostName} Power Management password, please set host Power Management manually for this host."
      else
          # update the pm_password field for the given host
          CMD="update vds_static set pm_password = '${encryptedPasswd}' where vds_id = '${hostId}';"
          execute_command "${CMD}" "${DATABASE}" ${SERVERNAME} ${PORT} > /dev/null
      fi
  fi
done < ${filename}
