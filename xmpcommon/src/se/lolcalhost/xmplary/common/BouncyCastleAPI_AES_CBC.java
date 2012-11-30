package se.lolcalhost.xmplary.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Copyright (C) 2011 www.itcsolutions.eu
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1, or (at your
 * option) any later version.
 *
 * This file is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 *
 * @author Catalin - www.itcsolutions.eu
 * @version 2011
 *
 * From http://www.itcsolutions.eu/2011/08/24/how-to-encrypt-decrypt-files-in-java-with-aes-in-cbc-mode-using-bouncy-castle-api-and-netbeans-or-eclipse/
 * http://www.itcsolutions.eu/wp-content/uploads/2011/08/BouncyCastleProvider_AES_CBC.java.txt
 * 
 * @author sx00042
 *
 */
public class BouncyCastleAPI_AES_CBC {
    PaddedBufferedBlockCipher encryptCipher = null;
    PaddedBufferedBlockCipher decryptCipher = null;
 
    // Buffer used to transport the bytes from one stream to another
    byte[] buf = new byte[16];              //input buffer
    byte[] obuf = new byte[512];            //output buffer
    // The key
    byte[] key = null;
    // The initialization vector needed by the CBC mode
    byte[] IV =  null;
 
    // The default block size
    public static int blockSize = 16;
 
    public BouncyCastleAPI_AES_CBC(){
        //default 192 bit key
        key = "SECRET_1SECRET_2SECRET_3".getBytes();
        //default IV vector with all bytes to 0
        IV = new byte[blockSize];
    }
    public BouncyCastleAPI_AES_CBC(byte[] keyBytes){
        //get the key
        key = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0 , key, 0, keyBytes.length);
 
        //default IV vector with all bytes to 0
        IV = new byte[blockSize];
    }
 
    public BouncyCastleAPI_AES_CBC(byte[] keyBytes, byte[] iv){
        //get the key
        key = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0 , key, 0, keyBytes.length);
 
        //get the IV
        IV = new byte[blockSize];
        System.arraycopy(iv, 0 , IV, 0, iv.length);
    }
 
    public void InitCiphers(){
        //create the ciphers
        // AES block cipher in CBC mode with padding
        encryptCipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new AESEngine()));
 
        decryptCipher =  new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new AESEngine()));
 
        //create the IV parameter
        ParametersWithIV parameterIV =
                new ParametersWithIV(new KeyParameter(key),IV);
 
        encryptCipher.init(true, parameterIV);
        decryptCipher.init(false, parameterIV);
    }
    
    public void CBCEncrypt(InputStream in, OutputStream out)
		throws ShortBufferException, 
		        IllegalBlockSizeException,
		        BadPaddingException,
		        DataLengthException,
		        IllegalStateException,
		        InvalidCipherTextException,
		        IOException
		{
		    // Bytes written to out will be encrypted
		    // Read in the cleartext bytes from in InputStream and
		    //      write them encrypted to out OutputStream
		 
		    //optionaly put the IV at the beggining of the cipher file
		    //out.write(IV, 0, IV.length);
		 
		    int noBytesRead = 0;        //number of bytes read from input
		    int noBytesProcessed = 0;   //number of bytes processed
		 
		    while ((noBytesRead = in.read(buf)) >= 0) {
		        noBytesProcessed =
		                encryptCipher.processBytes(buf, 0, noBytesRead, obuf, 0);
		        out.write(obuf, 0, noBytesProcessed);
		    }
		 
		    noBytesProcessed = encryptCipher.doFinal(obuf, 0);
		    out.write(obuf, 0, noBytesProcessed);
		    out.flush();
		 
		    in.close();
		    out.close();
		}
}