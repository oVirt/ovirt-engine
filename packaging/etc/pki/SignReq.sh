#!/bin/sh

die() {
        local m="$1"
        echo "$m" >&2
        exit 1
}

usage() {
        cat << __EOF__
SignReq.sh - Sign a certificate request (with ca key)
USAGE:
    SignReq [Request Filename] [Output certificate filename] [days to expire] [CA Directory] [startdate] [lock file] [locking timeout]
Where:
    Request Filename            = Filename of request file. must reside under requests directory.
    Output certificate filename = Filename of output file. will reside under certs directory.
    days to expire              = Amount of days until certificate expires.
    CA Directory                = Full path to CA directory
    startdate                   = in YYMMDDHHMMSSZ ANS1 format
    Pass                        = Certificate password
    Host                        = CN
    Organization                = O
    locking timeout             = Amount of seconds to wait for locking
__EOF__
        exit 1
}

sign () {
      cd "$ca_dir"

      if openssl x509 -text -in ca.pem | grep "Subject Key Identifier" > /dev/null; then
          EXTRA_COMMAND="-extfile cert.conf -extensions v3_ca"
      fi
      openssl ca \
        -batch -policy policy_match -config openssl.conf -cert ca.pem \
        -in "requests/$req_file" -keyfile private/ca.pem -passin "pass:$cert_pass" \
        -days "$exp_time" -out "certs/$out_file" -startdate "$start_time" \
        ${req_name:+-subj "/O=$req_org/CN=$req_name"} \
        ${EXTRA_COMMAND}
}

result=9
req_file="$1"
out_file="$2"
exp_time="$3"
ca_dir="$4"
start_time="$5"
cert_pass="$6"
req_name="$7"
req_org="$8"

[ -n "${req_org}" ] || usage

lock_file="$(dirname "$0")/SignReq.lock"
shift
timeout=$8
if [ -z "$timeout" ]; then
        timeout=20
fi

# Wait for lock on $lock_file (fd 200) for $timeout seconds
(
        flock -e -w $timeout 9 || die "Timeout waiting for lock. Giving up"
        sign
) 9< "$lock_file"
result=$?

exit $result
