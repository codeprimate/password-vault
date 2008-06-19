/*
 * SearchMatch.java
 * 
 * Created on Oct 8, 2007, 12:42:47 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

import java.util.ArrayList;

/**
 *
 * @author patrick
 */
public class PasswordStoreSearch {
    
    public PasswordStore store = null;
    public Object search_object = null;
    public int search_object_type = 0;  /* 1 => Credential, 2 => Server, 3 => Person */
    public String search_string = null;
    public ArrayList<Credential> credentials = new ArrayList<Credential>();
    public ArrayList<Server> servers = new ArrayList<Server>();
    public ArrayList<Person> people = new ArrayList<Person>();
    
    public PasswordStoreSearch(PasswordStore pws, String the_text) {
        store = pws;
        search_string = the_text;
        run_text_search();
    }

    public PasswordStoreSearch(PasswordStore pws, Object the_object) {
        store = pws;
        search_object = the_object;
        search_object_type = identify_object(the_object);
        run_object_search();
    }
    
    
    private boolean run_object_search() {
        switch (search_object_type) {
        case 1:
            Credential cred = (Credential)search_object;
            servers = get_servers(cred.server.name);
            people = get_people(cred.person.name);
            credentials.add(cred);
            break;
        case 2: 
            Server server = (Server)search_object;
            servers.add(server);
            credentials = get_credentials_by_server(server.name);
            people = get_people_by_server(server.name);
            break;
        case 3:
            Person person = (Person)search_object;
            people.add(person);
            credentials = get_credentials_by_person(person.name);
            servers = get_servers_by_person(person.name);
            break;
        }
        remove_dupes();
        return true;
    }
    
    private boolean run_text_search() {
        
        return true;
    }
    
    private void remove_dupes() {
        remove_server_dupes();
        remove_people_dupes();
        remove_credential_dupes();
    }
    
    
    private void remove_server_dupes(){
        ArrayList <Server> clean_servers = new ArrayList <Server>();
        for (Server server : servers) {
            if ( ! clean_servers.contains(server)) {
                clean_servers.add(server);
            }
        }
        servers = clean_servers;
    }
    
    private void remove_people_dupes() {
        ArrayList <Person> clean_people = new ArrayList <Person>();
        for (Person server : people) {
            if ( ! clean_people.contains(server)) {
                clean_people.add(server);
            }
        }
        people = clean_people;
    }
    
    private void remove_credential_dupes() {
        ArrayList <Credential> clean_credentials = new ArrayList <Credential>();
        for (Credential credential : credentials) {
            if ( ! clean_credentials.contains(credential)) {
                clean_credentials.add(credential);
            }
        }
        credentials = clean_credentials;   
    }
    
    private ArrayList<Server> get_servers(String servername) {
        ArrayList<Server> results = new ArrayList<Server>();
        for (Server server : store.servers) {
            if (server.name.equals(servername)) {
                results.add(server);
            }
        }
        return results;
    }
    
    private ArrayList<Server> get_servers_by_person(String personname) {
        ArrayList<Server> results = new ArrayList<Server>();
        for (Credential cred : get_credentials_by_person(personname) ) {
            results.add(cred.server);
        }
        return results;
    }
    
    private ArrayList<Credential> get_credentials_by_server(String servername) {
        ArrayList<Credential> results = new ArrayList<Credential>();
        for (Credential credential : store.credentials) {
            if (credential.server.name.equals(servername)) {
                results.add(credential);
            }
        }
        return results;
    }
    
    private ArrayList<Credential> get_credentials_by_person(String personname) {
        ArrayList<Credential> results = new ArrayList<Credential>();
        for (Credential credential : store.credentials) {
            if (credential.person.name.equals(personname)) {
                results.add(credential);
            }
        }
        return results;
    }
    
    private ArrayList<Person> get_people_by_server(String servername) {
        ArrayList<Person> results = new ArrayList<Person>();
        for (Credential credential : store.credentials) {
            if (credential.server.name.equals(servername)) {
                results.add(credential.person);
            }
        }
        return results;
    }
    
    private ArrayList<Person> get_people(String personname) {
        ArrayList<Person> results = new ArrayList<Person>();
        for (Person person : store.people) {
            if (person.name.equals(personname)) {
                results.add(person);
            }
        }
        return results;
    }
    
    private int identify_object(Object the_object) {
        Class object_class = the_object.getClass();
        String classname = object_class.getSimpleName();
        int class_id = 0;
        if (classname.equals("Credential")) { class_id = 1; }
        if (classname.equals("Server")) { class_id = 2; }
        if (classname.equals("Person")) { class_id = 3; }
        return class_id;
    }
    
    
}
