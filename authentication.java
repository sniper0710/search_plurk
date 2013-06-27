package sniper;

import java.util.Properties;
import java.io.*;

import com.google.jplurk_oauth.skeleton.PlurkOAuth;
public class authentication{
	public PlurkOAuth auth(){
        Properties prop = System.getProperties();
        try {
//        	String workingdirectory=System.getProperty("user.dir");
            prop.load(new FileInputStream("properties.properties"));
//        	prop.load(new FileInputStream(prop.getProperty("user.home")+"/properties.properties"));
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        /* create oauth config */
        PlurkOAuth auth = new PlurkOAuth(
                prop.getProperty("consumer_key"), prop.getProperty("consumer_secret_key"),
                prop.getProperty("token_key"), prop.getProperty("token_secret_key"));
        System.out.println(prop.getProperty("consumer_key"));
        System.out.println(prop.getProperty("consumer_secret_key"));
        System.out.println( prop.getProperty("token_key"));
        System.out.println(prop.getProperty("token_secret_key"));
        return auth;
	}
}