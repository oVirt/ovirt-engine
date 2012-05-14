#!/bin/sh

die () {
    printf >&2 "$@"
    exit 1
}

usage () {
    echo "Usage:"
    echo "  $0 [git CA dir path] [target working directory]"
    echo "e.g.:"
    echo "  $0 `pwd` /tmp"

    exit 1
}

# Check Args
[ "$#" -ge 2 ] || usage

# Developer mode
PASS=NoSoup4U
ALIAS=engine
DATE=`date --date "now -1 days" +"%y%m%d%H%M%S%z"`
SUBJECT=`hostname`
export START_DIR=`pwd`
COUNTRY=US
ORG=oVirt.org
WORKDIR=$2/ca
CA_SUBJECT=$SUBJECT
[ -d "$1" ] || die "Directory $1 does not exist"
[ -d "$1/keys" ] || die "Directory $1 is not CA !"
[ -d "$2" ] || die "Directory $2 does not exist"

# Copy files
echo "# Generating CA in working directory..."
cp -a $1 $2/

# Go to scripts location
cd $WORKDIR

# Run standard install CA script
./installCA.sh $SUBJECT $COUNTRY $ORG $ALIAS $PASS $DATE $WORKDIR $CA_SUBJECT

export START_DIR=

echo " "
echo "CA Installation Done."
echo " "
echo "======================================================"
echo "Please note:"
echo "======================================================"
echo "}}} Do not forget to set:"
echo " "
echo "}}} 1. Update ConfigValues.java (if not possible in DB):"
echo "}}} 1.1. CABaseDirectory to $WORKDIR"
echo "}}} 1.2. keystoreUrl to $WORKDIR/.keystore"
echo "}}} 1.3. TruststoreUrl to $WORKDIR/.keystore"
echo "}}} 1.4. Fix SignScriptName to sh(/bat)"
echo "}}} 1.5. PublicURLPath to [jboss dir]/standalone/deployments/engine.ear/root.war/"
echo " "
echo "}}} 2. DB updates (vdc_options table):"
echo "}}} 2.1. CertificateFileName $WORKDIR/certs/engine.cer"
echo "}}} 2.2. CABaseDirectory to $WORKDIR"
echo "}}} 2.3. CAEngineKey to $WORKDIR/private/ca.pem"
echo " "
echo "}}} 3. File updates:"
echo "}}} 3.1. Copy $WORKDIR/keys/engine.ssh.key.txt to root context of JBoss."
echo "}}}      ie: cp $WORKDIR/keys/engine.ssh.key.txt /usr/local/jboss-as/standalone/deployments/engine.ear/root.war/"
echo "======================================================"
echo " "

exit 0
