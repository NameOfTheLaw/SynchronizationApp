/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.IOException;

/**
 *
 * @author andrey
 */
public class SynchronizationApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        // TODO code application logic here
        Sync.SyncDirectories(args[0], args[1], args[2], args[3]);
    }
    
}
