/**
 * ZZDateParser.java
 *  Created: Wed Feb  9 08:20:13 2000
 *
 * 
 *    Copyright (c) 1999, Ted Nelson, Tuomas Lukka and Vesa Parkkinen
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of 
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 * 
 * 
 * @author Vesa Parkkinen
 * @version 0.2 ( might even work, sometimes )
 *
 */

package org.gzigzag.module;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.SimpleTimeZone;

/**
 * This class tries to parse RFC 822 formatted dates.
 * I haven't yet seen the spec, but this seems to work with 
 * few strings taken from my mails.
 * OK, now that i've seen the spec, this should pretty much be capable
 * of parsing the dates. 
 * Except the zone name information is not used.
 * <p>
 */
public class ZZDateParser {
public static final String rcsid = "$Id: ZZDateParser.java,v 1.9 2000/09/19 10:32:03 ajk Exp $";
    public static final boolean dbg = true;
    static final void p(String s) { if(dbg) System.out.println(s); }
    static final void pa(String s) { System.out.println(s); }
    
    /**
     * Month names used in date string.
     */
    static String[] months = { "Jan",
			       "Feb",
			       "Mar",
			       "Apr",
			       "May",
			       "Jun",
			       "Jul",
			       "Aug",
			       "Sep",
			       "Oct",
			       "Nov",
			       "Dec" };

    static String[] days   = { "Mon",
			       "Tue",
			       "Wed",
			       "Thu",
			       "Fri",
			       "Sat",
			       "Sun" };
    
    /**
     * Method for parsing RFC 822 formatted dates to calendar objects.
     * @param String d  Date string in RFC 822 format.
     * @return Calendar calendar having the date and time from RFC 822 
     * ERROR CHECKING IS MISSING !
     * Will be added someday soon.
     */
    public static Calendar parse(String d){
	
	int year     = 1;
	int month    = 1;
	int date     = 1;
	
	String day;
	
	int    time[];
	
	int    gmt_diff[];
	
	d.trim();
	try{
	
	/* check whether we have day name */
	StringTokenizer t = new StringTokenizer( d, " " );
	
	//if ( t.countTokens() > 1 ){ 
	//day = t.nextToken();
	//    t.nextToken( " " );
	//}
	//String tok;
	try { 
	    day = t.nextToken( );
	    date = (int) Integer.parseInt( day );
	} catch( NumberFormatException nfe ) {
	    //day = t.nextToken();
	    //t.nextToken( );
	    date = (int) Integer.parseInt( t.nextToken( ) );
	}
	//try{ 
	String smonth = t.nextToken();
	
	for ( int i = 0; i < 12 ; i++ )
	    if( smonth.equals( months[ i ] ) ){
		month = i ;
		break;
	    }
	
	year = (int) Integer.parseInt( t.nextToken() );
	
	time = parseTime( t.nextToken() );
	
	String[] id;
	
	String s;
	

	int off = 0;

 	if(  t.hasMoreTokens() )
	    gmt_diff = parseTz(t.nextToken());
	
	//if( ! t.hasMoreTokens() ){
	if(  t.hasMoreTokens() ){
	    gmt_diff = parseTz(t.nextToken());
	    
	    off =  gmt_diff[0] * 60 * 60 * 1000 + gmt_diff[1] * 60 * 1000;
	    id = TimeZone.getAvailableIDs( off );
	    if ( id.length > 0 ) 
		s = id[0];
	    else s = "";
	} else { 
	    
	    s = "GMT";//t.nextToken();
	    
	}
	
	Calendar 
	    calendar = new GregorianCalendar( 
				 new SimpleTimeZone( off, s ) );
	
	calendar.set( year, month, date, 
		      time[0], time[1], time[2] );
	
	return calendar;}
	catch (Exception e){
	    pa("Not a rfc822 date! Fix it :" + e);
	    return new GregorianCalendar();
	}
    }
    
    /**
     * What we've got here is a  method to parse 
     * Calendar to RFC 822 formatted date String.
     *
     * @param Calendar calendar having the date and time from RFC 822 
     * @return String d Date string in RFC 822 format.
     *
     */
    public static String parse(Calendar c){
	String dateString = "";
	
	/* parse day string */
	int d = c.get(Calendar.DAY_OF_WEEK);
	
	String day = "";
	
	switch ( d ){
	case (Calendar.MONDAY):
	    day = days[0];
	    break;

	case (Calendar.TUESDAY):
	    day = days[1];
	    break;

	case (Calendar.WEDNESDAY):
	    day = days[2];
	    break;
	    
	case (Calendar.THURSDAY):
	    day = days[3];;
	    break;
	   
	case (Calendar.FRIDAY):
	    day = days[4];;
	    break;
	   
	case (Calendar.SATURDAY):
	    day = days[5];;
	    break;

	case (Calendar.SUNDAY):
	    day = days[6];;
	    break;

	default:
	    day = "";
	    break;
	}
	//*/
	//day = days[d];
	if ( day.length() == 3 )
	    dateString += day +", "; 
	
	int dm = c.get(Calendar.DAY_OF_MONTH);
	
	if( dm < 1 || dm > 31 ) dm = 1;
	
	dateString += "" + dm + " " ;
	
	int m = c.get(Calendar.MONTH);
	
	String month = "";
	/*
	switch ( m ){
	case (Calendar.JANUARY):
	    month = months[0];
	    break;
	case (Calendar.FEBRUARY):
	    month = months[1];
	    break;
	case (Calendar.MARCH):
	    month = months[2];
	    break;
	case (Calendar.APRIL):
	    month = months[3];
	    break;
	case (Calendar.MAY):
	    month = months[4];
	    break;
	case (Calendar.JUNE):
	    month = months[5];
	    break;
	case (Calendar.JULY):
	    month = months[6];
	    break;
	case (Calendar.AUGUST):
	    month = months[7];
	    break;
	case (Calendar.SEPTEMBER):
	    month = months[8];
	    break;
	case (Calendar.OCTOBER):
	    month = months[9];
	    break;
	case (Calendar.NOVEMBER):
	    month = months[10];
	    break;
	case (Calendar.DECEMBER):
	    month = months[11];
	    break;
	default:
	    month = months[0];
	}
	*/

	month = months[m];
	
	dateString += month + " ";
	
	int hour = c.get(Calendar.HOUR_OF_DAY);
	
	String hh = ""+ hour;

	hh = addZeros(hh,2);
	
	int min = c.get(Calendar.MINUTE);
	
	String mm = ""+ min;

	mm = addZeros(mm,2);
	
	int sec = c.get(Calendar.SECOND);
	
	String ss = ""+ sec;
	
	ss = addZeros(ss,2);
	
	dateString += hh +":" + mm + ":" + ss + " " ;
	
	// tz
	
	int tz = c.get(Calendar.ZONE_OFFSET);
	
	int tz_h = tz / ( 60 * 60 * 1000 );
	int tz_m = ( tz % ( 60 * 60 * 1000 ) ) / 60 * 1000;
	String t = addZeros("" + Math.abs(tz_h), 2);
	t += addZeros("" + tz_m, 2);
	if( tz_h  < 0 )
	    dateString += "-";
	if( tz_h == 0 && tz_m == 0 ) 
	    dateString += "GMT";
	else dateString += ""+ t;
	
	return dateString;
	
    }

    private static String addZeros(String s,int count){ 
	int l =  s.length();
	if ( l < count ) 
	    for ( int i = l; i < count;i++){
		s = "0" + s;}
	return s;
    }
    
    
    private static int[] parseTime( String time ){
	
	int[] comp = { 0, 0, 0 };
	
	StringTokenizer tt = new StringTokenizer(time,":");
	
	if( tt.hasMoreTokens() )
	   comp[0] = (int)Integer.parseInt(tt.nextToken());
	else return comp;
	
	if( tt.hasMoreTokens() )
	   comp[1] = (int)Integer.parseInt(tt.nextToken());
	else return comp;
	
	if( tt.hasMoreTokens() )
	   comp[2] = (int)Integer.parseInt(tt.nextToken());
	
	return comp;
    }
    
    /* XXX TODO: add parsing of zone names if needed. */
    private static int[] parseTz( String tz ){
	
	int[] comp = { 0, 0 };
	
	int negative = 1;
	
	tz.trim();
	
	if( tz.startsWith( "-" ) ){ 
	    negative = -1;
	}
	
	int l = tz.length();
	
	String  s;
	
	StringTokenizer tt = new StringTokenizer( tz.substring( 1, l ) );
	
	if ( tt.hasMoreTokens() )
	  s = tt.nextToken();
	else s = tz.substring( 1, l );
	
	int aa;
	
	try {
	    aa = (int) Integer.parseInt( s );
	}
	catch( Exception e ) {
	    return comp;
	}
	
	comp[0] = negative * (int) aa / 100;
	// do we need this 
	comp[1] =/* negative * */ (int) aa % 100;

	return comp;

    }

    /*
    public static void main(String[] argv){
	//Calendar c = ZZDateParser.parse("Tue, 15 Feb 2000 23:57:42 GMT");
	Calendar c = ZZDateParser.parse("Mon, 14 Feb 2000 18:04 -0200");
	//Calendar c = ZZDateParser.parse("Sun, 13 Feb 2000 18:04:26 -0200");
	//System.out.println(c.toString());
	//System.out.println(c.getTime().getTime());
	//System.out.println(c.get(Calendar.DATE));
	//System.out.println(c.get(Calendar.MONTH));
	//System.out.println("ZONE_OFFSET: "
	//		   + (c.get(Calendar.ZONE_OFFSET))); // in hours
        //System.out.println("DST_OFFSET: "
	//		   + (c.get(Calendar.DST_OFFSET))); // in hours

	System.out.println("***********************");
	System.out.println(parse(c));
    }*/
}
