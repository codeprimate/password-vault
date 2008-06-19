/*
 * Credential.java
 * 
 * Created on Sep 23, 2007, 5:38:06 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

import java.util.Comparator;

/**
 *
 * @author patrick
 */
public class Credential implements Comparable {
    
    public String description = "";
    public Server server = new Server();
    public String address = "";
    public Person person = new Person();
    public String login = "";
    public String password = "";
    public String notes ="";
    
    public static void Credential() {}
    
    public void set_info(String givenDesc, String givenServer, 
            String givenAddress, String givenPerson, String givenLogin,
            String givenPassword, String givenNotes) {
        
    }
    
    
    
    
    public static String genPassword(int n) {
        char[] pw = new char[n];
        int c  = 'A';
        int  r1 = 0;
        for (int i=0; i < n; i++)
        {
          r1 = (int)(Math.random() * 3);
          switch(r1) {
            case 0: c = '0' +  (int)(Math.random() * 10); break;
            case 1: c = 'a' +  (int)(Math.random() * 26); break;
            case 2: c = 'A' +  (int)(Math.random() * 26); break;
          }
          pw[i] = (char)c;
        }
        return new String(pw);
    }
    
    @Override
    public String toString(){
        return description;
    }

    public int compareTo(Object arg0) {
        Credential credential = (Credential) arg0;
        String cred_name = credential.description;
        return description.compareToIgnoreCase(cred_name);
    }

}


