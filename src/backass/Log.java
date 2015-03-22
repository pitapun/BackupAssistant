/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backass;

/**
 *
 * @author Pita
 */
public class Log {
    
    //Log Level: 1=info; 2=Warning; 3=Error
    public static void Write(String msg, int LogLevel)
    {
       if(LogLevel > 0)
       {
           System.out.println(msg);
       }
    }
}
