#!/bin/sh

die () {
    printf >&2 "$@"
    exit 1
}

usage () {
    echo "Usage:"
    echo "  $0 [git CA dir path] [target working directory] [jboss_ear_path] [ear_src_path]"
    echo "e.g.:"
    echo "  $0 `pwd` /ca-dir /usr/share/jbossas/standalone/deployments/engine.ear /usr/share/ovirt-engine"

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

# verify optional params
if [ "x" == "x$3" ];then
    JBOSS_EAR_PATH="/usr/share/jbossas/standalone/deployments/engine.ear"
else
    JBOSS_EAR_PATH=$3
fi

if [ "x" == "x$4" ];then
    EAR_SRC_PATH="/usr/share/ovirt-engine"
else
    EAR_SRC_PATH=$4
fi

[ -d "$1" ] || die "Src CA Directory $1 does not exist\n"
[ -d "$1/keys" ] || die "Ca Src Directory $1 is not CA !\n"
[ -d "$2" ] || die "Target CA Directory $2 does not exist!\n"
[ -d "$3" ] || die "Jboss Home Directory $3 does not exist!\n"

# we can't link if the jboss ear doesn't exist
[ -e "$JBOSS_EAR_PATH" ] || die "Can't find ear folder under $JBOSS_EAR_PATH\n"

# src dir might not exist in a devel env, create it
if [ ! -d $EAR_SRC_PATH ];then
    mkdir -p $EAR_SRC_PATH || die "Failed creating dir $EAR_SRC_PATH"
fi

# link engine.ear from jboss_home to src dir (installed by rpms)
ln -sf $JBOSS_EAR_PATH $EAR_SRC_PATH || die "Failed linking $EAR_SRC_PATH to $JBOSS_EAR_PATH\n"

# Copy files
echo "# Generating CA in working directory..."
cp -a $1 $2/

# Go to scripts location
cd $WORKDIR

# Run standard install CA script
./installCA.sh $SUBJECT $COUNTRY $ORG $ALIAS $PASS $DATE $WORKDIR $CA_SUBJECT

#cleanup
EAR_NAME=$(basename $JBOSS_EAR_PATH)
if [ -L "$EAR_SRC_PATH/$EAR_NAME" ]; then
    unlink "$EAR_SRC_PATH/$EAR_NAME" || die "Failed removing link $EAR_SRC_PATH/$EAR_NAME"
fi

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
echo "}}} 1.2. keystoreUrl to $WORKDIR/keys/engine.p12"
echo "}}} 1.3. TruststoreUrl to $WORKDIR/.truststore"
echo "}}} 1.4. Fix SignScriptName to sh(/bat)"
echo "}}} 1.5. PublicURLPath to [jboss dir]/standalone/deployments/engine.ear/root.war/"
echo " "
echo "}}} 2. DB updates (vdc_options table):"
echo "}}} 2.1. CertificateFileName $WORKDIR/certs/engine.cer"
echo "}}} 2.2. CABaseDirectory to $WORKDIR"
echo "}}} 2.3. CAEngineKey to $WORKDIR/private/ca.pem"
echo " "

exit 0
