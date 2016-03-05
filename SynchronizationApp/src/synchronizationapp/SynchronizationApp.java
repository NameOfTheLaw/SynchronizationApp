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

    /**
     * метод используется для синхронизации двух директорий
     */
    public static void syncDirectories(){    
        
        //реализация с потоком ~ 40-80
        
        SyncThread syncThread = new SyncThread(config);
        syncThread.start();
        
        
        //реализация без потока ~ 70-110
        /*
        Directory dir1 = new Directory(config.getProperty("root1"));       
        Directory dir2 = new Directory(config.getProperty("root2"));

        if (dir1.loadLastState(new File(config.getProperty("lastState1"))) && dir2.loadLastState(new File(config.getProperty("lastState2")))) {           
            dir1.createState();
            dir2.createState();

            dir1.syncWith(dir2);           

            dir1.createState();
            dir2.createState();

            dir1.saveStateToFile(new File(config.getProperty("lastState1")));
            dir2.saveStateToFile(new File(config.getProperty("lastState2")));
        } else {            
            dir1.createState(); dir1.setLastState();
            dir2.createState(); dir2.setLastState();

            dir1.syncWith(dir2);

            dir1.createState();
            dir2.createState();

            dir1.saveStateToFile(new File(config.getProperty("lastState1")));
            dir2.saveStateToFile(new File(config.getProperty("lastState2")));
        }
        */
    }
    
    /**
     * метод для подгрузки параметров в config
     */
    public static void loadConfig() {
        config = new Config(CONFIG_PATH);
        if (!config.loadFromXML()) {
            config.standartConfig();
        }
    }
    
    /**
     * метод инициализации конфига
     * @param args консольные аргументы
     * @return результат инициализации (успех\провал)
     */
    public static boolean initConfig(String[] args) {        
        if (args.length == 0) {
            loadConfig();
        } else {
            if (args.length % 2 == 0) {
                for (int i = 0; i < args.length; i=i+2) {
                    config.setProperty(args[i], args[i+1]);
                    System.out.println("\'"+args[i]+"\' - \'"+args[i+1]+"\' saved");
                }
            } else {
                return false;
            }
        }
        return true;
    }
    
    /**
     * основной метод приложения
     * @param args the command line arguments
     */
    public static void main(String[] args){
        long time = System.currentTimeMillis();
        if (initConfig(args)) {            
            syncDirectories();
        } else {
            System.out.println("Incorrect input. try again");
            System.out.println("java -jar SynchronizationApp.jar [, key value ] ");
        }
        time = System.currentTimeMillis() - time;
        System.out.println(time);
    }
    
}
