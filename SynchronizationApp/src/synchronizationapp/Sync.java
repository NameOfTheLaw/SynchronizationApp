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
    
    private static Set<FileProperties> loadFromFile(File file){
        HashSet<FileProperties> set = null;
        try (FileInputStream fs = new FileInputStream(file);ObjectInputStream os = new ObjectInputStream(fs);) {
            if (!file.exists()) return null;
            set = (HashSet<FileProperties>)os.readObject();
        } catch (IOException ex) {
        
        } catch (ClassNotFoundException ex) {
            
        }
        return set;
    }
    
    private static boolean saveToFile(Set<FileProperties> set, File file){
        boolean result = false;
        try (FileOutputStream fs = new FileOutputStream(file);ObjectOutputStream os = new ObjectOutputStream(fs);) {
            os.writeObject(set);
            result=true;
        } catch (IOException ex) {
        
        }
        return result;
    }
    
    private static void scanDir(String root, String path, Set<FileProperties> s) {
        File dir = new File(path);
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            boolean isDirectory = f.isDirectory();
            long lastModified = f.isDirectory() ? 0 : f.lastModified();
            //long lastModified = f.lastModified();
            String fPath = f.getPath();
            if (isDirectory) {
                scanDir(root, fPath, s);
            }
            fPath = fPath.substring(root.length()+1);
            s.add(new FileProperties(fPath, lastModified, isDirectory));
        }
    }
    
    
    
    private static void compareSets(Set<FileProperties> list1, Set<FileProperties> list2, String path1, String path2) {
        for (FileProperties fi1: list1) {
            for (FileProperties fi2: list2) {
                if (!fi1.equals(fi2)) {
                    if (fi1.getPath().equals(fi2.getPath()) && !(boolean)fi1.isDirectory() && !(boolean)fi2.isDirectory()) {
                        if ((long)fi1.getModifiedTime()>(long)fi2.getModifiedTime()) {
                            System.out.println("replacing:");
                            System.out.println(path1+File.separator+(String)fi1.getPath());
                            System.out.println(path2+File.separator+(String)fi2.getPath());
                            System.out.println("---------------------"); 
                            try {
                                Files.copy(Paths.get(path1+File.separator+(String)fi1.getPath()), Paths.get(path2+File.separator+(String)fi2.getPath()), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ex) {
                                
                            }
                        } else {
                            System.out.println("replacing:");
                            System.out.println(path2+File.separator+(String)fi2.getPath());
                            System.out.println(path1+File.separator+(String)fi1.getPath());
                            System.out.println("---------------------");                            
                            try {
                                Files.copy(Paths.get(path2+File.separator+(String)fi2.getPath()), Paths.get(path1+File.separator+(String)fi1.getPath()), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ex) {
                                
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void SyncDirectories(String root1, String laststate1, String root2, String laststate2){
        File dir1 = new File(root1);
        File dir2 = new File(root2);
        File ls1 = new File(laststate1);
        File ls2 = new File(laststate2);
        
        Set<FileProperties> scan1,scan2;
        
        if (ls1.exists() && ls2.exists()) {
            System.out.println("1");
            scan1 = loadFromFile(ls1);
            scan2 = loadFromFile(ls2);
            
            Set<FileProperties> scan3 = new HashSet<>();
            Set<FileProperties> scan4 = new HashSet<>();
            
            scanDir(root1,root1,scan3);
            scanDir(root2,root2,scan4);
            
            compareSets(scan3,scan4,root1,root2);
            /*
            for (FileProperties fi: list1) {
                System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
            }
            System.out.println("-----");
            for (FileProperties fi: list2) {
                System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
            }
            */
        } else {
            System.out.println("2");
            scan1 = new HashSet<>();
            scan2 = new HashSet<>();
            scanDir(root1,root1,scan1);
            scanDir(root2,root2,scan2);
            saveToFile(scan1,ls1);
            saveToFile(scan2,ls2);
            
            compareSets(scan1,scan2,root1,root2);
            //..сравниваем множества и синхронизируем
            
            for (FileProperties fi: scan1) {
                System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
            }
            System.out.println("-----");
            for (FileProperties fi: scan2) {
                System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
            }
        }
        
        return;
    }
}
