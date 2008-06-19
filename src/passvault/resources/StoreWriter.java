/*
 * StoreWriter.java
 * 
 * Created on Sep 30, 2007, 1:55:48 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

/**
 *
 * @author patrick
 */
public class StoreWriter {

    public String[] options = null;
    public String filename = null;
    public String fileformat = null;
    public PasswordStore pws = null;
    public String password = null;

    public StoreWriter(String[] in_options,PasswordStore in_pws) {
        options = in_options;
        filename = options[0];
        fileformat = options[1];
        password = options[2];
        pws = in_pws;
    }

    public boolean valid_data() {
        // Stub to validate data
        return true;
    }

    public boolean write_store_data() {
        boolean result = false;
        if (options[1].equals("xml")) {
            result = write_xml(options[0]);
        }
        return result;
    }
    
    private boolean encrypt_file(String filename) {
        ReadFile file_reader = new ReadFile(filename);
        String unencrypted_text = file_reader.read();
        
        AES aes = new AES();
        aes.init(password);
        String encrypted_text = aes.encrypt(unencrypted_text);
        
        ReadFile file_writer = new ReadFile(filename);
        file_writer.write(encrypted_text);
        System.out.println(encrypted_text);
        return true;
    }

    private boolean write_xml(String in_filename) {
        boolean result = false;
        XMLWriter xml = new XMLWriter();
        String output_xml = null;
        xml.write_xml(in_filename, pws);
        encrypt_file(in_filename);
        return result;
    }
}
