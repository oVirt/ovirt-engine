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
        printf "\tdays to expire              = Amount of days until certificate expires.\n"
        printf "\tCA Directory                = Full path to CA directory\n"
        printf "\tstartdate                   = in YYMMDDHHMMSSZ ANS1 format\n"
        printf "\tPass                        = Certificate password\n"
        printf "\tHost                        = CN\n"
        printf "\tOrganization                = O\n"
        printf "\tlocking timeout             = Amount of seconds to wait for locking\n"
        return 0
}

sign () {
      cd $ca_dir

      if openssl x509 -text -in ca.pem | grep "Subject Key Identifier" > /dev/null; then
          EXTRA_COMMAND="-extfile cert.conf -extensions v3_ca"
      fi
      openssl ca \
        -batch -policy policy_match -config openssl.conf -cert ca.pem \
        -in requests/$req_file -keyfile private/ca.pem -passin pass:$cert_pass \
        -days $exp_time -out certs/$out_file -startdate $start_time \
        ${req_name:+-subj "/O=$req_org/CN=$req_name"} \
        ${EXTRA_COMMAND}
}

if [ "$#" -lt 6 ]; then
        usage
        die "Error: wrong argument number: $#.\n"
fi

result=9
req_file=$1
out_file=$2
exp_time=$3
ca_dir=$4
start_time=$5
cert_pass=$6
req_name=$7
req_org=$8

lock_file="$(dirname "$0")/SignReq.lock"
shift
timeout=$8
if [ -z "$timeout" ]; then
        timeout=20
fi

{
        # Wait for lock on $lock_file (fd 200) for $timeout seconds
        flock -e -w $timeout 200 || die "Timeout waiting for lock. Giving up"
        sign
        result=$?

} 200< $lock_file

exit $result
