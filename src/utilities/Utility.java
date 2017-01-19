package utilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Calendar;

public class Utility{
	
	public static String sha256(String input) throws NoSuchAlgorithmException {
		
		MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
		
        byte[] result = mDigest.digest( input.getBytes());
        
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString();
    }
	
	public static Timestamp getCurrentTimeStamp(){
		// 1) create a java calendar instance
		Calendar calendar = Calendar.getInstance();
		 
		// 2) get a java.util.Date from the calendar instance.
		//		    this date will represent the current instant, or "now".
		java.util.Date now = calendar.getTime();
		 
		// 3) a java current time (now) instance
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
		
		return currentTimestamp;
	}
	
	public static boolean compareGreaterTimeStamp( Timestamp ts1, Timestamp ts2, long minute){
		
		minute *= 60 ;
//		long t1 = ts1.getTime();
//		long t2 = ts1.getTime();
		
//		return ( Math.abs(t1 - t2) > time );
		long subtraction = Math.abs( ts1.getTime() - ts2.getTime()) / 1000; 
		return ( subtraction > minute );
	}
	
}