/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

/**
 * @author Jordan Jensen
 */
public class WebCrawler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        LinkedList<String> list = new LinkedList();
        list.add("https://www.byui.edu");
        getData(list);
    }
    
    public static void getData(LinkedList<String> list) throws IOException
    {
        int i = 0;
        System.out.println(list.get(0));
        while(!list.isEmpty())
        {            
            URL my_url = new URL(list.get(0));           
            HttpURLConnection huc = (HttpURLConnection) my_url.openConnection();
            int responseCode = huc.getResponseCode();

            if (responseCode < 400) {
            BufferedReader br = new BufferedReader(new InputStreamReader(my_url.openStream()));            
            String strTemp = "";
            while (null != (strTemp = br.readLine())) {
                if (strTemp.contains("<a href=\"htt")) {
                   // System.out.println(strTemp);
                    if(!strTemp.contains("RedirectURL") && !strTemp.contains("http://www.byui.edu/ask/live-chat/") && !strTemp.contains("http://www.byui.edu/students/") && !strTemp.contains("m.byui.edu") && !strTemp.contains("img") && strTemp.contains("http") && strTemp.contains("byui.edu") && !strTemp.contains("href/=\"/"))
                    {
                        strTemp = parse(strTemp);
                        list.add(strTemp);
                        i++;
                    }
                }
            }
            }
            list.removeFirst();   
        }
        System.out.println(i);
    }

    private static String parse(String strTemp) throws MalformedURLException {
        //System.out.println("Original: " + strTemp);
        strTemp = strTemp.substring(strTemp.indexOf("<a href=\"htt") + 9, strTemp.lastIndexOf("\"")); 
        if(strTemp.contains("alt="))
            strTemp = strTemp.substring(0, strTemp.lastIndexOf("alt")-2);
        
        URL temp = new URL(strTemp);
        /*System.out.println("protocol = " + temp.getProtocol());
        System.out.println("authority = " + temp.getAuthority());
        System.out.println("host = " + temp.getHost());
        System.out.println("port = " + temp.getPort());
        System.out.println("path = " + temp.getPath());
        System.out.println("query = " + temp.getQuery());
        System.out.println("filename = " + temp.getFile());
        System.out.println("ref = " + temp.getRef());  */
        //System.out.println(strTemp);
        String protocol = temp.getProtocol();
        strTemp = protocol + "://" + temp.getHost() + temp.getPath();
        System.out.println(strTemp);
        return strTemp;
        
    }
}
