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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jordan Jensen
 */
public class WebCrawler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        ArrayList<String> list = new ArrayList();
        list.add("http://allrecipes.com/cooks/top-reviewer-cooks.aspx");
        getData(list);
    }
    
    public static void getData(ArrayList<String> list) throws IOException
    {
        int i = 0;
        int page = 2;
        
        while(!list.isEmpty() && page < 6)
        {            
            URL my_url = new URL(list.get(0));   
            System.out.println(list.get(0));
            HttpURLConnection huc = (HttpURLConnection) my_url.openConnection();
            int responseCode = huc.getResponseCode();
            
            if (responseCode < 400) {
            BufferedReader br = new BufferedReader(new InputStreamReader(my_url.openStream()));            
            String strTemp = "";
            while (null != (strTemp = br.readLine())) {
                if (strTemp.contains("reviews.aspx\">Reviews</a>")) {
                    
                    strTemp = parse(strTemp);
                    list.add(strTemp);
                    i++;                   
                }

                if (strTemp.contains("top-reviewer-cooks.aspx?Page=") && strTemp.contains("<a href"))
                {
                    list.set(0, "http://allrecipes.com/cooks/top-reviewer-cooks.aspx?Page=" + page++);
                    break;
                }
            }
            }   
        }
        list.remove(0);
        getRecipes(list);
        System.out.println(i);
    }

    private static String parse(String strTemp) throws MalformedURLException {
        strTemp = strTemp.substring(strTemp.indexOf("href=\"")+ 6, strTemp.lastIndexOf("\">"));         
        URL temp = new URL(strTemp);
        String protocol = temp.getProtocol();
        strTemp = protocol + "://" + temp.getHost() + temp.getPath();
        System.out.println(strTemp);
        return strTemp;
        
    }
    
    static void getRecipes(ArrayList<String> list) throws MalformedURLException, IOException
    {
        int page = 2;
        int count = 0;
        int dataCount = 0;
        String userName = "";
        String userRating = "";
        String recipeURL = "";
        String recipeName = "";
                
        while(!list.isEmpty())
        {
            if(count == 0){
                count++;
                userName = list.get(0).substring(list.get(0).indexOf("cook/") + 5, list.get(0).indexOf("/re"));
            }
            URL my_url = new URL(list.get(0));           
            HttpURLConnection huc = (HttpURLConnection) my_url.openConnection();
            int responseCode = huc.getResponseCode();

            if (responseCode < 400) {
                BufferedReader br = new BufferedReader(new InputStreamReader(my_url.openStream()));
                String strTemp = "";
                while (null != (strTemp = br.readLine())) {
                    if(strTemp.contains("<meta itemprop=\"ratingValue\"")){

                        userRating = strTemp.substring(strTemp.indexOf("content=\"") + 9, strTemp.indexOf("content=\"") + 10);
                        dataCount++;
                    }
                    if (strTemp.contains("<a id=\"ctl00_CenterColumnPlaceHolder_ReviewList_rptReviewList_ReviewItem") 
                            && !strTemp.contains("WasHelpful.ashx?")) {
                        
                        recipeURL = strTemp.substring(strTemp.indexOf("href=") + 6,
                                strTemp.indexOf("aspx\">") + 4);
                        recipeName = strTemp.substring(strTemp.indexOf("detail.aspx\">") + 13,
                                strTemp.indexOf("</a>"));
                        dataCount +=2;
                        
                    }
                    if(dataCount == 3){
                        dataCount = 0;
                        try{
                        sendToDB(userName, recipeName, recipeURL, userRating);
                        }
                        catch(SQLException e){
                            e.printStackTrace();
                        }
                    }
                    if (strTemp.contains("?Page=") && strTemp.contains("<a href"))
                    {
                        String toAdd = list.get(0);
                        if(page > 2){
                            toAdd = toAdd.replace("?Page=" + (page - 1), "?Page=" + page);
                        }
                        else{
                            toAdd += "?Page=" + page;
                        }
                        page++;
                        list.set(0,toAdd);
                        break;
                    }
                }  
            }
            else{
                page = 2;
                list.remove(0);
                userName = list.get(0).substring(list.get(0).indexOf("cook/") + 5, list.get(0).indexOf("/re"));
            }
            
        }
    }

    private static void sendToDB(String userName, String recipeName, String recipeURL, String userRating) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        Connection con = DriverManager.getConnection(
                         "jdbc:mysql://dev.maurasoftware.com:3306/skillet",
                         "skilletAdmin",
                         "Twitchelltwit");

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("insert into user_table value (" + userName + "); select LAST_INSERT_ID() from user_table");
        String uId = rs.getString("LAST_INSERT_ID()");
        rs = stmt.executeQuery("insert into item_table value (" + recipeName + "); select LAST_INSERT_ID() from item_table");
        String rId = rs.getString("LAST_INSERT_ID()");
        rs = stmt.executeQuery("insert into rating_table value (" + uId + "," + rId + "," + userRating + ")");
    }
}
