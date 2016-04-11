/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import javax.swing.WindowConstants;
import synchronizationapp.ui.ClientJFrame;

/**
 * Класс-поток синхронизации. Запускает либо клиентскую часть приложения, 
 * либо серверную (согласно первому аргументу командной строки)
 * @author andrey
 */
public class SyncThread extends Thread{
    
    private Config config;

    /**
     * Конструктор потока синхронизации
     * @param config конфиг
     */
    public SyncThread(Config config) {
        this.config = config;
    }

    @Override
    public void run() {
        if (config != null) {
            if (config.getStatus().equals("server")) {
                Server server = new Server(config);                        
                System.out.println("Starting server...");
                server.start();
            } else if (config.getStatus().equals("client")) {                        
                System.out.println("Starting client...");       
                /*
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {                
                        ClientJFrame clientFrame = new ClientJFrame(client);
                        clientFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                        clientFrame.setVisible(true);
                    }
                });
                */
                ClientJFrame clientFrame = new ClientJFrame(config);
                clientFrame.setVisible(true);
                //client.start();
            }            
        }
    }    
}
