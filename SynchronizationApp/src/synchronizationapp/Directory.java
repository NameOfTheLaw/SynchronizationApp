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
        nowState = new HashSet<>();
        scanDir((String)root,nowState);
    }
    
    /**
     * Сравнивает две директории на основе их уникальных файлов и удаляет\создает нужные
     * 
     * @param dir1 первая директория
     * @param dir2 вторая директория
     * @param set1 множество уникальных файлов первой директории
     * @param set2 множество уникальных файлов второй директории
     */
    private void checkAddOrDelete(Directory dir1, Directory dir2, Set<FileProperties> set1, Set<FileProperties> set2){        
        Iterator iter1;
        while (!set1.isEmpty() && dir2.getLastState()!=null) {
            iter1=set1.iterator();
            do {
                boolean meet=false;
                FileProperties setFile=(FileProperties)iter1.next();
                for (FileProperties stateFile: (Set<FileProperties>)dir2.getLastState()) {
                    if (setFile.getPath().equals(stateFile.getPath())) {
                        meet=true;
                        try {
                            Files.delete(Paths.get((String)dir1.getPath()+File.separator+(String)setFile.getPath()));
                            iter1.remove();
                        } catch (IOException ex) {
                            
                        }
                    }
                }
                if (!meet) {
                    try {
                        Files.copy(Paths.get((String)dir1.getPath()+File.separator+(String)setFile.getPath()), Paths.get((String)dir2.getPath()+File.separator+(String)setFile.getPath()));
                        iter1.remove();
                    } catch (IOException ex) {
                        
                    }
                }
            } while (iter1.hasNext());
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
        
        set1.addAll(nowState);
        set2.addAll(other.getNowState());
        set1.removeAll(other.getNowState());
        set2.removeAll(nowState);        
        
        /*
        System.out.println("-----");
        for (FileProperties fi: set1) {
            System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
        }
        System.out.println("-----");
        for (FileProperties fi: set2) {
            System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
        }
        System.out.println("-----");
        */
        
        boolean f=true;
        Iterator iter1;
        Iterator iter2;
        
        while (f) {
            f=false;
            iter1 = set1.iterator();
            while (iter1.hasNext()) {            
                FileProperties file1=(FileProperties)iter1.next(), file2=null;
                iter2 = set2.iterator();
                while (iter2.hasNext()) {
                    file2=(FileProperties)iter2.next();
                    if (file1.getPath().equals(file2.getPath()) && !(boolean)file1.isDirectory() && !(boolean)file2.isDirectory()) {
                        if ((long)file1.getModifiedTime()>(long)file2.getModifiedTime()) {
                            try {
                                Files.copy(Paths.get(root+File.separator+(String)file1.getPath()), Paths.get((String)other.getPath()+File.separator+(String)file2.getPath()), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ex) {

                            }
                            f=true;
                        } else {
                            try {
                                Files.copy(Paths.get((String)other.getPath()+File.separator+(String)file2.getPath()), Paths.get(root+File.separator+(String)file1.getPath()), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ex) {

                            }
                            f=true;
                        }
                        iter1.remove();
                        iter2.remove();
                    }
                }
            }
        }
        
        checkAddOrDelete(this,other,set1,set2);
        checkAddOrDelete(other,this,set2,set1);
        
        /*
        System.out.println("-----");
        for (FileProperties fi: set1) {
            System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
        }
        System.out.println("-----");
        for (FileProperties fi: set2) {
            System.out.println(fi.getPath()+" "+fi.getModifiedTime()+" "+fi.isDirectory());
        }
        System.out.println("-----");
        */
    }
}
