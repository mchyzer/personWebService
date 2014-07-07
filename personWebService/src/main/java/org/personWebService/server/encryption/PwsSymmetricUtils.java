/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.encryption;

import java.security.SecureRandom;

import org.apache.commons.lang.StringUtils;


/**
 * call command line like:  /opt/appserv/tomcat/apps/twoFactor/java/bin/java -cp "/opt/appserv/tomcat/apps/twoFactorWs/webapps/twoFactorWs/WEB-INF/classes:/opt/appserv/tomcat/apps/twoFactorWs/webapps/twoFactorWs/WEB-INF/lib/*:/opt/appserv/common/tomcat6_32_ds_base/lib/servlet-api.jar" org.personWebService.server.encryption.PwsSymmetricUtils
 */
public class PwsSymmetricUtils {

  /**
   * 20 special chars
   */
  private static final char[] SPECIAL_CHARS = new char[]{'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']', '{', '}', ';', ':'};
  
  /**
   * Encrypt a sample message using AES in CBC mode with an IV.
   * 
   * @param args not used
   * @throws Exception if the algorithm, key, iv or any other parameter is
   *             invalid.
   */
  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      usage();
    }

    if (args.length == 3 && StringUtils.equals(args[0], "encryptCbc")) {
      
      String secret = args[1];
      String text = args[2];
      
      String encrypted = new PwsSymmetricEncryptAesCbcPkcs5Padding().encrypt(secret, text);
      
      System.out.println(encrypted);
          
      return;
      
    } else if (args.length == 3 && StringUtils.equals(args[0], "decryptCbc")) {
      
      String secret = args[1];
      String encryptedText = args[2];
      
      String decrypted = new PwsSymmetricEncryptAesCbcPkcs5Padding().decrypt(secret, encryptedText);
      
      System.out.println(decrypted);
      return;
      
    } else     if (args.length == 2 && StringUtils.equals(args[0], "encryptKeyCbc")) {
      
      String text = args[1];
      
      String encrypted = EncryptionKey.encrypt(text, new PwsSymmetricEncryptAesCbcPkcs5Padding());
      
      System.out.println(encrypted);
          
      return;
      
    } else if (args.length == 2 && StringUtils.equals(args[0], "decryptKeyCbc")) {
      
      String encryptedText = args[1];
      
      String decrypted = EncryptionKey.decrypt(encryptedText, new PwsSymmetricEncryptAesCbcPkcs5Padding());
      
      System.out.println(decrypted);
      return;
            
    } else if (args.length == 1 && StringUtils.equals(args[0], "generatePass")) {
      
      char[] pass = new char[40];
      for (int i=0;i<pass.length;i++) {
        
        int theInt = new SecureRandom().nextInt(26+26+10+20);
        
        if (theInt < 26) {
          pass[i] = (char)('a' + theInt);
        } else {
          theInt -= 26;
          if (theInt < 26) {
            pass[i] = (char)('A' + theInt);
          } else {
            theInt -= 26;
            if (theInt < 10) {
              pass[i] = (char)('0' + theInt);
            } else {
              theInt -= 10;
              pass[i] = SPECIAL_CHARS[theInt];
            }
          }
        }
      }
      
      System.out.println("Secure pass is: " + new String(pass));
      return;
      
    }

    usage();

  }


  /**
   * 
   */
  private static void usage() {

    System.out.println("Call with 'generatePass' to generate a secure password for config file");
    System.out.println("Call with 'encryptCbc secret text' encrypt something with cbc");
    System.out.println("Call with 'decryptCbc secret encryptedText' decrypt something with cbc");
    System.out.println("Call with 'encryptKeyCbc text' decrypt something with cbc encrypt key");
    System.out.println("Call with 'decryptKeyCbc encryptedText' decrypt something with cbc encrypt key");
    System.exit(1);
  }
  
}
