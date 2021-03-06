package net.minecraft.src;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptManager {
    /**
     * Generates RSA KeyPair
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator var0 = KeyPairGenerator.getInstance("RSA");
            var0.initialize(1024);
            return var0.generateKeyPair();
        } catch (NoSuchAlgorithmException var1) {
            var1.printStackTrace();
            System.err.println("Key pair generation failed!");
            return null;
        }
    }

    /**
     * Compute a serverId hash for use by sendSessionRequest()
     */
    public static byte[] getServerIdHash(String par0Str, PublicKey par1PublicKey, SecretKey par2SecretKey) {
        try {
            return digestOperation("SHA-1", par0Str.getBytes("ISO_8859_1"), par2SecretKey.getEncoded(), par1PublicKey.getEncoded());
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    /**
     * Compute a message digest on arbitrary byte[] data
     */
    private static byte[] digestOperation(String par0Str, byte[]... par1ArrayOfByte) {
        try {
            MessageDigest var2 = MessageDigest.getInstance(par0Str);

            for (byte[] var6 : par1ArrayOfByte) {
                var2.update(var6);
            }

            return var2.digest();
        } catch (NoSuchAlgorithmException var7) {
            var7.printStackTrace();
            return null;
        }
    }

    /**
     * Create a new PublicKey from encoded X.509 data
     */
    public static PublicKey decodePublicKey(byte[] par0ArrayOfByte) {
        try {
            X509EncodedKeySpec var1 = new X509EncodedKeySpec(par0ArrayOfByte);
            KeyFactory var2 = KeyFactory.getInstance("RSA");
            return var2.generatePublic(var1);
        } catch (NoSuchAlgorithmException var3) {
            var3.printStackTrace();
        } catch (InvalidKeySpecException var4) {
            var4.printStackTrace();
        }

        System.err.println("Public key reconstitute failed!");
        return null;
    }

    /**
     * Decrypt shared secret AES key using RSA private key
     */
    public static SecretKey decryptSharedKey(PrivateKey par0PrivateKey, byte[] par1ArrayOfByte) {
        return new SecretKeySpec(decryptData(par0PrivateKey, par1ArrayOfByte), "AES");
    }

    /**
     * Decrypt byte[] data with RSA private key
     */
    public static byte[] decryptData(Key par0Key, byte[] par1ArrayOfByte) {
        return cipherOperation(2, par0Key, par1ArrayOfByte);
    }

    /**
     * Encrypt or decrypt byte[] data using the specified key
     */
    private static byte[] cipherOperation(int par0, Key par1Key, byte[] par2ArrayOfByte) {
        try {
            return createTheCipherInstance(par0, par1Key.getAlgorithm(), par1Key).doFinal(par2ArrayOfByte);
        } catch (IllegalBlockSizeException var4) {
            var4.printStackTrace();
        } catch (BadPaddingException var5) {
            var5.printStackTrace();
        }

        System.err.println("Cipher data failed!");
        return null;
    }

    /**
     * Creates the Cipher Instance.
     */
    private static Cipher createTheCipherInstance(int par0, String par1Str, Key par2Key) {
        try {
            Cipher var3 = Cipher.getInstance(par1Str);
            var3.init(par0, par2Key);
            return var3;
        } catch (InvalidKeyException var4) {
            var4.printStackTrace();
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
        } catch (NoSuchPaddingException var6) {
            var6.printStackTrace();
        }

        System.err.println("Cipher creation failed!");
        return null;
    }

    /**
     * Create a new BufferedBlockCipher instance
     */
    private static BufferedBlockCipher createBufferedBlockCipher(boolean par0, Key par1Key) {
        BufferedBlockCipher var2 = new BufferedBlockCipher(new CFBBlockCipher(new AESFastEngine(), 8));
        var2.init(par0, new ParametersWithIV(new KeyParameter(par1Key.getEncoded()), par1Key.getEncoded(), 0, 16));
        return var2;
    }

    public static OutputStream encryptOuputStream(SecretKey par0SecretKey, OutputStream par1OutputStream) {
        return new CipherOutputStream(par1OutputStream, createBufferedBlockCipher(true, par0SecretKey));
    }

    public static InputStream decryptInputStream(SecretKey par0SecretKey, InputStream par1InputStream) {
        return new CipherInputStream(par1InputStream, createBufferedBlockCipher(false, par0SecretKey));
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}
