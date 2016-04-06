/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.TreeSet;

/**
 * Интерфейс удаленного доступа RMI
 */
interface ISyncService extends Remote {
    
    /**
     * Проверяет существование такого пользователя в базе данных и в случае успеха
     * утверждает пройденную авторизацию
     * @param user Пользователь
     * @return результат авторизации
     * @throws RemoteException 
     */
    public boolean authorization(ApplicationUser user) throws RemoteException;
    

    /**
     * Сообщает, пройдена ли авторизация
     * @return результат
     */
    public boolean isAuthorizationPassed() throws RemoteException;
    
    /**
     * Метод находит необходимые для синхронизации множества и сохраняет их в полях данных объекта
     * @param clientDir директория клиента (отдается удаленно)
     */
    public void sync(Directory clientDir) throws RemoteException;

    public TreeSet<FileProperties> getToClientReplace() throws RemoteException;

    public void setToClientReplace(TreeSet<FileProperties> toClientReplace) throws RemoteException;

    public TreeSet<FileProperties> getToClientRemove() throws RemoteException;

    public void setToClientRemove(TreeSet<FileProperties> toClientRemove) throws RemoteException;

    public TreeSet<FileProperties> getToServerReplace() throws RemoteException;

    public void setToServerReplace(TreeSet<FileProperties> toServerReplace) throws RemoteException;

    public TreeSet<FileProperties> getToServerRemove() throws RemoteException;

    public void setToServerRemove(TreeSet<FileProperties> toServerRemove) throws RemoteException;
    
    public void setServerDir(Directory serverDir) throws RemoteException;
    
    public Directory getServerDir() throws RemoteException;
    
    public void setServer(Server server) throws RemoteException ;
    
    public Server getServer() throws RemoteException ;
    
}
