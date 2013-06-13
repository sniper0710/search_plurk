package sniper;

import java.util.Calendar;
import com.google.jplurk_oauth.skeleton.DateTime;
import com.google.jplurk_oauth.Offset;

public class Offset_sniper extends Offset{
    
    private DateTime dateTime;
    
    public Offset_sniper() {
        dateTime = DateTime.create(Calendar.getInstance());
    }
    
    public Offset_sniper(long offsetInMs)
    {
        dateTime = DateTime.create(offsetInMs);
    }

    public Offset_sniper(int year, int month, int day, int hour, int minute, int second) {
        dateTime = DateTime.create(year, month, day, hour, minute, second);
        
    }

    public String formatted() {
        return dateTime.toTimeOffset();
    }
    

}
