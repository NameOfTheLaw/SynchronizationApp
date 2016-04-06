/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Абстрактный класс, содержащий методы передачи файлов по сети
 * @author andrey
 */
abstract class WebMember extends Thread{
    
    protected Config config;
    protected final String CLIENT_ROOT="root2";
    protected final String CLIENT_STATE="lastState2";
    protected final String SERVER_ROOT="root1";
    protected final String SERVER_STATE="lastState1";
    
    protected Socket commandSocket, objectSocket, dataSocket;
    protected OutputStream out;
    protected InputStream in;
    protected PrintWriter textOutput;
    protected DataOutputStream dataOutput;
    protected DataInputStream dataInput;
    protected ObjectOutputStream objectOutput;
    protected ObjectInputStream objectInput;
    protected ServerSocket server;
    protected BufferedReader textInput;
        
    /**
     * Метод, читающий размер файла, а затем сам файл с входного потока is, 
     * и записывающий его (файл) в директорию dir согласно параметрам fp.
     * @param is входной поток
     * @param dir директория для вставки
     * @param fp параметры файла
     */
    protected void receiveFile(DataInputStream is, Directory dir, FileProperties fp) {
        File file = new File(dir.getPath()+File.separator+(String)fp.getPath());
        if ((boolean)fp.isDirectory()) {
            file.mkdir();
        } else {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                //ignore
            }
            try (FileOutputStream os = new FileOutputStream(file);){            
                System.out.print("Saving " + (String)fp.getPath() + "... ");
                long length = is.readLong();
                long now = 0;
                int readedBytesCount, total = 0;
                byte[] buffer = new byte[1000];
                while ((readedBytesCount = is.read(buffer, 0, Math.min(buffer.length, (int)length-total))) != -1) {
                    total += readedBytesCount;
                    os.write(buffer, 0, readedBytesCount);

                    if (total == (int)length){
                        break;
                    }
                }
                System.out.print("file has been saved");
            } catch (SocketException ex) {            
                System.out.print("file has been saved*");
            } catch (IOException ex) {            
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println();
        }
    }
    
    /**
     * Метод, считывающий файл из директории dir согласно параметрам fp, 
     * и отправляющий его размер (файла), а затем сам файл на поток is.
     * @param os выходной поток
     * @param dir директория с файлом
     * @param fp параметры файла
     */
    protected void sendFile(DataOutputStream os, Directory dir, FileProperties fp) {
        if (!(boolean)fp.isDirectory()) {
            try (FileInputStream is = new FileInputStream(dir.getPath()+File.separator+(String)fp.getPath());) {
                File file = new File(dir.getPath()+File.separator+(String)fp.getPath());
                long length = file.length();            
                System.out.print("Sending " + (String)fp.getPath() + "("+length+"bytes)... ");
                os.writeLong(length);
                byte[] buffer = new byte[1000];
                while (true){
                    int readedBytesCount = is.read(buffer);
                    if (readedBytesCount == -1) {
                        break;
                    }
                    if (readedBytesCount > 0) {
                        os.write(buffer, 0, readedBytesCount);
                    }
                }            
                System.out.print("sending has been finished");
            } catch (SocketException ex) {
                System.out.print("sending has been finished*");
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println();
        }
    }
    
    /**
     * Метод отправляющий файлы согласно параметрам из коллекции set, 
     * лежащие в директории dir, на поток dataOutput.
     * @param dataOutput выходной поток
     * @param dir директория с файлами
     * @param set параметры файлов
     */
    protected void sendFiles(DataOutputStream dataOutput, Directory dir, Set<FileProperties> set) {        
        for (FileProperties fp : set) {
            sendFile(dataOutput,dir,fp);
        }
    }
    
    /**
     * Метод, принимающий файлы с потока dataInput согласно параметрам из коллекции set
     * и сохраняющий их в директорию dir
     * @param dataInput входной поток
     * @param dir директория для вставки файлов
     * @param set параметры файлов
     */
    protected void receiveFiles(DataInputStream dataInput, Directory dir, Set<FileProperties> set) {   
        for (FileProperties fp : set) {
            receiveFile(dataInput,dir,fp);
        }
    }    
    
    /**
     * Перевод строки в MD5 хеш
     * @param s строка
     * @return MD5 хеш
     */
    private static String MD5(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // ignore
        }
        return null;
    }
    
    /**
     * Метод инициализирует потоки для передачи команд
     */
    abstract protected void initCommandTransferring();
    
    /**
     * Метод деинициализирует потоки для передачи команд
     */
    abstract protected void deinitCommandTransferring();
        
    /**
     * Метод инициализирует потоки для передачи объектов
     */
    abstract protected void initObjectTransferring();
    
    /**
     * Метод деинициализирует потоки для передачи объектов
     */
    abstract protected void deinitObjectTransferring();
    
    /**
     * Метод инициализирует потоки для передачи файлов
     */
    abstract protected void initFileTransferring();
    
    /**
     * Метод деинициализирует потоки для передачи файлов
     */
    abstract protected void deinitFileTransferring();
}
