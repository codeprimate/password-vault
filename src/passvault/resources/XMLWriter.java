/*
 * XMLWriter.java
 *
 * Created on Sep 29, 2007, 7:51:32 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author patrick
 */
public class XMLWriter {

    public XMLWriter() {
    }

    public boolean write_xml(String filename, PasswordStore pws) {
        boolean result = true;
        String output_xml = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("PasswordStore");
            
            root.appendChild(build_servers_node(doc, pws));
            root.appendChild(build_people_node(doc, pws));
            root.appendChild(build_credentials_node(doc, pws));
            doc.appendChild(root);

            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer aTransformer = tranFactory.newTransformer();

            Source src = new DOMSource(doc);
            Result dest = new StreamResult(new File(filename));
            
            aTransformer.transform(src, dest);
            
        } catch (TransformerException ex) {
            Logger.getLogger(XMLWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException pce) {
            result = false;
            pce.printStackTrace();
        }


        return result;
    }

    private Node build_servers_node(Document document, PasswordStore pws) {
        Node serversNode = (Node) document.createElement("servers");
        for (Server server : pws.servers) {
            Node serverNode = (Node) document.createElement("server");
            Element serverName = document.createElement("name");
            serverName.setTextContent(server.name);
            serverNode.appendChild(serverName);
            Element serverAddress = document.createElement("address");
            serverAddress.setTextContent(server.address);
            serverNode.appendChild(serverAddress);
            Element serverNotes = document.createElement("notes");
            serverNotes.setTextContent(server.notes);
            serverNode.appendChild(serverNotes);
            serversNode.appendChild(serverNode);
        }
        return serversNode;
    }

    private Node build_people_node(Document document, PasswordStore pws) {
        Node peopleNode = (Node) document.createElement("people");
        for (Person person : pws.people) {
            Node personNode = (Node) document.createElement("person");
            Element personName = document.createElement("name");
            personName.setTextContent(person.name);
            personNode.appendChild(personName);
            Element personEmail = document.createElement("email");
            personEmail.setTextContent(person.email);
            personNode.appendChild(personEmail);
            Element personNotes = document.createElement("notes");
            personNotes.setTextContent(person.notes);
            personNode.appendChild(personNotes);
            peopleNode.appendChild(personNode);
        }
        return peopleNode;
    }

    private Node build_credentials_node(Document document, PasswordStore pws) {
        Node credentialsNode = (Node) document.createElement("credentials");
        for (Credential credential : pws.credentials) {
            Node credentialNode = (Node) document.createElement("credential");
            Element credentialName = document.createElement("description");
            credentialName.setTextContent(credential.description);
            credentialNode.appendChild(credentialName);
            Element credentialServer = document.createElement("server_name");
            credentialServer.setTextContent(credential.server.name);
            credentialNode.appendChild(credentialServer);
            Element credentialPerson = document.createElement("person_name");
            credentialPerson.setTextContent(credential.person.name);
            credentialNode.appendChild(credentialPerson);
            Element credentialAddress = document.createElement("credential_address");
            credentialAddress.setTextContent(credential.address);
            credentialNode.appendChild(credentialAddress);
            Element credentialLogin = document.createElement("login");
            credentialLogin.setTextContent(credential.login);
            credentialNode.appendChild(credentialLogin);
            Element credentialPassword = document.createElement("password");
            credentialPassword.setTextContent(credential.password);
            credentialNode.appendChild(credentialPassword);
            Element credentialNotes = document.createElement("notes");
            credentialNotes.setTextContent(credential.notes);
            credentialNode.appendChild(credentialNotes);
            credentialsNode.appendChild(credentialNode);
        }
        return credentialsNode;
    }
}