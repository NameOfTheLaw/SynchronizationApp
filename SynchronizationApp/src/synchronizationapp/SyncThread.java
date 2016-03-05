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
public class SyncThread extends Thread{
    
    private Config config;

    public SyncThread(Config config) {
        this.config = config;
    }
    
    public void sync() {
        if (config != null) {
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
        }
    }

    @Override
    public void run() {
        sync();
    }    
}
