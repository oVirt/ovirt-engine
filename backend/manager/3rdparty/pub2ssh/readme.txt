The purpose of this project is to convert a PEM based public key
into OpenSSH format. This project includes one source file, which
can be conpiled for Linux using the shell script or Windows using
the batch script.

In order to compile, you'll need to have openssl libraries installed,
and for windows you'll need Visual Stusio 2005 as well.

In order to extract a public key from Java's keystore or encrypted
pem file, you can use one of the given examples below.

1. Extract public key from private:

openssl rsa -in /tmp/ca/keys/ca.pem -pubout -out id_rsa.foropenssl
openssl rsa -in id_rsa.foropenssl -pubout > /tmp/test.pub

output:
-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFE/YnZIzhY9Z6VGR50b2v/E4k
fjH4gKkgGPhtPRpnoQPFUV5qI1vm1nsMN8caUW1IYmh6HMecGRLuFPrVPPUrdyYE
lrjujcoCuKrkp5+2fpUAGnpDZZ3nZC2Zhz0hA9xNN0jjejHT//HprzrIbUx4MU2R
sQhZVWdlrg2L5h9uiwIDAQAB
-----END PUBLIC KEY-----


2. Extract public key from keystore.

keytool -rfc -export -keystore ca/.keystore -alias mycert -file aaa.txt
openssl x509 -noout -in aaa.txt -pubkey > aaa.pub