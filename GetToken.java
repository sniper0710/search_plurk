package sniper;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.io.*;

public class GetToken {
  public void Get_perment_token(String user , String passwd) throws IOException{
	    String urlString = null;
	    String oauth_token_secret=null;
	    String oauth_token=null;
	    String verify=null;
	    String line;
	    String filename="properties.properties";
		FileWriter outputFile = new FileWriter(filename,false);
	    CookieManager manager = new CookieManager();
	    CookieHandler.setDefault(manager);
	    
	    //log in
	    urlString = "https://www.plurk.com/Users/login";
	    URL url = new URL(urlString);
	    URLConnection connection = url.openConnection();
	    connection.setDoOutput(true);
	    String para="nick_name="+user+"&password="+passwd+"&login_token=991d93e2bfc24af08b0e610467426212%40moj33q&logintoken=1";
	    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	    writer.write(para);
	    writer.flush();
	    
	    // get cookie and store
	    Object obj = connection.getContent();
//	    CookieStore cookieJar = manager.getCookieStore();
//	    List<HttpCookie> cookies = cookieJar.getCookies();
//	    System.out.println(cookies);
//	    for (HttpCookie cookie : cookies) {
//	      System.out.println(cookie);
//	    }

	    //get temporary token and secret token
	    urlString = "http://www.plurk.com/OAuth/test/get_request_token";
	    url = new URL(urlString);
	    connection = url.openConnection();
	    connection.setRequestProperty("http.agent", "");
	    connection.setDoOutput(true);
	    para="oauth_consumer_key=DdUlxMgudLUD&oauth_consumer_secret=AHCj15q5Rp5PoWFOAHQFZKASpDyUBWXZ";
	    writer = new OutputStreamWriter(connection.getOutputStream());
	    writer.write(para);
	    writer.flush();
	    
	    //get cookie and store
	    obj = connection.getContent();
//	    CookieStore cookieJar = manager.getCookieStore();
//	    List<HttpCookie> cookies = cookieJar.getCookies();
//	    System.out.println(cookies);
//	    for (HttpCookie cookie : cookies) {
//	      System.out.println(cookie);
//	    }
	   
	    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    while ((line = reader.readLine()) != null) {
	        System.out.println(line);
	        System.out.println("---------------");
	        String[] aa= line.split("response");
	        System.out.print("oauth_token = ");
	        oauth_token=aa[1].split("oauth_token=")[1].split("&")[0];
	        System.out.println(oauth_token);
	        
	        System.out.print("oauth_token_secret = ");
	        oauth_token_secret = aa[1].split("oauth_token_secret=")[1].split("&")[0];
	        System.out.println(oauth_token_secret);
	    }
	    
	    //get verify code
	    urlString = "http://www.plurk.com/OAuth/authorizeDone";
	    url = new URL(urlString);
	    connection = url.openConnection();
	    connection.setDoOutput(true);
	    para="oauth_token="+oauth_token+"&accept=1&deviceid=";
	    writer = new OutputStreamWriter(connection.getOutputStream());
	    writer.write(para);
	    writer.flush();
        
	    //get cookie and store
	    obj = connection.getContent();
//	    cookieJar = manager.getCookieStore();
//	    cookies = cookieJar.getCookies();
//	    System.out.println(cookies);
//	    for (HttpCookie cookie : cookies) {
//	      System.out.println(cookie);
//	    }
	    
	    
	    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    while ((line = reader.readLine()) != null) {
	        String[] aa= line.split("Your verification number is: <b>");
	        if (aa.length>=2){
	        verify=aa[1].split("</b><br /> <br />")[0];
	        System.out.println(verify);
	        }
	    }
	    
	    //get permanent token and token_secret
	    urlString = "http://www.plurk.com/OAuth/test/get_access_token";
	    url = new URL(urlString);
	    connection = url.openConnection();
	    connection.setDoOutput(true);
	    para="oauth_consumer_key=DdUlxMgudLUD&oauth_consumer_secret=AHCj15q5Rp5PoWFOAHQFZKASpDyUBWXZ&oauth_token="+oauth_token+"&oauth_token_secret="+oauth_token_secret+"&oauth_verifier="+verify;
	    writer = new OutputStreamWriter(connection.getOutputStream());
	    writer.write(para);
	    writer.flush();
	    
	    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	    while ((line = reader.readLine()) != null) {
	        System.out.println(line);
	        System.out.println("---------------");
	        String[] aa= line.split("response");
	        System.out.print("oauth_token = ");
	        String return_pair=aa[1].split("}")[1];
	        oauth_token=return_pair.split("oauth_token=")[1].split("\"")[0];
	        System.out.println(oauth_token);
	        
	        System.out.print("oauth_token_secret = ");
	        oauth_token_secret = return_pair.split("oauth_token_secret=")[1].split("&")[0];
	        System.out.println(oauth_token_secret);
	    }    
	    
	    writer.close();   
	    
	    // output to file
	    outputFile.write("consumer_key=DdUlxMgudLUD\n");
	    outputFile.write("consumer_secret_key=AHCj15q5Rp5PoWFOAHQFZKASpDyUBWXZ\n");
	    outputFile.write("token_key="+oauth_token+"\n");
	    outputFile.write("token_secret_key="+oauth_token_secret+"\n");
	    outputFile.close();
  }
  public static void main(String args[]) throws Exception {
       GetToken a=new GetToken();
       a.Get_perment_token("user","passwd");
  }
}
