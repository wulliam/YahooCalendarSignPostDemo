package org.wuliang;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.UUID;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
/**
 *
 * @author wuliang
 */
public class YahooCalendarSignPostDemo {
    private static String PROTECTED_RESOURCE_URL = "http://social.yahooapis.com/v1/user/%1s/profile?format=json";
    private static final String YAHOO_GUID = "xoauth_yahoo_guid";
    private static String yahooGuid = null;
    public static void main(String[] args) throws Exception {
        OAuthConsumer consumer = getConsumer();
        System.out.println("\r\n\r\n*******getProfile*****************\r\n");
        getProfile(consumer);
        System.out.println("\r\n\r\n*******doCreateEvent*****************\r\n");
        doCreateEvent(consumer);
    }
    
    static public void doCreateEvent(OAuthConsumer consumer) throws Exception {
        // create a new EVENT
        HttpPost post = null;
        UUID uuid = UUID.randomUUID();
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar c = new net.fortuna.ical4j.model.Calendar();
        c.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
        c.getProperties().add(Version.VERSION_2_0);
        c.getProperties().add(CalScale.GREGORIAN);
        TimeZoneRegistry registry = builder.getRegistry();
        VTimeZone tz = registry.getTimeZone("Europe/Madrid").getVTimeZone();
        c.getComponents().add(tz);
        VEvent vevent = new VEvent(new net.fortuna.ical4j.model.Date(),
                new Dur(0, 1, 0, 0), "test");
        vevent.getProperties().add(new Uid(uuid.toString()));
        c.getComponents().add(vevent);
        String href = "https://caldav.calendar.yahoo.com/dav/wuliang_org@yahoo.com/Calendar/Wu_Liang/" + uuid.toString() + ".ics";
        post = new HttpPost(href);
        StringEntity body = new StringEntity(URLEncoder.encode(c.toString(), "UTF-8"));
        body.setContentType("text/calendar");
        post.setEntity(body);
        consumer.sign(post);

        System.out.println("doCreateEvent...");

        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(post);

        System.out.println("Response: "
                + response.getStatusLine().getStatusCode() + " "
                + response.getStatusLine().getReasonPhrase());
        System.out.println("body:" + toStr(response.getEntity().getContent()));
    }

    public static OAuthConsumer getConsumer() throws Exception {

        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
                "dj0yJmk9U2FpR0U5UmNUcU05JmQ9WVdrOWVFUlpSSFpQTkdVbWNHbzlNVFkyTVRnNU5URTJNZy0tJnM9Y29uc3VtZXJzZWNyZXQmeD1lMw--",
                "baa7bdb6e4e1b609c94d7e48b27c30d65fccc6b3");

        OAuthProvider provider = new DefaultOAuthProvider(
                "https://api.login.yahoo.com/oauth/v2/get_request_token",
                "https://api.login.yahoo.com/oauth/v2/get_token",
                "https://api.login.yahoo.com/oauth/v2/request_auth");

        System.out.println("Fetching request token from Yahoo...");

        // we do not support callbacks, thus pass OOB
        String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

        System.out.println("Request token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");
        System.out.println("Enter the verification code and hit ENTER when you're done");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();

        System.out.println("Fetching access token from Yahoo...");

        provider.retrieveAccessToken(consumer, code);

        System.out.println("Access token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());
        yahooGuid = provider.getResponseParameters().get(YAHOO_GUID).first();
        System.out.println("yahoo_guid: " + yahooGuid);
        PROTECTED_RESOURCE_URL = String.format(PROTECTED_RESOURCE_URL, yahooGuid);

        return consumer;
    } 
   
    
    public static String toStr(InputStream in) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        return br.readLine();
    }
    
    public static void  getProfile(OAuthConsumer consumer) throws Exception {
        HttpGet request = new HttpGet(
                "http://social.yahooapis.com/v1/user/"+yahooGuid+"/profile?format=json");

        consumer.sign(request);

        System.out.println("getProfile...");

        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        System.out.println("Response: "
                + response.getStatusLine().getStatusCode() + " "
                + response.getStatusLine().getReasonPhrase());
        System.out.println("body:" + toStr(response.getEntity().getContent()));
    }
   
}
