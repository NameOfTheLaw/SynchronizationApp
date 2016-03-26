/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

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
        SyncThread syncThread = new SyncThread(config);
        syncThread.start();        
    }
    
    /**
     * метод для подгрузки параметров в config
     */
    public static void loadConfig() {
        config = new Config(CONFIG_PATH);
        System.out.println("Loading config.xml...");
        if (!config.loadFromXML()) {
            System.out.println("Config was not found");
            System.out.println("Creating standart config...");
            config.standartConfig();
            config.saveToXML();
        }
    }
    
    /**
     * метод инициализации конфига
     * @param args консольные аргументы
     * @return результат инициализации (успех\провал)
     */
    public static boolean initConfig(String[] args) {
        loadConfig();
        if (args.length >= 1) {               
            config.setStatus(args[0]);
        } else {
            return false;
        }        
        if (args.length > 1) {         
            if ((args.length + 1) % 2 == 0) {
                for (int i = 1; i < args.length; i=i+2) {
                    config.setProperty(args[i], args[i+1]);
                    System.out.println("\'"+args[i]+"\' - \'"+args[i+1]+"\' saved in config!");
                }
                config.saveToXML();
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
        if (initConfig(args)) {            
            syncDirectories();
        } else {
            System.out.println("Incorrect input. Please try again");
            System.out.println("java -jar SynchronizationApp.jar client/server [, key value ] ");
        }
    }
    
}
