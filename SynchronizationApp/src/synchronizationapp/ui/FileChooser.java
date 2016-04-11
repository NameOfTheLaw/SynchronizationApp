/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronizationapp.ui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 *
 * @author andrey
 */
public class FileChooser extends JFrame {
    
    ClientJFrame frame;
    File file;
    
    FileChooser (ClientJFrame frame) {
        file = null;
        this.frame = frame;
        setBounds(0, 0, 500, 500);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.showOpenDialog(this);
        file = fileChooser.getSelectedFile();
    }
    
    public File getDirectory() {
        if (file != null) {
            return file;
        }
        return null;
    }
    
    public class myFileFilter extends javax.swing.filechooser.FileFilter {
    
        String description;

        @Override
        public String getDescription() {
            return description;
        }

        myFileFilter(String description) {
            this.description = description;
        }
        
        @Override
        public boolean accept(File f) {
            if(f != null) {
                if(f.isDirectory()) {
                    return true;
                }
            }
            return false;
        }
    }
}
