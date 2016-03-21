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
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrey
 */
public class Client extends WebMember{
    
    private Directory clientDir;

    public Client(Config config) {
        this.config = config;            
        clientDir = new Directory(config.getProperty(CLIENT_ROOT));
        clientDir.createState();
        if (!clientDir.loadLastState(new File(config.getProperty(CLIENT_STATE)))) {
            clientDir.setLastState();
        }
    }
    
    private boolean checkConnection(PrintWriter out, BufferedReader in) {
        String message = "hello", answer = null;
        out.println(message);
        try {
            answer = in.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (answer.equals("hello")) {
            return true;
        } else {
            System.out.println(answer);
            return false;
        }
    }
    
    @Override
    public void run() {
        try (
                Socket fromserver = new Socket(config.getProperty("host"),Integer.valueOf(config.getProperty("port")));
                ObjectOutputStream objectOutput = new ObjectOutputStream(fromserver.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(fromserver.getInputStream());
                PrintWriter out = new PrintWriter(fromserver.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(fromserver.getInputStream()));
                ){
            System.out.println("Connecting to " + config.getProperty("host") + ":" + config.getProperty("port")+ " ...");
            if (checkConnection(out,in)) {
                System.out.println("Connection online");
                objectOutput.writeObject(clientDir);
                HashSet<FileProperties> toServerReplace = new HashSet<FileProperties>(),
                                toClientRemove = new HashSet<FileProperties>(),
                                toClientReplace = new HashSet<FileProperties>();
                out.println("getServerReplace");                
                toServerReplace = (HashSet<FileProperties>) objectInput.readObject();
                out.println("getClientReplace");                
                toClientReplace = (HashSet<FileProperties>) objectInput.readObject();
                out.println("getClientRemove");                
                toClientRemove = (HashSet<FileProperties>) objectInput.readObject();
                clientDir.deleteAll(toClientRemove);
                out.println("sendForServerReplace");
                for (FileProperties fi : toServerReplace) {
                    System.out.println("Sending "+(String)fi.getPath()+" ...");
                    sendFile(fromserver, clientDir, (String)fi.getPath());
                }
                out.println("waitForClientReplace");
                for (FileProperties fi : toClientReplace) {
                    System.out.println("Receiving "+(String)fi.getPath()+" ...");
                    receiveFile(fromserver, clientDir, (String)fi.getPath());
                }          
                out.println("stop");
            } else {
                System.out.println("Can't connection to the server");
            }
            clientDir.saveStateToFile(new File(config.getProperty(CLIENT_STATE)));
            System.out.println("Completed!");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Shit happens");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
