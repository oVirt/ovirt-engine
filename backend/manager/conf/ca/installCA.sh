#!/bin/sh

die () {
    printf >&2 "$@"
    exit 1
}

usage () {
    DATE=`date --date "now -1 days" +"%y%m%d%H%M%S%z"`
    echo "Usage:"
    echo "  $0 [Subject] [Country] [Organization] [Alias] [Password] [ANSI Start Date] [Working Directory] [CA Subject]"
    echo "e.g.:"
    echo "  $0 hostname.fqdn US oVirt engine NoSoup4U $DATE"

    exit 1
}

# Check Args
[ "$#" -ge 3 ] || usage

# Set var's
SUBJECT=$1
COUNTRY=$2
ORG=$3
ALIAS=$4
PASS=$5
DATE=$6
WORKDIR=$7
CA_SUBJECT=$8
[ -d "$7" ] || die "Directory $7 does not exists"

echo " "
echo "} Creating CA..."

# Move to scripts location
cd $WORKDIR

# Create CA
./CreateCA.sh $COUNTRY "$ORG" "CA-$CA_SUBJECT" "$DATE"
[ $? == 0 ] || die "CreateCA.sh exited with errors"
[ -s private/ca.pem ] || die "file private/ca.pem does not exist!"
[ -s requests/ca.csr ] || die "file requests/ca.csr does not exist!"
[ -s ca.pem ] || die "file ca.pem does not exist!"

# Create KeyStore
echo " "
echo "} Creating KeyStore..."
./CreateKStore.sh $WORKDIR $COUNTRY "$ORG" "$SUBJECT" $PASS $PASS
[ -s ./.keystore ] || die "file ./.keystore does not exist!"

# Convert pem to der
echo " "
echo "}} Converting formats..."
openssl x509 -in ca.pem -out certs/ca.der -outform DER

# Import CA into keystore 
echo " "
echo "> Importing CA certificate..."
# Generate truststore
keytool -import -noprompt -trustcacerts -alias cacert -keypass $PASS -file certs/ca.der -keystore ./.truststore -storepass $PASS
# Import to keystore as well
./importToKeyStore.sh $WORKDIR/.keystore cacert certs/ca.der $PASS

echo " "
echo "} Creating client certificate for oVirt..."

# Create certificate request
echo " "
echo "}} Creating certificate request..."
./CreateReq.sh 1 $COUNTRY "$ORG" "$SUBJECT" engine $PASS $ALIAS

# Sign request
echo " "
echo "}} Signing certificate request..."
./SignReq.sh engine.req engine.cer 1800 `pwd` "$DATE" $PASS
[ -s certs/engine.cer ] || die "file certs/engine.cer does not exist!"

echo " "
echo "}} Converting formats..."
openssl x509 -in certs/engine.cer -out certs/engine.der -outform DER

# Import oVirt certificate into keystore
echo " "
echo "} Importing oVirt certificate..."
./importToKeyStore.sh $WORKDIR/.keystore $ALIAS certs/engine.der $PASS

# Export oVirt key as ssh key
echo " "
echo "} Exporting oVirt key as SSH..."
./store-utils.sh -pubkey2ssh "$WORKDIR/.keystore" "$PASS" "$ALIAS" > $WORKDIR/keys/engine.ssh.key.txt

exit 0

