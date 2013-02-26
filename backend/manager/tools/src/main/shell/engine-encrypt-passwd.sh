#!/bin/sh

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

die () {
    printf >&2 "$@"
    exit 1
}

usage () {
        printf "engine-encrypt-passwd.sh - Generate a an encrypted password from the plain-text password given.\n"
        printf "Usage: \n"
        printf "engine-encrypt-passwd.sh [Plain-Text-Password]\n"
        printf "Where:\n"
        printf "Plain-Text-Password = The password to encrypt in plain text.\n"
        return 0
}

if [ ! "$#" -eq 1 ]; then
    usage
    die "Error: wrong argument number: $#.\n"
fi

exec "${JAVA_HOME}/bin/java" \
  -jar "${JBOSS_HOME}/jboss-modules.jar" \
  -dependencies org.picketbox \
  -class org.picketbox.datasource.security.SecureIdentityLoginModule \
  "$@"
