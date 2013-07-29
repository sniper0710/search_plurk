package sniper;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.modules.time.Time;

import com.google.jplurk_oauth.Offset;
import com.google.jplurk_oauth.module.Responses;
import com.google.jplurk_oauth.module.Timeline;
import com.google.jplurk_oauth.skeleton.Args;
import com.google.jplurk_oauth.skeleton.DateTime;
import com.google.jplurk_oauth.skeleton.PlurkOAuth;
import com.google.jplurk_oauth.skeleton.RequestException;

//public class search_multi extends search implements Runnable {
public class search_multi implements Runnable {
	private String no;
	private static int thread_num=20;
	private static int count=0;
	private static Object lock = new Object();
	private static String option;
	private static String item;
	public static LinkedList<String> datepool = new LinkedList<String>();
	boolean done=false;
	protected Args arg_limit=new Args();
	public String title; 
	public String reply=new String();
	public static Map<String,String> convert_month = new LinkedHashMap<String, String>();
	public LinkedList<String> contents;
	public LinkedList<String> result_contents;
	public static Map<String, LinkedList> plurks_map = 
            new LinkedHashMap<String, LinkedList>();
	public Map<String, LinkedList> result_plurks_map = 
            new LinkedHashMap<String, LinkedList>();
	
	public synchronized void set(int newcount){
		count=newcount+count;
	}

	search_multi(){
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
		this.set_pool();
	};

	search_multi(String no){
		this.no=no;
		arg_limit.add("limit","20");
	};
	
	public String convert_time(String get_time){
		String time=null;
		String[] time_format;
		time_format = get_time.split(",");
		time_format = time_format[1].split(" ");
		time=time_format[3]+"-"+convert_month.get(time_format[2])+"-"+time_format[1]+"T"+time_format[4];
		return time;
	}

	
	
	
	public void get_title_content(JSONArray plurks,PlurkOAuth auth , String offset , int month) throws JSONException, RequestException{
		JSONArray responses= new JSONArray();
		String cur_time=null;
		String cur_time_format=null;
		do{
		System.out.println("processing...");
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
            
            synchronized(plurks_map){
            	plurks_map.put(cur_time+"\t"+title, contents);
            }
        }
        cur_time_format=this.convert_time(cur_time);
        arg_limit.add("offset",cur_time_format);
		}while(plurks.length()!=0);
        System.out.println(plurks_map);
	}
	
	public void get_title(JSONArray plurks,PlurkOAuth auth , String offset , int month) throws JSONException, RequestException{

		contents=null;
		String cur_time=null;
		String cur_time_format=null;
		int cur_month=-1;
		
		arg_limit.add("offset",offset);
		do{
		System.out.println("processing...");
		plurks = auth.using(Timeline.class).getPlurks(arg_limit).getJSONArray("plurks");
        for(int i=0; i<plurks.length(); i++){
            title=plurks.getJSONObject(i).getString("content_raw");
            title=title.replace("\n", "\\n");
            cur_time=plurks.getJSONObject(i).getString("posted");
            cur_time_format=this.convert_time(cur_time);
            cur_month=Integer.valueOf(cur_time_format.split("-")[1]);
            if(month==(cur_month%12+1)){
            	synchronized(plurks_map){
            		plurks_map.put(cur_time+"\t"+title, contents);
            	}
            }
        }
        arg_limit.add("offset",cur_time_format);
        System.out.println(month+" "+cur_month + " " +plurks.length()+ " " + this.no +" " + cur_time_format);
		} while ((plurks.length()!=0) && (month==(cur_month%12+1))); 
	}

	
	//search title only
	public Map<String, LinkedList> found_item(Iterator iter,String item){
        int found = -1;
        while (iter.hasNext()) { 
            Map.Entry entry = (Map.Entry) iter.next(); 
            String key = (String) entry.getKey(); 
            LinkedList val = (LinkedList)entry.getValue(); 
    		found = key.toLowerCase().indexOf(item.toLowerCase(),1);
    		if (found!=-1){
    			result_plurks_map.put(key, val);
    		}
        }
        return result_plurks_map;
	}

	//search title and reply
	public Map<String, LinkedList> found_item_all(Iterator iter,String item){	
		boolean found;
        while (iter.hasNext()) { 
        	found=false;
            Map.Entry entry = (Map.Entry) iter.next(); 
            String key = (String) entry.getKey(); 
            LinkedList val = (LinkedList)entry.getValue(); 
            Iterator iter_val=val.iterator();
            if (key.toLowerCase().indexOf(item.toLowerCase(),1)!=-1)
            { 
            	found=true;
            }        
            if(!found){    	
                while (iter_val.hasNext()){
                	String val_element=iter_val.next().toString();
                	if (val_element.toLowerCase().indexOf(item.toLowerCase(),1)!=-1){
                		found=true;
                	}
                }
            }
            if (found){
            	result_plurks_map.put(key, val);
            }
        }
        return result_plurks_map;
	}
	
	
	
	public Map<String, LinkedList> find(String item){
        Iterator iter = plurks_map.entrySet().iterator(); 
        System.out.println(plurks_map.size());
        if (this.option.equals("title")){
        	return(found_item(iter,item));
        }
        else{
        	 return(found_item_all(iter,item));
        }
    }
	
	
	//search title only
	public Map<String, LinkedList> search_result_title(String item,String offset , int month) throws RequestException, JSONException {
		search addcontent=new search();
		contents=new LinkedList<String>();
		
		authentication authObj = new authentication();
		PlurkOAuth auth=authObj.auth();
		JSONArray plurks= new JSONArray();
      	get_title(plurks,auth,offset , month);
//        Iterator iter = plurks_map.entrySet().iterator(); 
//        found_item(iter,item);
        return result_plurks_map;
    	}
	
	//search title and reply 
	public Map<String, LinkedList> search_result_all(String item,String offset , int month) throws RequestException, JSONException {
		search addcontent=new search();
		contents=new LinkedList<String>();
		
		authentication authObj = new authentication();
		PlurkOAuth auth=authObj.auth();
		JSONArray plurks= new JSONArray();
		JSONArray responses= new JSONArray();
		get_title_content(plurks,auth,offset,month);
//        Iterator iter = plurks_map.entrySet().iterator(); 
//        found_item_all(iter,item);
//        System.out.println("\n\n\t\tSearch Complete\n\n");
        return result_plurks_map;
	
	}
        
	
	
	public void set_pool(){
		Calendar cal = Calendar.getInstance();
//		int date = cal.get(cal.DATE);
		int month= cal.get(cal.MONTH)+1;
		int year = cal.get(cal.YEAR);
//		int hour = cal.get(cal.HOUR_OF_DAY);
//		int min = cal.get(cal.MINUTE);
//		int sec = cal.get(cal.SECOND);
//		cal.get(cal.HOUR);
		if(month==12){
			year++;
		}
		datepool.add(Integer.toString(year)+'_'+Integer.toString(month%12+1)+"_1_0_0_0");
		
		for (month=month;month>=1;month--){
			datepool.add(Integer.toString(year)+'_'+Integer.toString(month)+"_1_0_0_0");
		}
		
		for (year=year-1;year>=2010;year--){
			for(int i=12;i>=1;i--){
				datepool.add(Integer.toString(year)+'_'+Integer.toString(i)+"_1_0_0_0");
			}
		}
	}
	
	public String get_time() throws JSONException, RequestException{
		synchronized(datepool){
			if (datepool.isEmpty()){
				this.done=true;
				return null;
			}
			else{
				return datepool.pop();
			}
		}
	}

	public void run() {
		String time="not initialized";
		int year;
		int month;
		int date;
		int hour;
		int min;
		int sec;
		String offset;
		try 
		{
			while(!this.done){
//				System.out.println(this.datepool.element());
				time=this.get_time();
				if (time != null){
					year=Integer.valueOf(time.split("_")[0]);
					month=Integer.valueOf(time.split("_")[1]);
					date=Integer.valueOf(time.split("_")[2]);
					hour=Integer.valueOf(time.split("_")[3]);
					min=Integer.valueOf(time.split("_")[4]);
					sec=Integer.valueOf(time.split("_")[5]);
//					System.out.println(year+" "+month+" " + date + " " + hour + " " + min +" " +sec);
				    DateTime test_dt = DateTime.create(year, month, date, hour, min , sec); 
					offset = test_dt.toTimeOffset();
				    System.out.println(offset);
				    if (this.option.equals("title")){
				    	search_result_title(item, offset , month);
				    }   
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		synchronized(lock){
			thread_num--;
		}
	}
	
	public static void main(String[] args) throws RequestException, IOException, JSONException, ParseException {
    	search_multi obj1 = new search_multi();
    	
    	Thread[] T = new Thread[thread_num];
    	for(int i=0;i<thread_num;i++){	
    		T[i] = new Thread(new search_multi(Integer.toString(i)));
    	}
     	
		String filename="search_result_plurk";

		Iterator iter;
		if (args.length==1){
            filename=args[0];		
		}
		
		System.out.print("result will write to "+filename+"\n");
		Writer outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));			
		Map<String, LinkedList> result=new LinkedHashMap<String, LinkedList>();
		search_multi test=new search_multi();

		Scanner scanner = new Scanner(System.in);
        System.out.print("Please Enter \n  1 for query title \n  2 for query reply \n  G for generate access token \n  cmd : ");
        String choice=scanner.next();
        
        Long start_time=System.currentTimeMillis();
        switch(choice.charAt(0)){
        	case('1'):
        		option="title";
                System.out.print("\nPlease Enter query terms : ");
                item = scanner.next();
                for(int i=0;i<thread_num;i++){	
            		T[i].start();
            	}
                
                while(thread_num !=0){
                	Time.sleep(1);
                }
                
                result=obj1.find(item);
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
            	
            	for(int i=0;i<thread_num;i++){	
               		T[i].start();
               	}
                   
                while(thread_num !=0){
                   	Time.sleep(1);
                }
                   
            	result=obj1.find(item);
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
   
        outputFile.close();
		Long stop_time=System.currentTimeMillis();
		System.out.println(result);
		System.out.println(obj1.plurks_map);
		System.out.println("Cost "+(stop_time-start_time)/1000.0+" SEC");
	}
}