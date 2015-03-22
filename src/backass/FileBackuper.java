/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backass;
import static backass.Zip.BUFFER;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
/**
 *
 * @author Pita
 */
public class FileBackuper implements Runnable {
    private final String filename;
    private final String SourcePath;
    private final String BackupPath;
    private final String ArchivePath;
    private final String ParentPath;
    private String url;
    private String user;
    private String password;
    
    private BackupFile file;
    
    FileBackuper(String filename,String SourcePath, String BackupPath, String ArchivePath,Config config)
    {
        url = "jdbc:mysql://"+config.GetValue("Server")+":"+config.GetValue("Port")+"/"+config.GetValue("Database")+"?useUnicode=true&characterEncoding=utf-8";
        user = config.GetValue("Username");
        password = config.GetValue("Password");
        this.filename = filename;
        this.SourcePath = SourcePath;
        this.BackupPath = BackupPath;
        this.ArchivePath = ArchivePath;
        Log.Write("Check file: "+filename ,1);
        file = new BackupFile(filename);
        ParentPath = file.getParent().replace(SourcePath, "");
    }
    
    @Override
    public void run()
    {
        String sql = "";
        //Date d;
        //Date d2;
        PreparedStatement ps;
        ResultSet rs;
        if(!file.isFile())
            return;
        //d = new Date(file.lastModified());
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //DateFormat df2 = new SimpleDateFormat("yyyyMMddHHmm");
        //Timestamp timestamp;
        
        try {
            Log.Write(filename+" is file.",1);
            
            Connection con = DriverManager.getConnection(url, user, password);
            //Statement st = con.createStatement();
            
            //sql = "select * from ba_filelist where filepath=?";
            sql = "select a.fileid, a.filename,a.filepath,a.last_modified,a.ext,a.filesize, version " +
            "from ba_filelist as a " +
            "left join ba_file_version as b " +
            "on a.fileid = b.fileid " +
            "where filepath=? and parentpath=? and sourcepath=?";
            
            ps = con.prepareStatement(sql);
            ps.setString(1, file.getName());
            ps.setString(2, ParentPath);
            ps.setString(3, SourcePath);
            rs = ps.executeQuery();
            //File Exist
            if(rs.next()) {
                //System.out.println("Version :"+ rs.getInt("version"));
                //timestamp = rs.getTimestamp("last_modified");
                //d2 = new Date(timestamp.getTime());
                //System.out.println("Compare :"+df2.format(d).compareTo(df2.format(timestamp)));
                //System.out.println("Compare :"+rs.getInt("filesize")+" = " + (file.length()));
                //if(df2.format(d).compareTo(df2.format(timestamp)) != 0 || (rs.getInt("filesize")) != (file.length()))
                if(file.CompareToVersionFrTimestamp(rs.getTimestamp("last_modified")) != 0)
                {
                    Log.Write("File not equal :"+file.getName(),1);
                    //rs.getString("filepath"),rs.getString("filename")
                    //Files.copy(Paths.get(rs.getString("filepath")),Paths.get(ArchivePath).resolve(rs.getString("filename")+"."+df2.format(d)));
                    //Files.copy(Paths.get(rs.getString("filepath")),Paths.get(BackupPath).resolve(rs.getString("filename")),StandardCopyOption.REPLACE_EXISTING);
                    DoBackup(file,rs);

                }
                //System.out.println("File detail: "+ rs.getDate("last_modified")+" size:"+rs.getInt("filesize"));
                Log.Write("File Exist: "+file.getName(),1);
            }
            else
            {
                /*
                sql = "INSERT INTO `ba_filelist`" +
                "(`filename`,`filepath`,`last_modified`,`ext`,`filesize`) VALUES (?,?,?,?,?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, file.getName());
                ps.setString(2, file.getAbsolutePath());
                ps.setString(3, df.format(d));
                ps.setString(4, file.getExt());
                ps.setLong(5, file.length());
                ps.execute();
                System.out.println("Insert file: "+file.getName());
                        */
                DoBackup(file,null);
            }   
            
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        } 
    }
    
    private void ZipFile(String filename,String SourcePath, String TargetPath)
    {
        byte[] buffer = new byte[1024];
    	try{
 
    		FileOutputStream fos = new FileOutputStream(TargetPath);
    		ZipOutputStream zos = new ZipOutputStream(fos);
    		ZipEntry ze= new ZipEntry(filename);
    		zos.putNextEntry(ze);
    		FileInputStream in = new FileInputStream(SourcePath);
 
    		int len;
    		while ((len = in.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
 
    		in.close();
    		zos.closeEntry();

    		//remember close it
    		zos.close();
 
    	}catch(IOException ex){
    	   ex.printStackTrace();
    	}
    }
    
    private boolean DoBackup(BackupFile file, ResultSet result) throws IOException
    {
        try{
            //JOptionPane.showMessageDialog(null, Paths.get(ArchivePath+ParentPath).resolve(file.getName()+"."+file.getVersion()).toString());  
            ZipFile(file.getName(),file.getAbsolutePath(),Paths.get(ArchivePath+ParentPath).resolve(file.getName()+"."+file.getVersion()).toString());
            Files.copy(Paths.get(file.getAbsolutePath()),Paths.get(BackupPath+ParentPath).resolve(file.getName()),StandardCopyOption.REPLACE_EXISTING);
        }catch(Exception ex)
        {
            return false;
        }
        String archivepath = Paths.get(ArchivePath+ParentPath).resolve(file.getName()+"."+file.getVersion()).toString();
        String sql = "";
        Connection con;
        PreparedStatement ps;

        try{
            con = DriverManager.getConnection(url, user, password);
            if(result == null)
            {
                sql = "INSERT INTO `ba_filelist`" +
                "(`filename`,`filepath`,`parentpath`,`sourcepath`,`last_modified`,`ext`,`filesize`) VALUES (?,?,?,?,?,?,?)";
                ps = con.prepareStatement(sql);
                ps.setString(1, file.getName());
                ps.setString(2, file.getName());
                ps.setString(3, ParentPath);
                ps.setString(4, SourcePath);
                ps.setString(5, file.getLastModifiedStr());
                ps.setString(6, file.getExt());
                ps.setLong(7, file.length());
                ps.execute();
                
                sql = "select a.fileid, a.filename,a.filepath,a.last_modified,a.ext,a.filesize, version " +
                "from ba_filelist as a " +
                "left join ba_file_version as b " +
                "on a.fileid = b.fileid " +
                "where filepath=? and parentpath=? and sourcepath=?";
            
                ps = con.prepareStatement(sql);
                ps.setString(1, file.getName());
                ps.setString(2, ParentPath);
                ps.setString(3, SourcePath);
                result = ps.executeQuery();
                if(result.next())
                {
                    sql = "INSERT INTO `ba_file_version`" +
                    "(`fileid`,`version`,`version_date`) VALUES (?,?,?)";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1,result.getInt("fileid"));
                    ps.setInt(2, 1);
                    ps.setString(3, file.getLastModifiedStr());
                    ps.execute();

                    sql = "INSERT INTO `ba_file_archive`" +
                    "(`fileid`,`version`,`path`,`file_createddate`) VALUES (?,?,?,now())";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1,result.getInt("fileid"));
                    ps.setInt(2, 1);
                    ps.setString(3, archivepath);
                    ps.execute();
                }
            }
            else
            {
                sql = "UPDATE `ba_filelist` SET " +
                "`last_modified` = ?, `filesize`=? WHERE fileid=?";
                ps = con.prepareStatement(sql);
                ps.setString(1, file.getLastModifiedStr());
                ps.setLong(2, file.length());
                ps.setInt(3, result.getInt("fileid"));
                ps.execute();
                
                sql = "update `ba_file_version` SET " +
                "`version`=?,`version_date`=? where fileid=?";
                ps = con.prepareStatement(sql);
                
                ps.setInt(1, result.getInt("version")+1);
                ps.setString(2, file.getLastModifiedStr());
                ps.setInt(3,result.getInt("fileid"));
                ps.execute();
                
                sql = "INSERT INTO `ba_file_archive`" +
                    "(`fileid`,`version`,`path`,`file_createddate`) VALUES (?,?,?,now())";
                ps = con.prepareStatement(sql);
                ps.setInt(1,result.getInt("fileid"));
                ps.setInt(2,result.getInt("version")+1);
                ps.setString(3, archivepath);
                ps.execute();
            }           
        }catch (SQLException ex)
        {
            System.out.println("Error in insert record: "+ex.getMessage());
        }

        System.out.println("Back up processed file : "+file.getAbsolutePath());
        return true;
    }

}
