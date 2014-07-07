/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.encryption;

import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;


/**
 *
 */
public class PwsSymmetricLegacyAesEncryption implements PwsSymmetricEncryption {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    String key = "key";
    
    String legacyEncrypted = new PwsSymmetricLegacyAesEncryption().encrypt(key, "abc");
    System.out.println("Legacy(" + key + ") encrypted: " + legacyEncrypted);
    
    String legacyDecrypted = new PwsSymmetricLegacyAesEncryption().decrypt(key, legacyEncrypted);
    System.out.println("Legacy(" + key + ") decrypted: " + legacyDecrypted);
    
    String cbcEncrypted = new PwsSymmetricEncryptAesCbcPkcs5Padding().encrypt(key, "abc");
    System.out.println("AesEcbPkcs5Padding(" + key + ") encrypted: " + cbcEncrypted);
    
    String cbcDecrypted = new PwsSymmetricEncryptAesCbcPkcs5Padding().decrypt(key, cbcEncrypted);
    System.out.println("AesEcbPkcs5Padding(" + key + ") decrypted: " + cbcDecrypted);
    
    
  }
  
  /**
   * @see org.personWebService.server.encryption.PwsSymmetricEncryption#encrypt(java.lang.String, java.lang.String)
   */
  public String encrypt(String key, String data) {
    return new Crypto(key).encrypt(data);
  }

  /**
   * @see org.personWebService.server.encryption.PwsSymmetricEncryption#decrypt(java.lang.String, java.lang.String)
   */
  public String decrypt(String key, String encryptedData) {
    return new Crypto(key).decrypt(encryptedData);
  }

}
