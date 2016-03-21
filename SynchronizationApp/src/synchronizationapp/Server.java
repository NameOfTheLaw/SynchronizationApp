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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrey
 */
public class Server extends WebMember {
    
    private Directory serverDir;

    public Server(Config config) {
        this.config = config;        
        serverDir = new Directory(config.getProperty(SERVER_ROOT));
        serverDir.createState();
        if (!serverDir.loadLastState(new File(config.getProperty(SERVER_STATE)))) {
            serverDir.setLastState();
        }
    }    
    
    @Override
    public void run() {
        try (
                ServerSocket server = new ServerSocket(Integer.valueOf(config.getProperty("port")));
                Socket fromclient = server.accept();
                ObjectOutputStream objectOutput = new ObjectOutputStream(fromclient.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(fromclient.getInputStream());
                PrintWriter out = new PrintWriter(fromclient.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(fromclient.getInputStream()));
                ) {           
            Directory clientDir;
            String input;
            int changes;
            HashSet<FileProperties> toClientReplace = new HashSet<FileProperties>(),
                    toClientRemove = new HashSet<FileProperties>(), 
                    toServerReplace = new HashSet<FileProperties>(),
                    toServerRemove = new HashSet<FileProperties>();
            while (!((input = in.readLine()).equals("stop"))) {
                switch (input) {
                    case "hello" : {                        
                        out.println("hello");
                        clientDir = (Directory) objectInput.readObject();
                        serverDir.syncWith(clientDir, toClientReplace, toClientRemove, toServerReplace, toServerRemove);
                        changes = toClientReplace.size() + toClientRemove.size() + toServerReplace.size() + toServerRemove.size();
                        System.out.println("I have found "+changes+" changes");
                        clientDir.deleteAll(toServerRemove);
                        /*
                        System.out.println("--------");
                        for (FileProperties fi1: toClientReplace) {
                            System.out.println(fi1.getPath());
                        }
                        System.out.println("--------");
                        for (FileProperties fi1: toClientRemove) {
                            System.out.println(fi1.getPath());
                        }
                        System.out.println("--------");
                        for (FileProperties fi1: toServerReplace) {
                            System.out.println(fi1.getPath());
                        }
                        System.out.println("--------");
                        for (FileProperties fi1: toServerRemove) {
                            System.out.println(fi1.getPath());
                        }
                        System.out.println("--------");
                        */
                        break;
                    }
                    case "getServerReplace" : {                        
                        objectOutput.writeObject(toServerReplace);
                        break;
                    }
                    case "getClientReplace" : {                        
                        objectOutput.writeObject(toClientReplace);
                        break;
                    }
                    case "getClientRemove" : {                        
                        objectOutput.writeObject(toClientRemove);
                        break;
                    }
                    case "sendForServerReplace" : {
                        for (FileProperties fi : toServerReplace) {
                            receiveFile(fromclient, serverDir, (String)fi.getPath());
                        }
                    }
                    case "waitForClientReplace" : {
                        for (FileProperties fi : toClientReplace) {
                            sendFile(fromclient, serverDir, (String)fi.getPath());
                        }
                    }
                    default : {
                        out.println("error");    
                    }
                }
            }
            serverDir.saveStateToFile(new File(config.getProperty(SERVER_STATE)));
            System.out.println("Completed!");
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Shit happens");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
