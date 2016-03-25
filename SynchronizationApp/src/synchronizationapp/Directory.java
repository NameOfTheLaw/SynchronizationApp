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
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * содержит информацию о директории и методы работы с ней
 * 
 * @author andrey
 */
public class Directory<P> implements Serializable{
    
    private P root;
    private TreeSet<FileProperties> lastState=null,nowState=null;
    
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
            lastState = (TreeSet<FileProperties>)os.readObject();
            return true;
        } catch (IOException ex) {
            return false;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
    /** Устанавливает последнее состояние системы равным настоящему
     *
     * @return результат операции (удачный\неудачный)
     */
    public boolean setLastState() {
        if (nowState != null) {
            lastState = (TreeSet)nowState.clone();
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Сохраняет текущее состояние в файл
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
     * Создает скан настоящего состояния директории
     */
    public void createState() {
        nowState = new TreeSet<>();
        scanDir((String)root,nowState);
    }
    
    public boolean copyFile(P path1, P path2) {
        try {
            Files.copy(Paths.get((String)path1), Paths.get((String)path2));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    public boolean deleteFile(P path) {        
        try {
            Files.delete(Paths.get((String)path));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    /**
     * Сравнивает две директории на основе их уникальных файлов и создает коллекции 
     * необходимых файлов для удаления\замены на основе последних состояний системы
     * 
     * @param dir1
     * @param dir2
     * @param set1
     * @param toReplace
     * @param toRemove 
     */
    private void checkAddOrDelete(Directory dir1, Directory dir2, TreeSet<FileProperties> set1, TreeSet<FileProperties> toReplace, TreeSet<FileProperties> toRemove){
        dir2.setFullEquals();
        FileProperties fi1;
        Iterator iter1;
        iter1 = set1.iterator();
        while (iter1.hasNext()) {
            fi1 = (FileProperties)iter1.next();
            if (dir2.getLastState().contains(fi1)) {
                toRemove.add(fi1);
            } else {
                toReplace.add(fi1);                
            }
        }
    }
    
    /**
     * 
     * 
     * @param dir1
     * @param dir2
     * @param set1
     * @param set2
     * @param toClientReplace
     * @param toServerReplace 
     */
    public void checkBothChanged(Directory dir1, Directory dir2, TreeSet<FileProperties> set1, TreeSet<FileProperties> set2, TreeSet<FileProperties> toServerReplace, TreeSet<FileProperties> toClientReplace) {
        Iterator iter1 = set1.iterator();
        Iterator iter2 = set2.iterator();
        
        FileProperties file1;
        FileProperties file2;
        while (iter1.hasNext()) {
            if (iter2.hasNext()) {
                file1 = (FileProperties)iter1.next();
                file2 = (FileProperties)iter2.next();
                file1.setFullEquals();
                file2.setFullEquals();
                if (file1.equals(file2)) {
                    iter1.remove();
                    iter2.remove();
                } else {
                    file1.setPathEquals();
                    file2.setPathEquals();
                    if (file1.equals(file2)) {
                        if ((long)file1.getModifiedTime()>(long)file2.getModifiedTime()) {
                            toClientReplace.add(file1);
                            //toServerReplace.add(file1);
                        } else {
                            toServerReplace.add(file2);   
                            //toClientReplace.add(file2);                         
                        }
                        iter1.remove();
                        iter2.remove();
                    }                    
                }
            }
        }
    }
    
    /**
     * Создает необходимые для дальнейшей синхронизации коллекции элементов    
     * 
     * @param other директория для синхронизии
     * @param toClientReplace
     * @param toClientRemove
     * @param toServerReplace
     * @param toServerRemove
     */
    public void syncWith(Directory other, TreeSet<FileProperties> toClientReplace, TreeSet<FileProperties> toClientRemove, TreeSet<FileProperties> toServerReplace, TreeSet<FileProperties> toServerRemove) {
        TreeSet<FileProperties> set1 = new TreeSet<>();
        TreeSet<FileProperties> set2 = new TreeSet<>();
        TreeSet<FileProperties> set3 = new TreeSet<>();
        TreeSet<FileProperties> set4 = new TreeSet<>();
        
        set1.addAll(nowState);
        set1.removeAll(other.getNowState());
        
        set2.addAll(other.getNowState());
        set2.removeAll(nowState);
        
        set3.addAll(nowState);
        set4.addAll(other.getNowState());
        
        set3.retainAll(set4);
        set4.retainAll(set3);
                
        checkBothChanged(this,other,set3,set4,toServerReplace,toClientReplace);
        
        checkAddOrDelete(this,other,set1,toClientReplace,toServerRemove);
        checkAddOrDelete(other,this,set2,toServerReplace,toClientRemove);
        
        /*
        checkAddOrDelete(this,other,set1,toServerReplace,toServerRemove);
        checkAddOrDelete(other,this,set2,toClientReplace,toClientRemove);
        */
    }

    public void deleteAll(TreeSet<FileProperties> set) {
        for (FileProperties fi : set.descendingSet()) {
            System.out.println("deleting "+(String)fi.getPath()+"...");
            deleteFile((P)((String)this.getPath()+File.separator+(String)fi.getPath()));
        }
    }
    
    /**
     * устанавливает элементам всех снимков уровень сравнения fullEquals
     */
    public void setFullEquals() {
        for (FileProperties fi: nowState) {
            fi.setFullEquals();
        }
        for (FileProperties fi: lastState) {
            fi.setFullEquals();
        }
    }
}
