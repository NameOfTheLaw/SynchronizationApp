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
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;
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
        Socket commandSocket = null, objectSocket = null, dataSocket = null;
        OutputStream out = null;
        InputStream in = null;
        PrintWriter textOutput = null;
        DataOutputStream dataOutput = null;
        DataInputStream dataInput = null;
        ObjectOutputStream objectOutput = null;
        ObjectInputStream objectInput = null;       

        TreeSet<FileProperties> toServerReplace = new TreeSet<FileProperties>(),
                        toClientRemove = new TreeSet<FileProperties>(),
                        toClientReplace = new TreeSet<FileProperties>();
        
        try {
            commandSocket = new Socket(config.getProperty("host"),Integer.valueOf(config.getProperty("port")));
            out = commandSocket.getOutputStream();
            in = commandSocket.getInputStream();            
            textOutput = new PrintWriter(out,true);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        textOutput.println("startObjectsTransfer");
        
        textOutput.println("sendDirectory");

        textOutput.println("getServerReplace");                

        textOutput.println("getClientReplace");                

        textOutput.println("getClientRemove");

        textOutput.println("startFilesTransfer");

        textOutput.println("sendForServerReplace");            

        textOutput.println("waitForClientReplace");

        textOutput.println("stop");

        try {
            commandSocket.close();
            in.close();
            out.close();
            textOutput.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Start processing...");

        try {
            objectSocket = new Socket(config.getProperty("host"),Integer.valueOf(config.getProperty("port")));
            out = objectSocket.getOutputStream();
            in = objectSocket.getInputStream();
            objectOutput = new ObjectOutputStream(out);
            objectInput = new ObjectInputStream(in);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
                          
        try {
            objectOutput.writeObject(clientDir);      

            toServerReplace = (TreeSet<FileProperties>) objectInput.readObject();          

            toClientReplace = (TreeSet<FileProperties>) objectInput.readObject();

            toClientRemove = (TreeSet<FileProperties>) objectInput.readObject();

            objectSocket.close();
            out.close();
            in.close();
            objectOutput.close();
            objectInput.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        clientDir.deleteAll(toClientRemove);

        System.out.println("Start transferring files...");
        
        try {
            dataSocket = new Socket(config.getProperty("host"),Integer.valueOf(config.getProperty("port")));
            out = dataSocket.getOutputStream();
            in = dataSocket.getInputStream();
            dataOutput = new DataOutputStream(out);
            dataInput = new DataInputStream(in);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendFiles(dataOutput,clientDir,toServerReplace);

        receiveFiles(dataInput,clientDir,toClientReplace);
        
        try {
            dataSocket.close();
            out.close();
            in.close();
            dataOutput.close();
            dataInput.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        clientDir.createState();
        clientDir.saveStateToFile(new File(config.getProperty(CLIENT_STATE)));
        System.out.println("Completed!");
        
    }
    
}
