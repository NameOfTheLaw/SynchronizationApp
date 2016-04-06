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
        commandSocket = null;
        dataSocket = null;
        objectSocket = null;
        dataOutput = null;
        dataInput = null;            
        objectOutput = null;
        objectInput = null;
        server = null;            
        out = null;
        in = null;        
        textInput = null;
    }    
    
    @Override
    public void run() {

        while (true) {            
            TreeSet<FileProperties> 
                    toClientReplace = new TreeSet<FileProperties>(),
                    toClientRemove = new TreeSet<FileProperties>(), 
                    toServerReplace = new TreeSet<FileProperties>(),
                    toServerRemove = new TreeSet<FileProperties>();            
            String input;
            ArrayList<String> commands = new ArrayList<String>();
            int changes;
            long time = 0;
            boolean timeisstart = false,
                clientIsAuthorised = false;
            
            System.out.println("Waiting for client...");
                        
            initCommandTransferring();
            try {
                while (!(input = textInput.readLine()).equals("stop")) {
                    time = (timeisstart) ? time : System.currentTimeMillis();
                    commands.add(input);
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }                        
            serverDir.createState();
            
            System.out.println("Start processing...");
            int mark = 0;
            while (!commands.isEmpty()) {
                System.out.println("> handling now: "+commands.get(mark)+"/"+commands.size());
                switch ((String)commands.get(mark)) {
                    case "authorization" : {
                        ApplicationUser user = new ApplicationUser();
                        try {
                            user.setLogin(textInput.readLine());
                            user.setPassword(textInput.readLine());
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SynchronizationAppPU");            
                        EntityManager em = emf.createEntityManager();
                        em.getTransaction().begin();
                        List<ApplicationUser> users = (List<ApplicationUser>)em.createQuery("from ApplicationUser").getResultList();
                        em.getTransaction().commit();
                        em.close();
                        emf.close();
                        if (users.contains(user)) {
                            System.out.println("Authorization successfull!");
                            clientIsAuthorised = true;
                            textOutput.println("continue");
                        } else {                            
                            System.out.println("Bad authorization...");
                            clientIsAuthorised = false;
                            textOutput.println("stop");
                            commands.clear();
                            break;
                        }
                        break;
                    }
                    case "sendDirectory" : {
                        if (clientIsAuthorised) {
                            try {
                                clientDir = (Directory) objectInput.readObject();
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ClassNotFoundException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }                       
                            serverDir.syncWith(clientDir, toClientReplace, toClientRemove, toServerReplace, toServerRemove);
                            changes = toClientReplace.size() + toClientRemove.size() + toServerReplace.size() + toServerRemove.size();
                            System.out.println("I have found " + changes + " changes");
                            serverDir.deleteAll(toServerRemove);
                            System.out.println("----toClientReplace----");
                            for (FileProperties fi1: toClientReplace) {
                                System.out.println(fi1.getPath());
                            }
                            System.out.println("----toClientRemove----");
                            for (FileProperties fi1: toClientRemove) {
                                System.out.println(fi1.getPath());
                            }
                            System.out.println("----toServerReplace----");
                            for (FileProperties fi1: toServerReplace) {
                                System.out.println(fi1.getPath());
                            }
                            System.out.println("----toServerRemove----");
                            for (FileProperties fi1: toServerRemove) {
                                System.out.println(fi1.getPath());
                            }
                            System.out.println("--------");
                        }
                        break;
                    }
                    case "getServerReplace" : {
                        if (clientIsAuthorised) {
                            try {
                                objectOutput.writeObject(toServerReplace);
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;
                    }
                    case "getClientReplace" : {                        
                        if (clientIsAuthorised) {
                            try {
                                objectOutput.writeObject(toClientReplace);
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;
                    }
                    case "getClientRemove" : {    
                        if (clientIsAuthorised) {                    
                            try {
                                objectOutput.writeObject(toClientRemove);
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        break;
                    }
                    case "startFilesTransfer" : {                        
                        if (clientIsAuthorised) {
                            deinitObjectTransferring();
                            initFileTransferring();
                        }
                        break;
                    }
                    case "startObjectsTransfer" : {
                        if (clientIsAuthorised) {
                            deinitCommandTransferring();
                            initObjectTransferring();
                        }
                        break;
                    }
                    case "sendForServerReplace" : {
                        if (clientIsAuthorised) {
                            receiveFiles(dataInput,serverDir,toServerReplace);
                        }
                        break;
                    }
                    case "waitForClientReplace" : {
                        if (clientIsAuthorised) {
                            sendFiles(dataOutput,serverDir,toClientReplace);
                        }
                        break;
                    }
                }
                if (!commands.isEmpty()) commands.remove(mark);
            }
            deinitFileTransferring();
            serverDir.createState();
            serverDir.saveStateToFile(new File(config.getProperty(SERVER_STATE)));
            serverDir.setLastState();
            System.out.println("Completed in " + (System.currentTimeMillis() - time) + "ms!");
        }
    }
    
    @Override
    protected void initCommandTransferring() {        
        try {
            server = new ServerSocket(Integer.valueOf(config.getProperty("port")));
            commandSocket = server.accept();
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
            server.close();
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
            server = new ServerSocket(Integer.valueOf(config.getProperty("port"))+1);
            objectSocket = server.accept();
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
            server.close();
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
            server = new ServerSocket(Integer.valueOf(config.getProperty("port"))+2);
            dataSocket = server.accept();
            out = dataSocket.getOutputStream();
            in = dataSocket.getInputStream();                            
            dataOutput = new DataOutputStream(out);
            dataInput = new DataInputStream(in);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            //ignore
        }
    }

    @Override
    protected void deinitFileTransferring() {
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
