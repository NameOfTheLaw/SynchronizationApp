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
public class FileProperties<P, T, D> implements Serializable, Comparable {
    private P fPath;
    private T fTime;
    private D fType;
    private boolean fullEquals;
    
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
        fullEquals = false;
    }
    
    /**
     * хэш-функция
     * 
     * @return уникальный код для конкретного набора fPath, fTime, fType
     */
    @Override
    public int hashCode(){
        int hash = 37;
        hash = hash*17 + (fPath == null ? 0 : fPath.hashCode());
        if (fullEquals) {
            hash = hash*17 + (fType == null ? 0 : fType.hashCode());
            hash = hash*17 + (fTime == null ? 0 : fTime.hashCode());
        }
        return hash;
    }
    
    /**
     * метод для установления равенства объектов класса
     * 
     * @param obj объект, с которым необходимо проверить равенство
     * @return результат установления равенства
     */
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
            //System.out.println("Не равны"+this.fPath+" "+other.fPath);
            return false;
        } else {
            //System.out.println("Равны"+this.fPath+" "+other.fPath);            
        }
        if (fullEquals) {
            //System.out.println("0");
            if (!this.fType.equals(other.fType)) {
                return false;
            }
            if (!this.fTime.equals(other.fTime)) {
                return false;
            }
        } else {            
            //System.out.println("1");
        }
        return true;
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
    
    /**
     * устанавливает полный уровень сравнения
     */
    public void setFullEquals() {
        fullEquals = true;
    }
    
    /**
     * устанавливает частичный уровень сравнения
     */
    public void setPathEquals() {
        fullEquals = false;
    }
    
    /**
     * возвращает уровень сравнения
     * @return полный уровень сравнения (да\нет)
     */
    public boolean getFullEquals() {
        return fullEquals;
    }

    /**
     * переопределение метода сравнения объектов класса (используется в TreeSet)
     * @param t объект для сравнения
     * @return результат сравнения
     */
    @Override
    public int compareTo(Object t) {
        return ((String)this.getPath()).compareTo((String)((FileProperties)t).getPath());
    }
}
