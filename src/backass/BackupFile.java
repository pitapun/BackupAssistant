/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backass;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author Pita
 */
public class BackupFile extends File {
    String ext = "";
    Date d;
    Date d2;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat df2 = new SimpleDateFormat("yyyyMMddHHmm");
        
    BackupFile(String filepath)
    {
        super(filepath);
    }
    
    public String getExt()
    {
        return this.getName().substring(this.getName().lastIndexOf('.')+1).toUpperCase();
    }
    
    public String getLastModifiedStr()
    {
        return df.format(new Date(this.lastModified()));
    }
    
    public String getVersion()
    {
        return df2.format(new Date(this.lastModified()));
    }
    
    public int CompareToVersion(String version)
    {
        return this.getVersion().compareTo(version);
    }
    public int CompareToVersionFrTimestamp(Timestamp timestamp)
    {
        return this.getVersion().compareTo(df2.format(timestamp));
    }
}
