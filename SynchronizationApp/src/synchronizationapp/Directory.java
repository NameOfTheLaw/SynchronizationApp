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
import java.util.Iterator;
import java.util.Set;

/**
 * содержит информацию о директории и методы работы с ней
 * 
 * @author andrey
 */
public class Directory<P> {
    
    private P root;
    private HashSet<FileProperties> lastState=null,nowState=null;
    
    /**
     * конструктор класса
     * 
     * @param root путь к директории
     */
    Directory (P root) {
        this.root=root;
    }
    
    /**
     * Возвращает путь к директории
     * 
     * @return путь к директории
     */
    public P getPath() {
        return root;
    }
    
    /**
     * Возращает последнее состояние
     * 
     * @return последнее состояние
     */
    public Set<FileProperties> getLastState() {
        return lastState;
    }
    
    /**
     * Возращает настоящее состояние
     * 
     * @return настоящее состояние
     */
    public Set<FileProperties> getNowState() {
        return nowState;
    }
    
    /**
     * Загрузить последнее состояние директории
     * 
     * @param file файл состояния
     * @return статус результата операции
     */
    public boolean loadLastState(File file){
        try (FileInputStream fs = new FileInputStream(file);ObjectInputStream os = new ObjectInputStream(fs);) {
            if (!file.exists()) return false;
            lastState = (HashSet<FileProperties>)os.readObject();
            return true;
        } catch (IOException ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
    /**
     * Сохранить текущее состояние в файл
     * 
     * @param file файл для сохранения состояния
     * @return статус результата операции
     */
    public boolean saveStateToFile(File file){
        if (nowState!=null) {
            boolean result = false;
            try (FileOutputStream fs = new FileOutputStream(file);ObjectOutputStream os = new ObjectOutputStream(fs);) {
                os.writeObject(nowState);
                return true;
            } catch (IOException ex) {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     * Сканируется директория
     * 
     * @param path путь к директории
     * @param s множество с информацией о файлах и папках в директории
     */
    private void scanDir(String path, Set<FileProperties> s) {
        File dir = new File(path);
        File[] list = dir.listFiles();
        if (list == null) return;
        for (File f : list) {
            boolean isDirectory = f.isDirectory();
            long lastModified = f.isDirectory() ? 0 : f.lastModified();
            //long lastModified = f.lastModified();
            String fPath = f.getPath();
            if (isDirectory) {
                scanDir(fPath, s);
            }
            fPath = fPath.substring(((String)root).length()+1);
            s.add(new FileProperties(fPath, lastModified, isDirectory));
        }
    }
    
    /**
     * создает скан настоящего состояния директории
     */
    public void createState() {
        if (nowState==null) {
            nowState = new HashSet<>();
            scanDir((String)root,nowState);
        }
    }
    
    /**
     * синхронизирует файлы директорий
     * 
     * @param other директория для синхронизии
     */
    public void syncWith(Directory other) {
        Set<FileProperties> set1 = new HashSet<>();
        Set<FileProperties> set2 = new HashSet<>();
        //ignoring lastState
        set1.addAll(nowState);
        set2.addAll(other.getNowState());
        set1.removeAll(other.getNowState());
        set2.removeAll(nowState);
        
        while (!set1.isEmpty() || !set2.isEmpty()) {
            Iterator iter1 = set1.iterator();
            Iterator iter2 = set2.iterator();
            boolean f;
            while (iter1.hasNext()) {              
                FileProperties file1 = (FileProperties)iter1.next();
                FileProperties file2;
                f=true;
                while (f) {
                    if (!iter2.hasNext()) {
                        f=false;
                    } else {
                        file2 = (FileProperties)iter2.next();
                        if (file1.getPath().equals(file2.getPath())) {
                            if ((long)file1.getModifiedTime()>(long)file2.getModifiedTime()) {
                                try {
                                    Files.copy(Paths.get(root+File.separator+(String)file1.getPath()), Paths.get((String)other.getPath()+File.separator+(String)file2.getPath()), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException ex) {
                                    
                                }
                            } else {
                                try {
                                    Files.copy(Paths.get((String)other.getPath()+File.separator+(String)file2.getPath()), Paths.get(root+File.separator+(String)file1.getPath()), StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException ex) {
                                    
                                }
                            }
                            iter1.remove();
                            iter2.remove();
                        }
                    }
                }
            }
        }
        
        System.out.println("-----");
        for (FileProperties fi: set1) {
            System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
        }
        System.out.println("-----");
        for (FileProperties fi: set2) {
            System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
        }
        System.out.println("-----");
    }
}
