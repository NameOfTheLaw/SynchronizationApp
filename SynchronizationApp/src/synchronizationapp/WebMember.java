/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrey
 */
abstract class WebMember extends Thread{
    
    protected Config config;
    protected final String CLIENT_ROOT="root2";
    protected final String CLIENT_STATE="lastState2";
    protected final String SERVER_ROOT="root1";
    protected final String SERVER_STATE="lastState1";
        
    protected void receiveFile(DataInputStream is, Directory dir, String string) {
        File file = new File(dir.getPath()+File.separator+string);
        try {
            file.createNewFile();
        } catch (IOException ex) {
            //ignore
        }
        try (FileOutputStream os = new FileOutputStream(file);){            
            System.out.println("Saving " + string + "...");
            long length = is.readLong();
            long now = 0;
            int readedBytesCount, total = 0;
            byte[] buffer = new byte[1000];
            while ((readedBytesCount = is.read(buffer, 0, Math.min(buffer.length, (int)length-total))) != -1) {
                total += readedBytesCount;
                os.write(buffer, 0, readedBytesCount);

                if (total == (int)length){
                    break;
                }
            }
            /*
            while (true){
                readedBytesCount = is.read(buffer);
                if (readedBytesCount == -1) {
                    break;
                }
                if (readedBytesCount > 0) {
                    os.write(buffer, 0, readedBytesCount);
                }
                total=+readedBytesCount;
                if (total == (int)length) {
                    break;
                } else {
                    System.out.println(total+" != "+length);
                }
            }
            */
            System.out.println("File has been saved: " + string);
        } catch (SocketException ex) {
            //ignore            
            System.out.println("File has been saved*: " + string);
        } catch (IOException ex) {            
            //System.out.println("Error at saving: " + string);
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void sendFile(DataOutputStream os, Directory dir, String string) {
        try (FileInputStream is = new FileInputStream(dir.getPath()+File.separator+string);){
            File file = new File(dir.getPath()+File.separator+string);
            long length = file.length();            
            System.out.println("Sending " + string + "("+length+"bytes)...");
            os.writeLong(length);
            byte[] buffer = new byte[1000];
            while (true){
                int readedBytesCount = is.read(buffer);
                if (readedBytesCount == -1) {
                    break;
                }
                if (readedBytesCount > 0) {
                    os.write(buffer, 0, readedBytesCount);
                }
            }            
            System.out.println("Sending has been finished: " + string);
        } catch (SocketException ex) {
            System.out.println("Sending has been finished*: " + string);
            //ignore
        } catch (IOException ex) {
            //System.out.println("Error at sending: " + string);
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void sendFiles(DataOutputStream dataOutput, Directory dir, Set<FileProperties> set) {        
        for (FileProperties fp : set) {
            sendFile(dataOutput,dir,(String)fp.getPath());
        }
    }
    
    protected void receiveFiles(DataInputStream dataInput, Directory dir, Set<FileProperties> set) {   
        for (FileProperties fp : set) {
            receiveFile(dataInput,dir,(String)fp.getPath());
        }
    }
    
    protected void sendCommand(OutputStream os, String text) {
        PrintWriter out = new PrintWriter(os,true);
        out.println(text);
        out.close();
    }
    
    protected String receiveCommand(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        try {
            String com = in.readLine();
            return com;
        } catch (IOException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    protected void sendObject(OutputStream os, Set object) {
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(os)) {
            objectOutput.writeObject(object);
        } catch (IOException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected Set receiveObject(InputStream is) {
        try (ObjectInputStream objectInput = new ObjectInputStream(is)) {
            Set object = (Set)objectInput.readObject();
            return object;
        } catch (IOException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    protected void sendDir(OutputStream os, Directory object) {
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(os)) {
            objectOutput.writeObject(object);
        } catch (IOException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected Directory receiveDir(InputStream is) {
        try (ObjectInputStream objectInput = new ObjectInputStream(is)) {
            Directory object = (Directory)objectInput.readObject();
            return object;
        } catch (IOException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    /*
    protected <T> void sendObject(OutputStream os, T object) {
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(os)) {
            objectOutput.writeObject((T)object);
        } catch (IOException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected <T> Object receiveObject(InputStream is) {
        try (ObjectInputStream objectInput = new ObjectInputStream(is)) {
            T object = (T)objectInput.readObject();
            return object;
        } catch (IOException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(WebMember.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    */
}
