/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backass;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
/**
 *
 * @author Pita
 */
public class FileRecord {
    private static final int MYTHREADS = 40;
    private Connection con = null;
    private Statement st = null;
    private ResultSet rs = null;
    
    private String url;
    private Config config;
    private String user;
    private String password;
        
    public FileRecord(Config config)
    {
        url = "jdbc:mysql://"+config.GetValue("Server")+":"+config.GetValue("Port")+"/"+config.GetValue("Database");
        user = config.GetValue("Username");
        password = config.GetValue("Password");
        this.config = config;
         //System.out.println(GetFiles());
    }
    
    public void BuildDB()
    {
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            
            st.execute("DROP TABLE `backass`.`ba_file_archive`;");
            st.execute("DROP TABLE `backass`.`ba_file_version`;");
            st.execute("DROP TABLE `backass`.`ba_filelist`;");
            
            st.execute("CREATE TABLE `ba_file_archive` (\n" +
            "  `file_archive_id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `fileid` int(10) unsigned DEFAULT NULL,\n" +
            "  `version` varchar(10) DEFAULT NULL,\n" +
            "  `path` varchar(200) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`file_archive_id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            
            
            st.execute("CREATE TABLE `ba_file_version` (\n" +
            "  `fileid` int(10) unsigned NOT NULL,\n" +
            "  `version` int(10) unsigned DEFAULT NULL,\n" +
            "  `version_date` datetime DEFAULT NULL,\n" +
            "  PRIMARY KEY (`fileid`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            
            st.execute("CREATE TABLE `ba_filelist` (\n" +
            "  `fileid` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `filename` varchar(45) NOT NULL,\n" +
            "  `sourcepath` varchar(255) DEFAULT NULL,\n" +
            "  `parentpath` varchar(255) DEFAULT NULL,\n" +
            "  `filepath` varchar(255) NOT NULL,\n" +
            "  `last_modified` datetime DEFAULT NULL,\n" +
            "  `ext` varchar(15) DEFAULT NULL,\n" +
            "  `filesize` int(10) unsigned DEFAULT NULL,\n" +
            "  PRIMARY KEY (`fileid`)\n" +
            ") ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;");
        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } 
    }
    
    /*
    public String GetFiles()
    {
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
               return (rs.getString(1));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
        } 
        return "";
    }
    */
    
    public void addFile(String filepath,String SourcePath, String BackupBath, String ArchivePath, javax.swing.JTextArea Filelist)
    {
        /*
        ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
        FileBackuper fileadd = new FileBackuper(filepath,backpath,archivepath);
        executor.execute(fileadd);
        executor.shutdown();
                */
        File[] files;  
        File file = new File(filepath);
        String AddPath;
        if(file.isFile())
        {
            Log.Write("Add file. "+file.getAbsolutePath(),3);
            Filelist.append(file.getAbsolutePath()+"\n");
            AddPath = file.getParent().replace(SourcePath, "");
            FileBackuper fileadd = new FileBackuper(filepath,SourcePath,BackupBath,ArchivePath,config);
            fileadd.run();
        }
        else if (file.isDirectory())
        {
            try{
                AddPath = file.getAbsolutePath().replace(SourcePath, "");

                if(!Files.isDirectory(Paths.get(BackupBath+AddPath)) )
                    Files.createDirectories(Paths.get(BackupBath+AddPath));
                if(!Files.isDirectory(Paths.get(ArchivePath+AddPath)) )
                    Files.createDirectories(Paths.get(ArchivePath+AddPath));
                files = file.listFiles();
                for(int i=0; i < files.length; i++)
                {
                    addFile(files[i].getAbsolutePath(),SourcePath,BackupBath,ArchivePath, Filelist);
                }
            }
            catch(Exception ex)
            {
                Log.Write("Cannot create folder. "+ex.toString(),3);
            }
        }
    }
    
}
