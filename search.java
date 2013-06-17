package sniper;


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

	
	public JSONArray get_all_content(JSONArray plurks,PlurkOAuth auth) throws JSONException, RequestException{
		Args arg_limit=new Args();
		JSONArray responses= new JSONArray();
		String cur_time=null;
		String cur_time_format=null;
		arg_limit.add("minimal_data","1");
		do{
		plurks = auth.using(Timeline.class).getPlurks(arg_limit).getJSONArray("plurks");
        for(int i=0; i<plurks.length(); i++){
        	Long id = (long)(plurks.getJSONObject(i).getInt("plurk_id"));
            title=plurks.getJSONObject(i).getString("content");
            cur_time=plurks.getJSONObject(i).getString("posted");
            JSONObject jsonObjectJackyFromString = auth.using(Responses.class).get(id);
            responses = jsonObjectJackyFromString.getJSONArray("responses");
            contents=new LinkedList<String>();
            for(int i1=0; i1<responses.length(); i1++){
            	reply=responses.getJSONObject(i1).getString("content");
            	contents.addLast(reply);
            }
            plurks_map.put(cur_time+"\t"+title, contents);
        }
        cur_time_format=this.convert_time(cur_time);
        arg_limit.add("offset",cur_time_format);
		}while(plurks.length()!=0);
		return plurks;
	}
		
	public JSONArray get_all_title(JSONArray plurks,PlurkOAuth auth) throws JSONException, RequestException{
		Args arg_limit=new Args();
		contents=null;
		String cur_time=null;
		String cur_time_format=null;
		arg_limit.add("minimal_data","1");
		do{
		plurks = auth.using(Timeline.class).getPlurks(arg_limit).getJSONArray("plurks");
        for(int i=0; i<plurks.length(); i++){
            title=plurks.getJSONObject(i).getString("content");
            cur_time=plurks.getJSONObject(i).getString("posted");
            plurks_map.put(cur_time+"\t"+title, contents);
        }
        cur_time_format=this.convert_time(cur_time);
        arg_limit.add("offset",cur_time_format);
		} while (plurks.length()!=0);
		return plurks;
	}
	
	//search title only
	public Map<String, LinkedList> search_result_title(String item) throws RequestException, JSONException {
		search addcontent=new search();
		contents=new LinkedList<String>();
		
		authentication authObj = new authentication();
		PlurkOAuth auth=authObj.auth();
		JSONArray plurks= new JSONArray();
      	plurks = get_all_title(plurks,auth);
        Iterator iter = plurks_map.entrySet().iterator(); 
        found_item(iter,item);
        System.out.println("\n\n\t\tSearch Complete\n\n");
        System.out.println(plurks_map.size());
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
        plurks = get_all_content(plurks,auth);
        Iterator iter = plurks_map.entrySet().iterator(); 
        found_item_all(iter,item);
        System.out.println("\n\n\t\tSearch Complete\n\n");
        return result_plurks_map;
	}
	
	public void test() throws JSONException, RequestException{
		authentication authObj = new authentication();
		PlurkOAuth auth=authObj.auth();
		JSONArray plurks= new JSONArray();
		plurks=this.get_all_title(plurks,auth);
		System.out.println(plurks_map);
	}
	
	public static void main(String[] args) throws RequestException, IOException, JSONException {
		String filename="search_result_plurk";
		if (args.length==1){
            filename=args[0];		
		}
		
		Args arg_limit=new Args();
		System.out.print("result will write to "+filename+"\n");
		FileWriter outputFile = new FileWriter(filename,false);
		Map<String, LinkedList> result=new LinkedHashMap<String, LinkedList>();
		search test=new search();
		
//		test.test();

		Scanner scanner = new Scanner(System.in);
        System.out.print("Please Enter \n  1 for query title \n  2 for query reply \n  cmd : ");
        String choice=scanner.next();
        Long start_time=System.currentTimeMillis();
        if (choice.equals("1")){
            System.out.print("\nPlease Enter query terms : ");
            String item = scanner.next();
        	result=test.search_result_title(item);
            Iterator iter = result.entrySet().iterator(); 
            while (iter.hasNext()) { 
                Map.Entry entry = (Map.Entry) iter.next(); 
                String key = (String) entry.getKey(); 
                LinkedList val = (LinkedList)entry.getValue(); 
                outputFile.write("Matched plurk : \n\tTitle : "+key+"\n");
            }
        }
        else if (choice.equals("2")){
            System.out.print("\nPlease Enter query terms : ");
            String item = scanner.next();
        	result=test.search_result(item);
            Iterator iter = result.entrySet().iterator(); 
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
        }
        else{
        	System.out.println("ERROR command");
        }     
        outputFile.close();
		Long stop_time=System.currentTimeMillis();
		System.out.println(result);
		System.out.println("Cost "+(stop_time-start_time)/1000.0+" SEC");
	}
}