/*
 * An implementation of convertion from OpenSSL to OpenSSH public key format
 *
 * Copyright (c) 2008 Mounir IDRASSI <mounir.idrassi@idrix.fr>. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */


#include <memory.h>
#include <openssl/bio.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/err.h>


static unsigned char pSshHeader[11] = { 0x00, 0x00, 0x00, 0x07, 0x73, 0x73, 0x68, 0x2D, 0x72, 0x73, 0x61};

static int SshEncodeBuffer(unsigned char *pEncoding, int bufferLen, unsigned char* pBuffer)
{
   int adjustedLen = bufferLen, index;
   if (*pBuffer & 0x80)
   {
      adjustedLen++;
      pEncoding[4] = 0;
      index = 5;
   }
   else
   {
      index = 4;
   }
   pEncoding[0] = (unsigned char) (adjustedLen >> 24);
   pEncoding[1] = (unsigned char) (adjustedLen >> 16);
   pEncoding[2] = (unsigned char) (adjustedLen >>  8);
   pEncoding[3] = (unsigned char) (adjustedLen      );
   memcpy(&pEncoding[index], pBuffer, bufferLen);
   return index + bufferLen;
}

int main(int argc, char**  argv)
{
   int iRet = 0;
   int nLen = 0, eLen = 0;
   int encodingLength = 0;
   int index = 0;
   unsigned char *nBytes = NULL, *eBytes = NULL;
   unsigned char* pEncoding = NULL;
   FILE* pFile = NULL;
   EVP_PKEY *pPubKey = NULL;
   RSA* pRsa = NULL;
   BIO *bio, *b64;

   ERR_load_crypto_strings();
   OpenSSL_add_all_algorithms();

   if (argc != 3)
   {
      printf("usage: %s public_key_file_name ssh_key_description\n", argv[0]);
      iRet = 1;
      goto error;
   }

   pFile = fopen(argv[1], "rt");
   if (!pFile)
   {
      printf("Failed to open the given file\n");
      iRet = 2;
      goto error;
   }

   pPubKey = PEM_read_PUBKEY(pFile, NULL, NULL, NULL);
   if (!pPubKey)
   {
      printf("Unable to decode public key from the given file: %s\n", ERR_error_string(ERR_get_error(), NULL));
      iRet = 3;
      goto error;
   }

   if (EVP_PKEY_type(pPubKey->type) != EVP_PKEY_RSA)
   {
      printf("Only RSA public keys are currently supported\n");
      iRet = 4;
      goto error;
   }

   pRsa = EVP_PKEY_get1_RSA(pPubKey);
   if (!pRsa)
   {
      printf("Failed to get RSA public key : %s\n", ERR_error_string(ERR_get_error(), NULL));
      iRet = 5;
      goto error;
   }

   // reading the modulus
   nLen = BN_num_bytes(pRsa->n);
   nBytes = (unsigned char*) malloc(nLen);
   BN_bn2bin(pRsa->n, nBytes);

   // reading the public exponent
   eLen = BN_num_bytes(pRsa->e);
   eBytes = (unsigned char*) malloc(eLen);
   BN_bn2bin(pRsa->e, eBytes);

   encodingLength = 11 + 4 + eLen + 4 + nLen;
   // correct depending on the MSB of e and N
   if (eBytes[0] & 0x80)
      encodingLength++;
   if (nBytes[0] & 0x80)
      encodingLength++;

   pEncoding = (unsigned char*) malloc(encodingLength);
   memcpy(pEncoding, pSshHeader, 11);

   index = SshEncodeBuffer(&pEncoding[11], eLen, eBytes);
   index = SshEncodeBuffer(&pEncoding[11 + index], nLen, nBytes);

   b64 = BIO_new(BIO_f_base64());
   BIO_set_flags(b64, BIO_FLAGS_BASE64_NO_NL);
   bio = BIO_new_fp(stdout, BIO_NOCLOSE);
   BIO_printf(bio, "ssh-rsa ");
   bio = BIO_push(b64, bio);
   BIO_write(bio, pEncoding, encodingLength);
   BIO_flush(bio);
   bio = BIO_pop(b64);
   BIO_printf(bio, " %s\n", argv[2]);
   BIO_flush(bio);
   BIO_free_all(bio);
   BIO_free(b64);

error:
   if (pFile)
      fclose(pFile);
   if (pRsa)
      RSA_free(pRsa);
   if (pPubKey)
      EVP_PKEY_free(pPubKey);
   if (nBytes)
      free(nBytes);
   if (eBytes)
      free(eBytes);
   if (pEncoding)
      free(pEncoding);

   EVP_cleanup();
   ERR_free_strings();
   return iRet;
}
