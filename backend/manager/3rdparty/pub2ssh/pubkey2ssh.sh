#!/bin/sh

gcc -I/usr/include/openssl  -L/usr/lib64/  -lcrypto src/pubkey2ssh.c -o bin/pubkey2ssh

exit $?
