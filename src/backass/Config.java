/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backass;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pita
 */
public class Config {
    File ConfigFile;
    Map<String, String> Configs = new HashMap<>();
    String ConfigTxt;
    public Config(String filename)
    {
        ConfigFile = new File(filename);
        
        if(!ConfigFile.exists())
        {
            Log.Write("Config file NOT found.", 1);
            try{
                PrintWriter out = new PrintWriter(ConfigFile.getAbsolutePath());
                out.write("SERVER=localhost\n");
                out.write("PORT=3306\n");
                out.write("DATABASE=backupass\n");
                out.write("USERNAME=root\n");
                out.write("PASSWORD=\n");
                out.flush();
                out.close();
            }catch(Exception ex)
            {
                Log.Write("Connot write config file.", 3);
            }
        }
        else
        {
            ReadConfig();
        }
        
    }
    
    public void ReadConfig()
    {
        Configs.clear();
        String[] tmp = new String[2];
        Log.Write("Config file found.", 1);
            try{
                List<String> content = Files.readAllLines(Paths.get(ConfigFile.getAbsolutePath()), Charset.defaultCharset());
                for(String line: content)
                {
                    Log.Write("Config file found."+line.toString(), 1);
                    tmp[0] = line.substring(0,line.indexOf("="));
                    tmp[1] = line.substring(line.indexOf("=")+1);
                    Configs.put(tmp[0].toUpperCase(),tmp[1]);
                }
            }catch(Exception ex)
            {
                Log.Write("Connot write config file.", 3);
            }
    }
    public String GetValue(String key)
    {
        String res= Configs.get(key.toUpperCase());
        return res;
    }
    
    public void Set(String key, String value)
    {
        Configs.put(key.toUpperCase(), value);
        //Log.Write("Set config:"+key+"="+value, 1);
    }
    
    public boolean Save()
    {
        PrintWriter out;
        try{
            out = new PrintWriter("config.txt");
            for(Map.Entry<String, String> entry : Configs.entrySet())
            {
                out.write(entry.getKey()+"="+entry.getValue()+"\n");   
            }
            out.flush();
            out.close();
        }
        catch(Exception ex)
        {
            return false;
        }
        return true;
    }  
}
