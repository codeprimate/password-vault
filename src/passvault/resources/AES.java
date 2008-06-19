/*
 * AES.java
 * 
 * Created on Oct 16, 2007, 10:07:49 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 *
 * @author patrick
 */
public class AES {
    
    private Cipher cipher = null;
    private SecretKeySpec skeySpec = null;
    private IvParameterSpec ivSpec = null;
    
    public static void AES() { }
    
    public void init(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes("UTF-8"), 0, password.length());
            byte[] rawKey = md.digest();
            skeySpec = new SecretKeySpec(rawKey, "AES");
            ivSpec = new IvParameterSpec(rawKey);
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String encrypt(String text) {
        String encrypted_text = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(text.getBytes());
            encrypted_text = Base64.encode(encrypted);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encrypted_text;
    }
    
    public String decrypt(String text) {
        String decrypted_text = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.decode(text));
            decrypted_text = new String(decrypted,"UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return decrypted_text;
    }
}
