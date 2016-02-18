/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

/**
 * отвечает за синхронизацию директорий
 * 
 * @author andrey
 */
public class Sync {
    
    public static void SyncDirectories(String root1, String laststate1, String root2, String laststate2){
        
        Directory dir1 = new Directory(root1);        
        Directory dir2 = new Directory(root2);
        
        if (dir1.loadLastState(new File(laststate1)) && dir2.loadLastState(new File(laststate2))) {
           
            dir1.createState();
            dir2.createState();
            
            dir1.syncWith(dir2);
            
            //compareSets(scan3,scan4,root1,root2);
        } else {
            
            dir1.createState();
            dir2.createState();
            
            dir1.saveStateToFile(new File(laststate1));
            dir2.saveStateToFile(new File(laststate2));
            
            dir1.syncWith(dir2);
            
            //compareSets(scan1,scan2,root1,root2);
        }
        
        return;
    }
}
