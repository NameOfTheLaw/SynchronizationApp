/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.Serializable;

/**
 * класс для хранения информации о файле или катологе
 * 
 * @author andrey
 */
public class FileProperties<P, T, D> implements Serializable {
    private P fPath;
    private T fTime;
    private D fType;
    
    /**
     * конструктор класса
     * 
     * @param fPath путь к файлу
     * @param fTime время последнего изменения
     * @param fType тип файла
     */
    FileProperties(P fPath, T fTime, D fType) {
        this.fPath=fPath;
        this.fTime=fTime;
        this.fType=fType;
    }
    
    @Override
    public int hashCode(){
        int hash = 37;
        hash = hash*17 + (fPath == null ? 0 : fPath.hashCode());
        hash = hash*17 + (fType == null ? 0 : fType.hashCode());
        hash = hash*17 + (fTime == null ? 0 : fTime.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FileProperties<P, T, D> other = (FileProperties<P, T, D>) obj;
        if (!this.fPath.equals(other.fPath)) {
            return false;
        }
        if (!this.fType.equals(other.fType)) {
            return false;
        }
        if (!this.fTime.equals(other.fTime)) {
            return false;
        }
       return true;
    }
    
    public long compareTo(FileProperties<P, T, D> f) {    
        return (Long)this.fTime - (Long)f.fTime;
    }
    
    /**
     * возвращает путь к Файлу
     * 
     * @return путь к файлу
     */
    public P getPath() {
        return fPath;
    }
    
    /**
     * возращает время последнего изменения
     * 
     * @return время последнего изменения
     */
    public T getModifiedTime() {
        return fTime;
    }
    
    /**
     * возвращает тип файла
     * 
     * @return тип файла
     */
    public D isDirectory() {
        return fType;
    }
}
