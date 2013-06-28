package sniper;


import org.python.util.PythonInterpreter;
import org.python.core.PySystemState;
import org.python.core.PyString;
//import joptsimple.OptionParser;
//import joptsimple.OptionSet;

//import com.google.jplurk_oauth.Qualifier;
import com.google.jplurk_oauth.module.*;
import com.google.jplurk_oauth.skeleton.PlurkOAuth;
import com.google.jplurk_oauth.skeleton.RequestException;
import org.json.*;
//import java.util.Properties;
import java.io.*;
//import com.google.jplurk_oauth.Offset;
//import org.scribe.model.Response;
import sniper.authentication;
import sniper.GetToken;
import com.google.jplurk_oauth.skeleton.Args;
import sniper.Offset_sniper;
import java.lang.String;
import java.util.*;
import java.text.SimpleDateFormat;


public class search{
	public Map<String,String> convert_month = new LinkedHashMap<String, String>();
	public LinkedList<String> contents;
	public Map<String, LinkedList> plurks_map = 
            new LinkedHashMap<String, LinkedList>();
	public String title; 
	public String reply=new String();
	public LinkedList<String> result_contents;
	public Map<String, LinkedList> result_plurks_map = 
            new LinkedHashMap<String, LinkedList>();
	private String cur_date_str;
	private Args arg_limit=new Args();
	private boolean save_to_file=false;
	
	public search(){
		convert_month.put("Jan", "1");
		convert_month.put("Feb", "2");
		convert_month.put("Mar", "3");
		convert_month.put("Apr", "4");
		convert_month.put("May", "5");
		convert_month.put("Jun", "6");
		convert_month.put("Jul", "7");
		convert_month.put("Aug", "8");
		convert_month.put("Sep", "9");
		convert_month.put("Oct", "10");
		convert_month.put("Nov", "11");
		convert_month.put("Dec", "12");
		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
		sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateString = sdf1.format(date);
		String timeString = sdf2.format(date);
		cur_date_str=dateString+"T"+timeString;
		
//		arg_limit.add("minimal_data","1");
		arg_limit.add("limit","10");
 
	}
	//search title only
	public void found_item(Iterator iter,String item){
        int found = -1;
        while (iter.hasNext()) { 
            Map.Entry entry = (Map.Entry) iter.next(); 
            String key = (String) entry.getKey(); 
            LinkedList val = (LinkedList)entry.getValue(); 
    		found = key.toLowerCase().indexOf(item,1);
    		if (found!=-1){
    			result_plurks_map.put(key, val);
    		}
        }
	}

	//search title and reply
	public void found_item_all(Iterator iter,String item){	
		boolean found;
        while (iter.hasNext()) { 
        	found=false;
            Map.Entry entry = (Map.Entry) iter.next(); 
            String key = (String) entry.getKey(); 
            LinkedList val = (LinkedList)entry.getValue(); 
            Iterator iter_val=val.iterator();
            if (key.toLowerCase().indexOf(item,1)!=-1)
            { 
            	found=true;
            }        
            if(!found){    	
                while (iter_val.hasNext()){
                	String val_element=iter_val.next().toString();
                	if (val_element.toLowerCase().indexOf(item,1)!=-1){
                		found=true;
                	}
                }
            }
            if (found){
            	result_plurks_map.put(key, val);
            }
        }
	}
	
	//convert time format of plurk response to offset format
	public String convert_time(String get_time){
		int i=1;
		String time=null;
		String[] time_format;
		time_format = get_time.split(",");
		time_format = time_format[1].split(" ");
		time=time_format[3]+"-"+convert_month.get(time_format[2])+"-"+time_format[1]+"T"+time_format[4];
		return time;
	}

	
	public void get_all_content(JSONArray plurks,PlurkOAuth auth) throws JSONException, RequestException{
		JSONArray responses= new JSONArray();
		String cur_time=null;
		String cur_time_format=null;
		do{
		plurks = auth.using(Timeline.class).getPlurks(arg_limit).getJSONArray("plurks");
        for(int i=0; i<plurks.length(); i++){
        	Long id = (long)(plurks.getJSONObject(i).getInt("plurk_id"));
            title=plurks.getJSONObject(i).getString("content_raw");
            title=title.replace("\n", "\\n");
            cur_time=plurks.getJSONObject(i).getString("posted");
            JSONObject jsonObjectJackyFromString = auth.using(Responses.class).get(id);
            responses = jsonObjectJackyFromString.getJSONArray("responses");
            contents=new LinkedList<String>();
            for(int i1=0; i1<responses.length(); i1++){
            	reply=responses.getJSONObject(i1).getString("content_raw");
            	reply=reply.replace("\n", "\\n");
            	contents.addLast(reply);
            }
            plurks_map.put(cur_time+"\t"+title, contents);
        }
        cur_time_format=this.convert_time(cur_time);
        arg_limit.add("offset",cur_time_format);
		}while(plurks.length()!=0);
        System.out.println(plurks_map);
	}
	
	public void get_all_title(JSONArray plurks,PlurkOAuth auth) throws JSONException, RequestException{
		contents=null;
		String cur_time=null;
		String cur_time_format=null;
		do{
		plurks = auth.using(Timeline.class).getPlurks(arg_limit).getJSONArray("plurks");
        for(int i=0; i<plurks.length(); i++){
            title=plurks.getJSONObject(i).getString("content_raw");
            title=title.replace("\n", "\\n");
            cur_time=plurks.getJSONObject(i).getString("posted");
            plurks_map.put(cur_time+"\t"+title, contents);
        }
        cur_time_format=this.convert_time(cur_time);
        arg_limit.add("offset",cur_time_format);
		} while (plurks.length()!=0);
	}
	
	//search title only
	public Map<String, LinkedList> search_result_title(String item) throws RequestException, JSONException {
		search addcontent=new search();
		contents=new LinkedList<String>();
		
		authentication authObj = new authentication();
		PlurkOAuth auth=authObj.auth();
		JSONArray plurks= new JSONArray();
      	get_all_title(plurks,auth);
        Iterator iter = plurks_map.entrySet().iterator(); 
        found_item(iter,item);
        System.out.println("\n\n\t\tSearch Complete\n\n");
//        System.out.println(plurks_map.size());
        System.out.println(plurks_map);
        return result_plurks_map;
    	}
	
	//search title and reply 
	public Map<String, LinkedList> search_result(String item) throws RequestException, JSONException {
		search addcontent=new search();
		contents=new LinkedList<String>();
		
		authentication authObj = new authentication();
		PlurkOAuth auth=authObj.auth();
		JSONArray plurks= new JSONArray();
		JSONArray responses= new JSONArray();
        get_all_content(plurks,auth);
        Iterator iter = plurks_map.entrySet().iterator(); 
        found_item_all(iter,item);
        System.out.println("\n\n\t\tSearch Complete\n\n");
        return result_plurks_map;
	}
		
	public void save_all_data() throws JSONException, RequestException, IOException{
		Writer outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plurk_data"), "UTF-8"));			
		authentication authObj = new authentication();
		PlurkOAuth auth=authObj.auth();
		JSONArray plurks= new JSONArray();
        get_all_content(plurks,auth);
        Iterator iter = plurks_map.entrySet().iterator(); 
        while (iter.hasNext()) { 
            Map.Entry entry = (Map.Entry) iter.next(); 
            String key = (String) entry.getKey(); 
            LinkedList val = (LinkedList)entry.getValue(); 
            Iterator iter_val=val.iterator(); 
            outputFile.write("T:"+key+"\n");
            while (iter_val.hasNext()){
            	String val_element=iter_val.next().toString();
                outputFile.write("R:"+val_element+"\n");
           	}
        }
        outputFile.close();
        System.out.println(plurks_map);
        System.out.println("\n\n\t\tStore Complete\n\n");
	}
	
	public void get_all_data() throws JSONException, RequestException, IOException{
		BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream("plurk_data"),"UTF-8"));			
        String text=null;
        String key=null;
        String cur_time=null;
		while((text=inputFile.readLine())!=null){
            System.out.println(text);
            if(text.charAt(0)=='T'){
            	key=text.split("\t")[1];
            	cur_time=text.substring(2).split("\t")[0];
            	contents=new LinkedList<String>();
            }
            else{
              	reply=text.substring(2);
               	contents.addLast(reply);
                plurks_map.put(cur_time+"\t"+key, contents);
            }
        }
         inputFile.close();
	}

	public void test() throws JSONException, IOException, RequestException{
	}
	
	public Map<String, LinkedList> offline_search(String item){
        Iterator iter = plurks_map.entrySet().iterator();
		found_item_all(iter,item);
		return result_plurks_map;
	}
	
	public static void main(String[] args) throws RequestException, IOException, JSONException {
		String filename="search_result_plurk";
		String item=null;
		Iterator iter;
		if (args.length==1){
            filename=args[0];		
		}
		
		System.out.print("result will write to "+filename+"\n");
//		FileWriter outputFile = new FileWriter(filename,false);
		Writer outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));			
		Map<String, LinkedList> result=new LinkedHashMap<String, LinkedList>();
		search test=new search();

//		test.test();

		Scanner scanner = new Scanner(System.in);
        System.out.print("Please Enter \n  1 for query title \n  2 for query reply \n  3 for save data for offline search \n  4 for offline search \n  G for generate access token \n  cmd : ");
        String choice=scanner.next();
        Long start_time=System.currentTimeMillis();
        switch(choice.charAt(0)){
        	case('1'):
                System.out.print("\nPlease Enter query terms : ");
                item = scanner.next();
            	result=test.search_result_title(item);
                iter = result.entrySet().iterator(); 
                while (iter.hasNext()) { 
                    Map.Entry entry = (Map.Entry) iter.next(); 
                    String key = (String) entry.getKey(); 
                    LinkedList val = (LinkedList)entry.getValue(); 
                    outputFile.write("Matched plurk : \n\tTitle : "+key+"\n");
                }
        		break;
        	case('2'):
                System.out.print("\nPlease Enter query terms : ");
            	item = scanner.next();
            	result=test.search_result(item);
            	iter = result.entrySet().iterator(); 
            	while (iter.hasNext()) { 
            		Map.Entry entry = (Map.Entry) iter.next(); 
            		String key = (String) entry.getKey(); 
            		LinkedList val = (LinkedList)entry.getValue(); 
            		outputFile.write("Matched plurk : \n\tTitle : "+key+"\n");	
            		while (!val.isEmpty()){
            			String reply=(String)val.pollFirst();
            			outputFile.write("\tReply : "+reply+"\n");
            		}
            	}
        		break;
        	case('3'):
              	System.out.print("\n\tData is saved ");
        		test.save_all_data();
        		break;
        	case('4'):
                System.out.print("\nPlease Enter query terms : ");
        		item = scanner.next();
        		test.get_all_data();
        		result=test.offline_search(item);
        		iter = result.entrySet().iterator(); 
        		while (iter.hasNext()) { 
        			Map.Entry entry = (Map.Entry) iter.next(); 
        			String key = (String) entry.getKey(); 
        			LinkedList val = (LinkedList)entry.getValue(); 
        			outputFile.write("Matched plurk : \n\tTitle : "+key+"\n");
        			while (!val.isEmpty()){
        				String reply=(String)val.pollFirst();
        				outputFile.write("\tReply : "+reply+"\n");
        			}
        		}
        		break;
        	case('g'):
        	case('G'):
             	GetToken GetTokenObj=new GetToken();
        		System.out.print("\nPlease Enter your plurl id : ");
        		String user = scanner.next();
        		System.out.print("\nPlease Enter your password : ");
        		String passwd= scanner.next();
        		GetTokenObj.Get_perment_token(user, passwd);
        		break;
        	default:
        		System.out.println("ERROR command");
        }
        
//        if (choice.equals("1")){
//            System.out.print("\nPlease Enter query terms : ");
//            String item = scanner.next();
//        	result=test.search_result_title(item);
//            Iterator iter = result.entrySet().iterator(); 
//            while (iter.hasNext()) { 
//                Map.Entry entry = (Map.Entry) iter.next(); 
//                String key = (String) entry.getKey(); 
//                LinkedList val = (LinkedList)entry.getValue(); 
//                outputFile.write("Matched plurk : \n\tTitle : "+key+"\n");
//            }
//        }
//        else if (choice.equals("2")){
//            System.out.print("\nPlease Enter query terms : ");
//            String item = scanner.next();
//        	result=test.search_result(item);
//            Iterator iter = result.entrySet().iterator(); 
//            while (iter.hasNext()) { 
//                Map.Entry entry = (Map.Entry) iter.next(); 
//                String key = (String) entry.getKey(); 
//                LinkedList val = (LinkedList)entry.getValue(); 
//                outputFile.write("Matched plurk : \n\tTitle : "+key+"\n");
//                while (!val.isEmpty()){
//                	String reply=(String)val.pollFirst();
//                	  outputFile.write("\tReply : "+reply+"\n");
//                }
//            }
//        }
//        else if (choice.equals("3")){
//        	System.out.print("\n\tData is saved ");
//        	test.save_all_data();
//        }
//        else if (choice.equals("4")){
//            System.out.print("\nPlease Enter query terms : ");
//            String item = scanner.next();
//        	test.get_all_data();
//        	result=test.offline_search(item);
//            Iterator iter = result.entrySet().iterator(); 
//            while (iter.hasNext()) { 
//                Map.Entry entry = (Map.Entry) iter.next(); 
//                String key = (String) entry.getKey(); 
//                LinkedList val = (LinkedList)entry.getValue(); 
//                outputFile.write("Matched plurk : \n\tTitle : "+key+"\n");
//                while (!val.isEmpty()){
//                	String reply=(String)val.pollFirst();
//                	  outputFile.write("\tReply : "+reply+"\n");
//                }
//            }
//        }
//        else if (choice.equals("G")){
//        	GetToken GetTokenObj=new GetToken();
//        	System.out.print("\nPlease Enter your plurl id : ");
//            String user = scanner.next();
//            System.out.print("\nPlease Enter your password : ");
//            String passwd= scanner.next();
//        	GetTokenObj.Get_perment_token(user, passwd);
//        }
//        
//        else{
//        	System.out.println("ERROR command");
//        }     
        outputFile.close();
		Long stop_time=System.currentTimeMillis();
		System.out.println(result);
		System.out.println("Cost "+(stop_time-start_time)/1000.0+" SEC");
	}
}