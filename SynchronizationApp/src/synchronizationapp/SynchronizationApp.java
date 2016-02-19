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

    /**
     * метод используется для синхронизации двух директорий
     * 
     * @param root1 путь к первой директории
     * @param laststate1 путь к последнему снимку первой директории
     * @param root2 путь ко второй директории
     * @param laststate2 путь к последнему снимку второй директории
     */
    public static void SyncDirectories(String root1, String laststate1, String root2, String laststate2){
        
        Directory dir1 = new Directory(root1);        
        Directory dir2 = new Directory(root2);
        
        if (dir1.loadLastState(new File(laststate1)) && dir2.loadLastState(new File(laststate2))) {           
            dir1.createState();
            dir2.createState();
            
            dir1.syncWith(dir2);            
            
            dir1.createState();
            dir2.createState();
            
            dir1.saveStateToFile(new File(laststate1));
            dir2.saveStateToFile(new File(laststate2));
        } else {            
            dir1.createState();
            dir2.createState();
            
            dir1.syncWith(dir2);
            
            dir1.createState();
            dir2.createState();
            
            dir1.saveStateToFile(new File(laststate1));
            dir2.saveStateToFile(new File(laststate2));
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        // TODO code application logic here
        SyncDirectories(args[0], args[1], args[2], args[3]);
    }
    
}
