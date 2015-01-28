#!/usr/bin/python
#
# rhel-6 does not have valid ssh-keygen so we do this
# our selves.
#
import base64
import optparse
import re
import struct
import sys


from M2Crypto import EVP
from M2Crypto import RSA


def _getSSHPublicKeyRaw(key):
    ALGO = 'ssh-rsa'
    return {
        'algo': ALGO,
        'blob': (
            struct.pack('!l', len(ALGO)) + ALGO.encode('ascii') +
            key.pub()[0] +
            key.pub()[1]
        ),
    }


def _getSSHPublicKey(key):
    sshkey = _getSSHPublicKeyRaw(key)
    return '%s %s' % (sshkey['algo'], base64.b64encode(sshkey['blob']))


def _getSSHPublicKeyFingerprint(key, f):
    sshkey = _getSSHPublicKeyRaw(key)
    md5 = EVP.MessageDigest('md5')
    md5.update(sshkey['blob'])
    return '%s %s %s (%s)' % (
        len(key),
        re.sub(r'(..)', r':\1', base64.b16encode(md5.digest()))[1:].lower(),
        f,
        sshkey['algo'].replace('ssh-', '').upper(),
    )


def _getPublicKey(f):
    return RSA.load_pub_key(f)


def main():
    ret = 1
    try:
        parser = optparse.OptionParser()
        parser.add_option('-i', action='store_true', default=False)
        parser.add_option('-l', action='store_true', default=False)
        parser.add_option('-m')
        parser.add_option('-f')
        options, args = parser.parse_args(sys.argv)
        if options.i:
            if not options.m or not options.f:
                raise RuntimeError('Missing -m or -f')
            if options.m != 'PKCS8':
                raise RuntimeError('Unsupported format')
            print(_getSSHPublicKey(_getPublicKey(options.f)))
            ret = 0
        elif options.l:
            if not options.f:
                raise RuntimeError('Missing -m or -f')
            print(
                _getSSHPublicKeyFingerprint(
                    _getPublicKey(options.f),
                    options.f,
                )
            )
            ret = 0
        else:
            raise RuntimeError('Unsupported operation')
    except Exception as e:
        # import traceback
        # traceback.print_exc()
        sys.stderr.write('FATAL: %s\n' % (e,))

    return ret


if __name__ == '__main__':
    sys.exit(main())


# vim: expandtab tabstop=4 shiftwidth=4
