/*
 * Person.java
 * 
 * Created on Sep 23, 2007, 5:18:31 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

/**
 *
 * @author patrick
 */
public class Person implements Comparable {
    public String name = "";
    public String email = "";
    public String notes = "";

    public static void Person() {
        
    }
    
    public void set_info(String givenName, String givenEmail, String givenNotes) {
        name = givenName;
        email = givenEmail;
        notes = givenNotes;
    }
    
   
    @Override
    public String toString(){
        return name;
    }

    public int compareTo(Object arg0) {
        Person person = (Person)arg0;
        String person_name = person.name;
        return name.compareToIgnoreCase(person_name);
    }
}
