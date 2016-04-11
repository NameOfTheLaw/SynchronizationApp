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
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Класс-поток сервера. При запуске начинает записывать поступающие команды до "stop".
 * После этого начинает последовательно выполнять команды. 
 * По завершению выполнения команд всё начинается сначала
 * @author andrey
 */
public class Server extends WebMember {    
    private Directory serverDir, clientDir;

    /**
     * Класс-конструктор сервера
     * @param config конфиг
     */
    public Server(Config config) {
        this.config = config;
        serverDir = new Directory(config.getProperty(SERVER_ROOT));
        serverDir.createState();
        if (!serverDir.loadLastState(new File(config.getProperty(SERVER_STATE)))) {
            serverDir.setLastState();
        }                    
        dataSocket = null;
        dataOutput = null;
        dataInput = null;      
        server = null;            
        out = null;
        in = null;      
    }    
    
    @Override
    public void run() {
        String serviceName = "mySyncService";
        Registry reg = null;
        try {
            reg = LocateRegistry.createRegistry(Integer.valueOf(config.getProperty("port")));
        } catch (RemoteException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while (true) {
            TreeSet<FileProperties> 
                    toClientReplace = null,
                    toClientRemove = null, 
                    toServerReplace = null,
                    toServerRemove = null;
            boolean timeisstart = false,
                clientIsAuthorised = false;
            ISyncService serverService = null;

            System.out.println("Waiting for client...");

            try {                   
                serverService = new SyncService();
                serverService.setServer(this);
                serverService.setServerDir(serverDir);
                reg.rebind(serviceName, serverService);
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Класс засыпает до тех пор, пока клиент не получит необходимые для синхронизации данные.
            // метод sync класса SyncService будит этот класс
            suspend();
            
            try {
                toClientReplace = serverService.getToClientReplace();
                toClientRemove = serverService.getToClientRemove();
                toServerReplace = serverService.getToServerReplace();
                toServerRemove = serverService.getToServerRemove();
            } catch (RemoteException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            serverDir.deleteAll(toServerRemove);
            
            initFileTransferring();
            sendFiles(dataOutput,serverDir,toClientReplace);
            receiveFiles(dataInput,serverDir,toServerReplace);
            deinitFileTransferring();
            
            serverDir.createState();
            serverDir.saveStateToFile(new File(config.getProperty(SERVER_STATE)));
            serverDir.setLastState();
            System.out.println("Done.");
            //System.out.println("Completed in " + (System.currentTimeMillis() - time) + "ms!");            
        }
    }
    
    @Override
    public void initFileTransferring() {
        try {
            server = new ServerSocket(Integer.valueOf(config.getProperty("port"))+2);
            dataSocket = server.accept();
            out = dataSocket.getOutputStream();
            in = dataSocket.getInputStream();                            
            dataOutput = new DataOutputStream(out);
            dataInput = new DataInputStream(in);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deinitFileTransferring() {
        try {
            server.close();
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
