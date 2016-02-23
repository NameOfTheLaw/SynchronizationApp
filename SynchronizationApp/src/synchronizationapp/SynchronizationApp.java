/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.File;

/**
 *
 * @author andrey
 */
public class SynchronizationApp {
    public static final String CONFIG_PATH = "E:\\ITMO\\2_kurs\\programs\\SynchronizationApp\\SynchronizationApp\\config\\config.xml";
    public static Config config;
    public static String root1,root2,lastState1,lastState2;

    /**
     * метод используется для синхронизации двух директорий
     */
    public static void syncDirectories(){
        
        Directory dir1 = new Directory(root1);        
        Directory dir2 = new Directory(root2);
        
        if (dir1.loadLastState(new File(lastState1)) && dir2.loadLastState(new File(lastState2))) {           
            dir1.createState();
            dir2.createState();
            
            dir1.syncWith(dir2);            
            
            dir1.createState();
            dir2.createState();
            
            dir1.saveStateToFile(new File(lastState1));
            dir2.saveStateToFile(new File(lastState2));
        } else {            
            dir1.createState();
            dir2.createState();
            
            dir1.syncWith(dir2);
            
            dir1.createState();
            dir2.createState();
            
            dir1.saveStateToFile(new File(lastState1));
            dir2.saveStateToFile(new File(lastState2));
        }
    }
    
    /**
     * метод для подгрузки параметров из конфига
     */
    public static void loadConfig() {
        config = new Config(CONFIG_PATH);
        config.loadFromXML();
        root1 = config.getProperty("root1");
        root2 = config.getProperty("root2");
        lastState1 = config.getProperty("lastState1");
        lastState2 = config.getProperty("lastState2");
    }
    
    /**
     * основной метод приложения
     * @param args the command line arguments
     */
    public static void main(String[] args){
        loadConfig();
        syncDirectories();
    }
    
}
