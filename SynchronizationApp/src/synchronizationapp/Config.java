/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Класс, реализующий подгрузку конфига и его запись
 * @author andrey
 */
class Config {
    private final String file;
    private Properties prop;

    /**
     * Конструктор класса
     * @param file путь к конфигу
     */
    public Config(String file) {
        this.file = file;
        prop = new Properties();
    }
    
    /**
     * Метод загрузки конфигурации из XML-файла
     */
    public void loadFromXML() {
        try (FileInputStream fs = new FileInputStream(file);) {
            prop.loadFromXML(fs);
        } catch (IOException ex) {
            
        }
    }

    /**
     * Метод сохранения конфигурации в XML-файл
     */
    public void saveToXML() {
        try (FileOutputStream fs = new FileOutputStream(file);) {
            prop.storeToXML(fs, "SynchronizationApp config");
        } catch (IOException ex) {
            
        }
    }
    
    /**
     * Метод для установки параметра конфигурации
     * @param key ключ
     * @param value значение
     */
    public void SetProperty(String key, String value) {
        prop.setProperty(key,value);
    }
    
    /**
     * Метод для просмотра значения параметра конфигурации
     * @param key
     * @return 
     */
    public String getProperty(String key) {
        return prop.getProperty(key);
    }    
}
