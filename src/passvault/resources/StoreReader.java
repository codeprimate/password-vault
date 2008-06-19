/*
 * StoreReader.java
 *
 * Created on Sep 27, 2007, 10:46:54 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

/**
 *
 * @author patrick
 */
public class StoreReader {

    public String[] options = null;
    public String filename = null;
    public String fileformat = null;
    public String password = null;

    public StoreReader(String[] in_options) {
        options = in_options;
        filename = options[0];
        fileformat = options[1];
        password = options[2];
    }

    public boolean valid_data() {
        // Stub to validate data
        return true;
    }

    public PasswordStore read_store_data() {
        PasswordStore pws = new PasswordStore();
        if (options[1].equals("xml")) {
            pws = read_xml(options[0]);
        }
        return pws;
    }
    
    private String decrypt_xml(String encrypted_text) {
        AES aes = new AES();
        aes.init(password);
        String decrypted_text = aes.decrypt(encrypted_text);
        return decrypted_text;
    }

    private PasswordStore read_xml(String in_filename) {
        PasswordStore pws = null;
        XMLReader xml = new XMLReader();
        ReadFile file_reader = new ReadFile(in_filename);
        String unencrypted_text = file_reader.read().toString();
        System.out.println(unencrypted_text);
        String decrypted_text = null; 
        if (unencrypted_text.startsWith("<?xml")) {
            System.out.println("Store is not encrypted.");
            pws = xml.read_xml(unencrypted_text);
        } else {
            System.out.println("Store is encrypted.");
            decrypted_text =  decrypt_xml(unencrypted_text);
            pws = xml.read_xml(decrypted_text);
        }
        return pws;
    }
}