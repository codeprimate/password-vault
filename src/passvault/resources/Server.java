/*
 * Server.java
 * 
 * Created on Sep 23, 2007, 4:33:14 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

/**
 *
 * @author patrick
 */


public class Server implements Comparable {

    public String name = "";
    public String address = "";
    public String notes = "";
    
    public static void Server() { }
    
    public void set_info(String givenName, String givenAddr, String givenNotes) {
        name = givenName;
        address = givenAddr;
        notes = givenNotes;
    }
    
    @Override
    public String toString(){
        return name;
    }

    public int compareTo(Object arg0) {
//        Server foo = (Server)arg0;
        Server server = (Server)arg0;
        String server_name = server.name; 
        return name.compareToIgnoreCase(server_name);
    }


    
    
    
}
