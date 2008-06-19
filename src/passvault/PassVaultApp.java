/*
 * PassVaultApp.java
 */

package passvault;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import passvault.resources.PasswordStore;

/**
 * The main class of the application.
 */
public class PassVaultApp extends SingleFrameApplication {
    
    public PasswordStore passwordStore = new PasswordStore();
    public String [] store_file = {null, null};
    public String password = "test_password";
            

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
//       String filename = ClassLoader.getSystemResource("lib/passwordstore.xml").toString();
//       System.out.println(filename);
//       String [] options = {filename, "xml"};
//       StoreReader sr = new StoreReader(options);
//       passwordStore = sr.read_store_data();
       passwordStore = load_password_data(); 
       show(new PassVaultView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of PassVaultApp
     */
    public static PassVaultApp getApplication() {
        return Application.getInstance(PassVaultApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(PassVaultApp.class, args);
    }
    
    public PasswordStore load_password_data() {
        PasswordStore pwstore = new PasswordStore();
        pwstore.set_defaults();
        return pwstore;
    }
    
    public void load_password_data(PasswordStore new_pws) {
//        String [] options = {"lib/passwordstore.xml","xml"};
//        StoreReader sr = new StoreReader(options);
//        passwordStore = sr.read_store_data();
        passwordStore = new_pws;
    }
    
}
