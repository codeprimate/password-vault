/*
 * PasswordStore.java
 * 
 * Created on Sep 23, 2007, 5:20:25 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author patrick
 */
public class PasswordStore {
    public ArrayList<Server> servers = new ArrayList<Server>();
    public ArrayList<Person> people = new ArrayList<Person>();
    public ArrayList<Credential> credentials = new ArrayList<Credential>();
    
    public static void PasswordStore() {
    }
    
    public void set_defaults() {
        add_server("Default");
        add_person("Default");
    }
    
    public boolean test_defaults(){
        set_defaults();
        ArrayList<Server> ts = servers;
        ArrayList<Person> ppl = people;
        if ((ts.size() < 1) && (ppl.size() < 1)) {
            return false;
        }
        else {
            return true;
        }
    }
    
    @SuppressWarnings("unchecked")
    public boolean add_credential(Credential new_credential) {
        boolean success = true;
        credentials.add(new_credential);
        Collections.sort(credentials);
        return success;
    }
    
    @SuppressWarnings("unchecked")
    public boolean add_server(Server server) {
        boolean success = false;
        if (has_server(server.name)) { 
            success = false;
        }
        else {
            servers.add(server);
            Collections.sort(servers);
            success = true;
        }        
        return success;
    }
   
    public Server add_server(String server_name) {
        Server the_server = new Server();        
        the_server.name = server_name;
        add_server(the_server);
        return the_server;
    }
    
    public boolean has_server(String server_name) {
        boolean has_it = false;
        for (int s=0; s < servers.size(); s++) {
            Server server = servers.get(s);
            if (server.name == null) {
                server.name = "";
            }
            if (server.name.equals(server_name)) {
                has_it = true;
                break;
            }
        }
        return has_it;
    }
    
    public Server find_server(String server_name) {
        Server the_server = null;
        for (int s=0; s < servers.size(); s++) {
            Server server = servers.get(s);
            if (server.name == null) {
                server.name = "";
            }
            if (server.name.equals(server_name)) {
                the_server = server;
                break;
            }
        }
        return the_server;
    }
    
    public boolean update_server(Server server) {
       boolean was_updated = true;
       Server old_server = find_server(server.name);
       if (old_server == null) {
           add_server(server);
           was_updated = false;
       }
       else
       {
           old_server.name = server.name;
           old_server.address = server.address;
           old_server.notes = server.notes;
       }
       return was_updated;
    }
   
    @SuppressWarnings("unchecked")
    public boolean update_credential(Credential credential) {
       boolean was_updated = true;
       Credential old_credential = find_credential(credential.description);
       if (old_credential == null) {
           add_credential(credential);
           was_updated = false;
       }
       else
       {
           old_credential.description = credential.description;
           old_credential.server = credential.server;
           old_credential.person = credential.person;
           old_credential.address = credential.address;
           old_credential.login = credential.login;
           old_credential.password = credential.password;
           old_credential.notes = credential.notes;
           Collections.sort(credentials);
       }
       return was_updated;
    }
    
    public Credential find_credential(String credential_name) {
        Credential the_credential = null;
        for (int s=0; s < credentials.size(); s++) {
            Credential credential = credentials.get(s);
            if (credential.description == null) {
                credential.description = "";
            }
            if (credential.description.equals(credential_name)) {
                the_credential = credential;
                break;
            }
        }
        return the_credential;
    }
    
    @SuppressWarnings("unchecked")
    public boolean update_person(Person person) {
       boolean was_updated = true;
       Person old_person = find_person(person.name);
       if (old_person == null) {
           add_person(person);
           was_updated = false;
       }
       else
       {
           old_person.name = person.name;
           old_person.email = person.email;
           old_person.notes = person.notes;
           Collections.sort(people);
       }
       return was_updated;
    }
    
    public Person find_person(String person_name) {
        Person the_person = null;
        for (int s=0; s < people.size(); s++) {
            Person person = people.get(s);
            if (person.name == null) {
                person.name = "";
            }
            if (person.name.equals(person_name)) {
                the_person = person;
                break;
            }
        }
        return the_person;
    }
    
    @SuppressWarnings("unchecked")
    public boolean add_person(Person person) {
        boolean success = false;
        if (has_person(person.name)) { 
            success = false;
        }
        else {
            people.add(person);
            Collections.sort(people);
            success = true;
        }        
        return success;
    }
    
    public Person add_person(String person_name) {
        Person the_person = new Person();        
        the_person.name = person_name;
        add_person(the_person);
        return the_person;
    }
    
    public boolean has_person(String person_name) {
        boolean has_it = false;
        for (int s=0; s < people.size(); s++) {
            Person person = people.get(s);
            if (person.name == null) {
                person.name = "";
            }
            if (person.name.equals(person_name)) {
                has_it = true;
                break;
            }
        }
        return has_it;
    }
    
    public String[] server_names() {
        int server_ct = servers.size() - 1;
        ArrayList<String> names = new ArrayList<String>();
        for (int i=0; i <= server_ct; i++) {
            String name = servers.get(i).name;
            names.add(name);
        }
        String[] name_ary = names.toArray(new String[names.size()]);
        return name_ary;
    }
    
    public String[] person_names() {
        int person_ct = people.size() - 1;
        ArrayList<String> names = new ArrayList<String>();
        for (int i=0; i <= person_ct; i++) {
            names.add(people.get(i).name);
        }
        String[] name_ary = names.toArray(new String[names.size()]);
        return name_ary;
    }
    
    public String[] credential_names() {
        ArrayList<String> names = new ArrayList<String>();
        int credential_ct = credentials.size() - 1;
        for (int i=0; i <= credential_ct; i++) {
            names.add(credentials.get(i).description);
        }
        String[] name_ary = names.toArray(new String[names.size()]);
        return name_ary;
    }

   
    
    
    @SuppressWarnings("unchecked")
    public boolean remove_person(String name) {
        boolean result = false;
        Person to_remove = null;
        for (Person person : people) {
            if (person.name.equals(name)) {
                to_remove = person;
                break;
            }
        }
        if (to_remove != null) {
            people.remove(to_remove);
            Collections.sort(people);
        }
        return result;
    }
   
    @SuppressWarnings("unchecked")
    public boolean remove_server(String name) {
        boolean result = false;
        Server to_remove = null;
        for (Server server : servers) {
            if (server.name.equals(name)) {
                to_remove = server;
                break;
            }
        }
        if (to_remove != null) {
            servers.remove(to_remove);            
            Collections.sort(servers);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public boolean remove_credential(String description) {
        boolean result = false;
        Credential to_remove = null;
        for (Credential credential : credentials) {
            if (credential.description.equals(description)) {
                to_remove = credential;
                break;
            }
        }
        if (to_remove != null) {
            credentials.remove(to_remove);
            Collections.sort(credentials);
        }
        return result;
    }
    
}
