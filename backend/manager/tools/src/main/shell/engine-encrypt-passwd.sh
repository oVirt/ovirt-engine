#!/bin/sh

JBOSS_HOME="${JBOSS_HOME:-/usr/share/jboss-as}"
export CLASSPATH=""
export JAVA_MODULEPATH="${JBOSS_HOME}/modules"

# Load the prolog, not during installation
[ -z "${ENGINE_CONFIG_IGNORE}" ] && \
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
  -Djboss.modules.write-indexes=false \
  -dependencies org.picketbox \
  -class org.picketbox.datasource.security.SecureIdentityLoginModule \
  "$@"
