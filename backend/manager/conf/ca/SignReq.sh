#!/bin/sh

die () {
        printf >&2 "$@"
        exit 1
}

usage () {
        printf "SignReq.sh - Sign a certificate request (with ca key)\n"
        printf "USAGE:\n"
        printf "\tSignReq [Request Filename] [Output certificate filename] [days to expire] [CA Directory] [startdate] [lock file] [locking timeout]\n"
        printf "Where:\n"
        printf "\tRequest Filename            = Filename of request file. must reside under requests directory.\n"
        printf "\tOutput certificate filename = Filename of output file. will reside under certs directory.\n"
        printf "\tdays to expire              = Amount of days till certificate expires.\n"
        printf "\tCA Directory                = Full path to CA directory\n"
        printf "\tstartdate                   = in YYMMDDHHMMSSZ ANS1 format\n"
        printf "\tPass                        = Certificate password\n"
        printf "\tlock file                   = Name of file to be used for locking\n"
        printf "\tlocking timeout             = Amount of seconds to wait for locking\n"
        return 0
}

rollback () {
       [[ $step -eq 1 ]] && rm -f $lock_file
}

sign () {
      cd $ca_dir

      openssl x509 -text -in ca.pem | grep "Subject Key Identifier"
      if [ "$?" -eq 0 ]; then
            openssl ca -batch -policy policy_match -config openssl.conf -extfile cert.conf -extensions v3_ca -cert ca.pem \
             -in requests/$req_file -keyfile private/ca.pem -passin pass:$cert_pass -days $exp_time -out certs/$out_file -startdate $start_time
      else
            openssl ca -batch -policy policy_match -config openssl.conf -cert ca.pem -in requests/$req_file -keyfile private/ca.pem \
             -passin pass:$cert_pass -days $exp_time -out certs/$out_file -startdate $start_time
      fi
      return $?
}

if [ "$#" -lt 6 ]; then
        usage
        die "Error: wrong argument number: $#.\n"
fi

step=0
result=9
req_file=$1
out_file=$2
exp_time=$3
ca_dir=$4
start_time=$5
cert_pass=$6
lock_file=$7
if [ -z "$lock_file" ]; then
        lock_file=/var/lock/engine/.openssl.exclusivelock
fi

timeout=$8
if [ -z "$timeout" ]; then
        timeout=20
fi

trap "rollback; exit $result" HUP KILL INT QUIT TERM

{
        # Wait for lock on $lock_file (fd 200) for $timeout seconds
        flock -e -w $timeout 200
        if [ $? -eq 0 ];
        then
                step=1
                sign
        else
                die "Timeout waiting for lock. Giving up"
        fi
        result=$?

} 200>$lock_file

exit $result
