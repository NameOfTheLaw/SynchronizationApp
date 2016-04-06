/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Класс, осуществляющий удаленный доступ клиента на сервер
 */
public class SyncService extends UnicastRemoteObject implements ISyncService {
    
    boolean authorizationPassed;
    TreeSet<FileProperties> toClientReplace, toClientRemove, toServerReplace, toServerRemove;
    Directory serverDir;
    Server server;

    public Directory getServerDir() {
        return serverDir;
    }

    public void setServerDir(Directory serverDir) {
        this.serverDir = serverDir;
    }

    public TreeSet<FileProperties> getToClientReplace() {
        return toClientReplace;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setToClientReplace(TreeSet<FileProperties> toClientReplace) {
        this.toClientReplace = toClientReplace;
    }

    public TreeSet<FileProperties> getToClientRemove() {
        return toClientRemove;
    }

    public void setToClientRemove(TreeSet<FileProperties> toClientRemove) {
        this.toClientRemove = toClientRemove;
    }

    public TreeSet<FileProperties> getToServerReplace() {
        return toServerReplace;
    }

    public void setToServerReplace(TreeSet<FileProperties> toServerReplace) {
        this.toServerReplace = toServerReplace;
    }

    public TreeSet<FileProperties> getToServerRemove() {
        return toServerRemove;
    }

    public void setToServerRemove(TreeSet<FileProperties> toServerRemove) {
        this.toServerRemove = toServerRemove;
    }

    /**
     * Конструктор класса
     * @throws RemoteException 
     */
    public SyncService() throws RemoteException {
        authorizationPassed = false;
        toClientReplace = new TreeSet<FileProperties>();
        toClientRemove = new TreeSet<FileProperties>();
        toServerReplace = new TreeSet<FileProperties>();
        toServerRemove = new TreeSet<FileProperties>();
        server = null;
        serverDir = null;
    }
    
    @Override
    public boolean authorization(ApplicationUser user) throws RemoteException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SynchronizationAppPU");            
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        List<ApplicationUser> users = (List<ApplicationUser>)em.createQuery("from ApplicationUser").getResultList();
        em.getTransaction().commit();
        em.close();
        emf.close();
        if (users.contains(user)) {
            authorizationPassed = true;
            return authorizationPassed;
        } else {
            authorizationPassed = false;
            return authorizationPassed;
        }
    }
    
    @Override
    public boolean isAuthorizationPassed() {
        return authorizationPassed;
    }
    
    @Override
    public void sync(Directory clientDir) {
        serverDir.createState();
        serverDir.syncWith(clientDir, toClientReplace, toClientRemove, toServerReplace, toServerRemove);
        int changes = toClientReplace.size() + toClientRemove.size() + toServerReplace.size() + toServerRemove.size();
        System.out.println("I have found " + changes + " changes");
        //будим сервер
        server.resume();
    }
    
}
