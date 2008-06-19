/*
 * XMLReader.java
 *
 * Created on Sep 29, 2007, 3:58:25 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 *
 * @author patrick
 */
public class XMLReader {

    public XMLReader() {
    }

    public PasswordStore read_xml(String input_text) {
        PasswordStore pws = new PasswordStore();

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//            Document doc = docBuilder.parse(new File(filename));
            
            Document doc = docBuilder.parse(input_text);
            doc.getDocumentElement().normalize();

            // Read People
            NodeList listOfPersons = doc.getElementsByTagName("person");
            int totalPersons = listOfPersons.getLength();

            for (int s = 0; s < listOfPersons.getLength(); s++) {
                // Create Person
                Person firstPerson = new Person();
                Node firstPersonNode = listOfPersons.item(s);
                if (firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
                    firstPerson = read_person(firstPersonNode);
                }
                pws.add_person(firstPerson);
            }

            // Read Servers
            NodeList listOfservers = doc.getElementsByTagName("server");
            int totalservers = listOfservers.getLength();

            for (int s = 0; s < listOfservers.getLength(); s++) {
                // Create server
                Server firstserver = new Server();
                Node firstserverNode = listOfservers.item(s);
                if (firstserverNode.getNodeType() == Node.ELEMENT_NODE) {
                    firstserver = read_server(firstserverNode);
                }
                pws.add_server(firstserver);
            }

            // Read Credentials
            NodeList listOfcredentials = doc.getElementsByTagName("credential");
            int totalcredentials = listOfcredentials.getLength();

            for (int s = 0; s < listOfcredentials.getLength(); s++) {
                // Create credential
                Credential firstcredential = new Credential();
                Node firstcredentialNode = listOfcredentials.item(s);
                if (firstcredentialNode.getNodeType() == Node.ELEMENT_NODE) {
                    firstcredential = read_credential(firstcredentialNode);
                }
                pws.add_credential(firstcredential);
            }
        } catch (SAXParseException err) {
            pws = null;
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
        } catch (SAXException e) {
            pws = null;
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            pws = null;
            t.printStackTrace();
        }
        return pws;
    }

    public Person read_person(Node firstPersonNode) {
        Person firstPerson = new Person();
        try {
            Element firstpersonElement = (Element) firstPersonNode;

            // Set Name
            NodeList nameList = firstpersonElement.getElementsByTagName("name");
            Element nameElement = (Element) nameList.item(0);
            NodeList textNameList = nameElement.getChildNodes();
            String name = textNameList.item(0).getNodeValue().trim();
            firstPerson.name = name;

            // Set email
            NodeList emailList = firstpersonElement.getElementsByTagName("email");
            Element emailElement = (Element) emailList.item(0);
            NodeList textemailList = emailElement.getChildNodes();
            String email = "";
            if (textemailList.item(0) != null) {
                email = textemailList.item(0).getNodeValue().trim();
            }
            firstPerson.email = email;

            // Set notes
            NodeList notesList = firstpersonElement.getElementsByTagName("notes");
            Element notesElement = (Element) notesList.item(0);
            NodeList textnotesList = notesElement.getChildNodes();
            String notes = "";
            if (textnotesList.item(0) != null) {
                notes = textnotesList.item(0).getNodeValue().trim();
            }
            firstPerson.notes = notes;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return firstPerson;
    }

    public Server read_server(Node firstserverNode) {
        Server firstserver = new Server();
        try {
            Element firstserverElement = (Element) firstserverNode;

            // Set Name
            NodeList nameList = firstserverElement.getElementsByTagName("name");
            Element nameElement = (Element) nameList.item(0);
            if (nameElement != null) {
                NodeList textNameList = nameElement.getChildNodes();
                String name = "";
                if (textNameList.item(0) != null) {
                    name = textNameList.item(0).getNodeValue().trim();
                }
                firstserver.name = name;

                // Set address
                NodeList addressList = firstserverElement.getElementsByTagName("address");
                Element addressElement = (Element) addressList.item(0);
                NodeList textaddressList = addressElement.getChildNodes();
                String address = "";
                if (textaddressList.item(0) != null) {
                    address = textaddressList.item(0).getNodeValue().trim();
                }
                firstserver.address = address;

                // Set notes
                NodeList notesList = firstserverElement.getElementsByTagName("notes");
                Element notesElement = (Element) notesList.item(0);
                NodeList textnotesList = notesElement.getChildNodes();
                String notes = "";
                if (textnotesList.item(0) != null) {
                    notes = textnotesList.item(0).getNodeValue().trim();
                }
                firstserver.notes = notes;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return firstserver;
    }

    public Credential read_credential(Node firstcredentialNode) {
        Credential firstcredential = new Credential();
        try {
            Element firstcredentialElement = (Element) firstcredentialNode;

            // Set description
            NodeList descriptionList = firstcredentialElement.getElementsByTagName("description");
            Element descriptionElement = (Element) descriptionList.item(0);
            NodeList textdescriptionList = descriptionElement.getChildNodes();
            String description = "";
            if (textdescriptionList.item(0) != null) {
                description = textdescriptionList.item(0).getNodeValue().trim();
            }
            firstcredential.description = description;

            // Set server
            NodeList serverList = firstcredentialElement.getElementsByTagName("server_name");
            Element serverElement = (Element) serverList.item(0);
            String server_name = "";
            Server server = new Server();
            if (serverElement != null) {
                NodeList textserverList = serverElement.getChildNodes();
                server_name = textserverList.item(0).getNodeValue().trim();
                server.name = server_name;
            }
            firstcredential.server = server;

            // Set person
            NodeList personList = firstcredentialElement.getElementsByTagName("person_name");
            Element personElement = (Element) personList.item(0);
            String person_name = "";
            Person person = new Person();
            if (personElement != null) {
                NodeList textpersonList = personElement.getChildNodes();
                person_name = textpersonList.item(0).getNodeValue().trim();
                person.name = person_name;
            }
            firstcredential.person = person;

            // Set address
            NodeList addressList = firstcredentialElement.getElementsByTagName("credential_address");
            Element addressElement = (Element) addressList.item(0);
            NodeList textaddressList = addressElement.getChildNodes();
            String address = "";
            if (textaddressList.item(0) != null) {
                address = textaddressList.item(0).getNodeValue().trim();
            }
            firstcredential.address = address;

            // Set login
            NodeList loginList = firstcredentialElement.getElementsByTagName("login");
            Element loginElement = (Element) loginList.item(0);
            NodeList textloginList = loginElement.getChildNodes();
            String login = "";
            if (textloginList.item(0) != null) {
                login = textloginList.item(0).getNodeValue().trim();
            }
            firstcredential.login = login;

            // Set password
            NodeList passwordList = firstcredentialElement.getElementsByTagName("password");
            Element passwordElement = (Element) passwordList.item(0);
            NodeList textpasswordList = passwordElement.getChildNodes();
            String password = "";
            if (textpasswordList.item(0) != null) {
                password = textpasswordList.item(0).getNodeValue().trim();
            }
            firstcredential.password = password;

            // Set notes
            NodeList notesList = firstcredentialElement.getElementsByTagName("notes");
            Element notesElement = (Element) notesList.item(0);
            NodeList textnotesList = notesElement.getChildNodes();
            String notes = "";
            if (textnotesList.item(0) != null) {
                notes = textnotesList.item(0).getNodeValue().trim();
            }
            firstcredential.notes = notes;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return firstcredential;
    }
}