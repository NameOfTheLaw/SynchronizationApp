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
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
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
        dataSocket = null;
        OutputStream out = null;
        InputStream in = null;
        PrintWriter textOutput = null;
        DataOutputStream dataOutput = null;
        DataInputStream dataInput = null;
    }
    
    @Override
    public void run() {
        TreeSet<FileProperties> toServerReplace = new TreeSet<FileProperties>(),
                        toClientRemove = new TreeSet<FileProperties>(),
                        toClientReplace = new TreeSet<FileProperties>();
        boolean connectionIsEstablished = false,
                authorizationPassed = false;           
        ISyncService serverService = null;
        ApplicationUser user = new ApplicationUser(config.getProperty("login"),config.getProperty("password"));
        String objectName = "rmi://" + config.getProperty("host") + "/mySyncService";    
        
        try {
            serverService = (ISyncService) Naming.lookup(objectName);
        } catch (NotBoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            authorizationPassed = serverService.authorization(user);
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        if (authorizationPassed) {   
            System.out.println("Start processing...");

            try {
                serverService.sync(clientDir);
                toClientReplace = serverService.getToClientReplace();
                toClientRemove = serverService.getToClientRemove();
                toServerReplace = serverService.getToServerReplace();
            } catch (RemoteException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
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
    }
    
    @Override
    public void initFileTransferring() {
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
    public void deinitFileTransferring() {
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
