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
    public static Statement stmt;
    public static Connection con;
    public static int uId;
    public static int rId;
    public static void main(String[] args) throws IOException {
        LinkedList<String> list = new LinkedList();
        list.add("http://allrecipes.com/cooks/top-reviewer-cooks.aspx");
        try {        
            init();
        } catch (SQLException ex) {
            Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        getData(list);
    
    }
    
    public static void getData(LinkedList<String> list) throws IOException
    {
        int i = 0;
        int page = 2;
        
        while(!list.isEmpty() && page < 6)
        {            
            URL my_url = new URL(list.get(0));   
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
    }

    private static String parse(String strTemp) throws MalformedURLException {
        strTemp = strTemp.substring(strTemp.indexOf("href=\"")+ 6, strTemp.lastIndexOf("\">"));         
        URL temp = new URL(strTemp);
        String protocol = temp.getProtocol();
        strTemp = protocol + "://" + temp.getHost() + temp.getPath();
        return strTemp;
        
    }
    
    static void getRecipes(LinkedList<String> list) throws MalformedURLException, IOException
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
                try {
                    insertUser(userName);
                    System.out.println(userName);
                } catch (SQLException ex) {
                    Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                                strTemp.indexOf("</a>")).replace("\'", "\'\'");
                        dataCount +=2;
                        
                    }
                    if(dataCount == 3){
                        dataCount = 0;
                        try{
                        sendToDB(recipeName, recipeURL, userRating);
                        }
                        catch(SQLException e){
                            e.printStackTrace();
                        }                        
                        //System.out.println(recipeName + " " + recipeURL + " " + userRating);
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
                    if(strTemp.contains("Uh-oh, looks like no one has reviewed this recipe yet.")){
                        page = 2;
                        list.remove(0);
                        userName = list.get(0).substring(list.get(0).indexOf("cook/") + 5, list.get(0).indexOf("/re"));
                        try {
                            insertUser(userName);
                        } catch (SQLException ex) {
                            Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        uId++;
                    }
                }  
            }
            else{
                page = 2;
                list.remove(0);
                userName = list.get(0).substring(list.get(0).indexOf("cook/") + 5, list.get(0).indexOf("/re"));
                try {
                    insertUser(userName);
                } catch (SQLException ex) {
                    Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, ex);
                }
                uId++;
            }
            
        }
    }

    private static void sendToDB(String recipeName, String recipeURL, String userRating) throws SQLException {
        int tempId = rId;
        int temp = 0;
        ResultSet rs = stmt.executeQuery("select id from item_table where title=\"" + recipeName + "\" and url=\"" + recipeURL + "\"");
        if(rs.next()){
            rId = rs.getInt("id");
            temp = stmt.executeUpdate("insert into rating_table (user_id, item_id, rating_value) values (\'" + uId + "\',\'" + rId + "\',\'" + userRating + "\')");
            rId = tempId;
        }
        else{
            temp = stmt.executeUpdate("insert into item_table (title, url) values (\'" + recipeName + "\',\'" + recipeURL + "\')"); 
            temp = stmt.executeUpdate("insert into rating_table (user_id, item_id, rating_value) values (\'" + uId + "\',\'" + rId + "\',\'" + userRating + "\')");
            rId++;
        }
    }
    private static void insertUser(String userName) throws SQLException {   
        int temp = stmt.executeUpdate("insert into user_table (name) values (\'" + userName + "\')");
    }

    private static void init() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(WebCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        con = DriverManager.getConnection(
                         "jdbc:mysql://dev.maurasoftware.com:3306/skillet",
                         "skilletAdmin",
                         "Twitchelltwit");
        stmt = con.createStatement();
        rId = 1;
        uId = 1;
    }
}
