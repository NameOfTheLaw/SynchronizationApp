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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс-поток клиента. При запуске отправляет серверу список действий и начинает их последовательно выполнять.
 * @author andrey
 */
public class Client extends WebMember{
    
    private Directory clientDir;

    /**
     * Метод-конструктор клиента
     * @param config конфиг
     */
    public Client(Config config) {
        this.config = config;            
        clientDir = new Directory(config.getProperty(CLIENT_ROOT));
        clientDir.createState();
        if (!clientDir.loadLastState(new File(config.getProperty(CLIENT_STATE)))) {
            clientDir.setLastState();
        }        
        commandSocket = null; 
        objectSocket = null; 
        dataSocket = null;
        OutputStream out = null;
        InputStream in = null;
        PrintWriter textOutput = null;
        DataOutputStream dataOutput = null;
        DataInputStream dataInput = null;
        ObjectOutputStream objectOutput = null;
        ObjectInputStream objectInput = null;
    }
    
    @Override
    public void run() {
        TreeSet<FileProperties> toServerReplace = new TreeSet<FileProperties>(),
                        toClientRemove = new TreeSet<FileProperties>(),
                        toClientReplace = new TreeSet<FileProperties>();
        boolean connectionIsEstablished = false,
                authorizationPassed = false;
        
        try {
            initCommandTransferring();
        } catch (Exception ex) {
            System.out.println("Can't connect to server...");
        } finally {
            System.out.println("Connection had established...");
            connectionIsEstablished = true;
        }
        
        if (connectionIsEstablished) {
            textOutput.println("authorization");
            textOutput.println("startObjectsTransfer");
            textOutput.println("sendDirectory");
            textOutput.println("getServerReplace");
            textOutput.println("getClientReplace");
            textOutput.println("getClientRemove");
            textOutput.println("startFilesTransfer");
            textOutput.println("sendForServerReplace");
            textOutput.println("waitForClientReplace");
            textOutput.println("stop");
            
            textOutput.println(config.getProperty("login"));
            textOutput.println(config.getProperty("password"));
            String answer = null;
            
            try {
                answer = textInput.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                authorizationPassed = false;
                System.out.println("Authorization error");
            }
            
            if (answer.equals("continue")) {
                authorizationPassed = true;
                System.out.println("Authorization successfull!");
            } else {
                authorizationPassed = false;
                System.out.println("Bad authorization...");
            }
            deinitCommandTransferring();

            if (authorizationPassed) {            
                System.out.println("Start processing...");
                initObjectTransferring();
                try {
                    objectOutput.writeObject(clientDir);      
                    toServerReplace = (TreeSet<FileProperties>) objectInput.readObject();   
                    toClientReplace = (TreeSet<FileProperties>) objectInput.readObject();
                    toClientRemove = (TreeSet<FileProperties>) objectInput.readObject();
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                deinitObjectTransferring();
                clientDir.deleteAll(toClientRemove);

                System.out.println("Start transferring files...");        
                initFileTransferring();        
                sendFiles(dataOutput,clientDir,toServerReplace);
                receiveFiles(dataInput,clientDir,toClientReplace);        
                deinitFileTransferring();

                clientDir.createState();
                clientDir.saveStateToFile(new File(config.getProperty(CLIENT_STATE)));
                System.out.println("Completed!");            
            } else {
                System.out.println("Please check your login and password. Bye");               
            }
        } else {
            System.out.println("Please try again later. Bye");
        }
    }
    
    @Override
    protected void initCommandTransferring() {        
        try {
            commandSocket = new Socket(config.getProperty("host"),Integer.valueOf(config.getProperty("port")));
            out = commandSocket.getOutputStream();
            in = commandSocket.getInputStream();         
            textOutput = new PrintWriter(out,true);       
            textInput = new BufferedReader(new InputStreamReader(in));     
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void deinitCommandTransferring() {        
        try {
            commandSocket.close();
            out.close();     
            in.close();
            textOutput.close();
            textInput.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            //ignore
        }
    }
    
    @Override
    protected void initObjectTransferring() {
        try {
            objectSocket = new Socket(config.getProperty("host"),Integer.valueOf(config.getProperty("port"))+1);
            out = objectSocket.getOutputStream();
            in = objectSocket.getInputStream();
            objectOutput = new ObjectOutputStream(out);
            objectInput = new ObjectInputStream(in);
        } catch (IOException ex) {                            
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void deinitObjectTransferring() {
        try {
            objectSocket.close();
            out.close();
            in.close();
            objectOutput.close();
            objectInput.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            //ignore
        }
    }
    
    @Override
    protected void initFileTransferring() {
        try {
            dataSocket = new Socket(config.getProperty("host"),Integer.valueOf(config.getProperty("port"))+2);
            out = dataSocket.getOutputStream();
            in = dataSocket.getInputStream();
            dataOutput = new DataOutputStream(out);
            dataInput = new DataInputStream(in);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void deinitFileTransferring() {
        try {
            dataSocket.close();
            out.close();
            in.close();
            dataOutput.close();
            dataInput.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            //ignore
        }
    }
    
}
