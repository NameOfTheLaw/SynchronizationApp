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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.TreeSet;
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
        Socket commandSocket = null, dataSocket = null, objectSocket = null;
        DataOutputStream dataOutput = null;
        DataInputStream dataInput = null;            
        ObjectOutputStream objectOutput = null;
        ObjectInputStream objectInput = null;
        ServerSocket server = null;            
        OutputStream out = null;
        InputStream in = null;        
        BufferedReader textInput = null;

        while (true) {
            System.out.println("Waiting for client...");
            try {
                server = new ServerSocket(Integer.valueOf(config.getProperty("port")));
                commandSocket = server.accept();
                out = commandSocket.getOutputStream();
                in = commandSocket.getInputStream();        
                textInput = new BufferedReader(new InputStreamReader(in));
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }            
            serverDir.createState();

            /*
            ObjectOutputStream objectOutput = new ObjectOutputStream(out);
            ObjectInputStream objectInput = new ObjectInputStream(in);
            DataInputStream dataInput = new DataInputStream(in);
            DataOutputStream dataOutput = new DataOutputStream(out);
            */

            Directory clientDir = null;
            String input;
            int changes;
            TreeSet<FileProperties> toClientReplace = new TreeSet<FileProperties>(),
                    toClientRemove = new TreeSet<FileProperties>(), 
                    toServerReplace = new TreeSet<FileProperties>(),
                    toServerRemove = new TreeSet<FileProperties>();
            ArrayList<String> commands = new ArrayList<String>();
            try {
                while (!(input = textInput.readLine()).equals("stop")) {
                    commands.add(input);
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                server.close();
                commandSocket.close();
                out.close();
                in.close();
                textInput.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (String str : commands) {
                System.out.println("command: "+str);
            }

            System.out.println("Start processing...");

            int mark = 0;

            while (!commands.isEmpty()) {
                System.out.println("> now case: "+commands.get(mark)+"/"+commands.size());
                switch ((String)commands.get(mark)) {
                    case "sendDirectory" : {             
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
                        break;
                    }
                    case "getServerReplace" : {
                        try {
                            objectOutput.writeObject(toServerReplace);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    case "getClientReplace" : {                        
                        try {
                            objectOutput.writeObject(toClientReplace);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    case "getClientRemove" : {                        
                        try {
                            objectOutput.writeObject(toClientRemove);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    case "startFilesTransfer" : {
                        try {
                            server.close();
                            objectSocket.close();
                            out.close();
                            in.close();
                            objectOutput.close();
                            objectInput.close();

                            server = new ServerSocket(Integer.valueOf(config.getProperty("port")));
                            dataSocket = server.accept();
                            out = dataSocket.getOutputStream();
                            in = dataSocket.getInputStream();                            
                            dataOutput = new DataOutputStream(out);
                            dataInput = new DataInputStream(in);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    case "startObjectsTransfer" : {
                        try {
                            server = new ServerSocket(Integer.valueOf(config.getProperty("port")));
                            objectSocket = server.accept();
                            out = objectSocket.getOutputStream();
                            in = objectSocket.getInputStream();
                            objectOutput = new ObjectOutputStream(out);
                            objectInput = new ObjectInputStream(in);
                        } catch (IOException ex) {                            
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    }
                    case "sendForServerReplace" : {
                        receiveFiles(dataInput,serverDir,toServerReplace);
                        break;
                    }
                    case "waitForClientReplace" : {
                        sendFiles(dataOutput,serverDir,toClientReplace);
                        break;
                    }
                }
                commands.remove(mark);
            }
            try {
                server.close();
                dataSocket.close();
                out.close();
                in.close();                           
                dataOutput.close();
                dataInput.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            serverDir.createState();
            serverDir.saveStateToFile(new File(config.getProperty(SERVER_STATE)));
            serverDir.setLastState();
            System.out.println("Completed!");
        }
    }    
}
