/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrey
 */
abstract class WebMember extends Thread{
    
    protected Config config;
    protected final String CLIENT_ROOT="root1";
    protected final String CLIENT_STATE="lastState1";
    protected final String SERVER_ROOT="root2";
    protected final String SERVER_STATE="lastState2";
    
    protected void sendFile(Socket socket, Directory dir, String string) {
        try (
                FileInputStream is = new FileInputStream(dir.getPath()+File.separator+string);
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                ){
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
        } catch (IOException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("-nope");
        }
    }
        
    protected void receiveFile(Socket socket, Directory dir, String string) {
        try (
                FileOutputStream os = new FileOutputStream(dir.getPath()+File.separator+string);
                DataInputStream is = new DataInputStream(socket.getInputStream());
                ){
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
        } catch (IOException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("-nope");
        }
    }
}
