/*
 * ReadFile.java
 * 
 * Created on Oct 17, 2007, 9:34:51 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package passvault.resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author patrick
 */
public class ReadFile {
    
    public String filename = null;
    
    ReadFile(String in_filename){ 
        filename = in_filename;
    }
    
    public String read() {
        String text = "";
        try {
            FileReader input = new FileReader(filename);
            BufferedReader bufRead = new BufferedReader(input);
            String line;
            line = bufRead.readLine();
            while (line != null){                
                text += line;
                line = bufRead.readLine();
            }
            bufRead.close();
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        } catch (IOException e){
        e.printStackTrace();
      }
     return text;
    }
    
    public String write(String text) {
        try {
            FileWriter input = new FileWriter(filename);
            input.write(text);
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        } catch (IOException e){
        e.printStackTrace();
      }
     return text;
    }
    
}