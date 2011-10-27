#!/bin/sh

die () {
        printf >&2 "$@"
        exit 1
}

usage () {
        printf "CreatePem.sh - convert pfx file to pem file\n"
        printf "Usage:\n"
	    printf "\tCreatePem [pfx-file] [pem-file] [in-pass] [out-pass]\n"
	    printf "Where:\n"
	    printf "\tpfx-file  = pfx input file\n"
	    printf "\tpem-file  = pem output file\n"
	    printf "\tin-pass   = pfx password\n"
	    printf "\tout-pass  = pem password\n"
        return 0
}

if [ ! "$#" -eq 4 ]; then
	usage
	die "Error: wrong argument number: $#.\n"
fi

pfx_file=$1
pem_file=$2
pass_in=$3
pass_out=$4

if [ ! -f $pfx_file ]; then
    die "Error: pfx file $pfx_file is not exists"
fi

if [ -f $pem_file ]; then
    rm -f $pem_file
fi

openssl pkcs12 -in $pfx_file -out $pem_file.orig -nocerts -passin pass:$pass_in -passout pass:$pass_out || die 'error: cannot find openssl'

IFS=$'\n'
i=1
for line in `cat $pem_file.orig`;
do
    if [ $i -gt 3 ]; then
        echo $line >> $pem_file
    fi
    let i+=1
done

rm -f $pem_file.orig

exit $?

