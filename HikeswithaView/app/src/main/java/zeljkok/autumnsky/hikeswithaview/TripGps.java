package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeljkok on 22/12/2015.
 */
public class TripGps implements ITripData
{
    // standard tag for logging
    public static final String TRIP_GPS_TAG = "HWV.TripGps";

    // gps.xml doesn't use namespaces
    private static final String xml_ns = null;

    public class GpsLayout
    {
        public String  zoom, bearing, tilt;
        public String  LAT, LONG;
    }

    public class GpsWaypoint
    {
        public String ALT, DIST;
        public String title, caption, icon;
        public LatLng gps;
    }

    private Context   m_context;
    private GpsLayout mGpsLayout = new GpsLayout();
    public  GpsLayout getGpsLayout(){return mGpsLayout;}

    protected List<GpsWaypoint> mWaypoints = new ArrayList<GpsWaypoint>();
    public List<GpsWaypoint> getWaypoints (){return mWaypoints;}

    public TripGps(Context c){m_context = c;}
    public void loadFromXML (File tripData)  throws XmlPullParserException, IOException
    {

        InputStream in = new FileInputStream(tripData);
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, TripGps.xml_ns, m_context.getString(R.string.gps_docelem));
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                String name = parser.getName();

                if (name.equals( m_context.getString(R.string.gps_layout) ))
                    readLayout(parser);

                else if (name.equals( m_context.getString(R.string.gps_waypoints) ))
                    readWaypoints(parser);
            }
        }
        finally
        {
            in.close();
        }
    }

    private void readLayout(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, TripGps.xml_ns,
                m_context.getString(R.string.gps_layout));         // redundant, following stack overflow

        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            if (name.equals(m_context.getString(R.string.gps_layout_center)))
            {
                mGpsLayout.LAT = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_LAT));
                mGpsLayout.LONG = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_LONG));

                parser.next();  // attribute?
            }

            else if (name.equals(m_context.getString(R.string.gps_layout_zoom)))
                mGpsLayout.zoom = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.gps_layout_bearing)))
                mGpsLayout.bearing = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.gps_layout_tilt)))
                mGpsLayout.tilt = HWVUtilities.readText(parser);

            else
                HWVUtilities.skipXml(parser);
        }

    }

    private void readWaypoints(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, TripGps.xml_ns,
                m_context.getString(R.string.gps_waypoints));         // redundant, following stack overflow

        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            if (name.equals(m_context.getString(R.string.gps_waypoint)))
            {
                GpsWaypoint wp = new GpsWaypoint();
                String LAT = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_LAT));
                String LONG = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_LONG));

                wp.gps = HWVUtilities.gps_from_string(LAT, LONG);
                wp.ALT = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_ALT));
                wp.DIST = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_DIST));

                while (parser.next() != XmlPullParser.END_TAG)
                {
                    if (parser.getEventType() != XmlPullParser.START_TAG)
                        continue;

                    name = parser.getName();

                    if (name.equals(m_context.getString(R.string.gps_waypoint_title)))
                        wp.title = HWVUtilities.readText(parser);

                    else if (name.equals(m_context.getString(R.string.gps_waypoint_caption)))
                        wp.caption = HWVUtilities.readText(parser);

                    else if (name.equals(m_context.getString(R.string.gps_waypoint_icon)) )
                        wp.icon = HWVUtilities.readText(parser);

                    else
                        HWVUtilities.skipXml(parser);
                }

                mWaypoints.add(wp);

            }
            else
                HWVUtilities.skipXml(parser);
        }

    }

    public void dump(String tripName)
    {
        Log.d(TripGps.TRIP_GPS_TAG, "Trip: " + tripName);
        Log.d(TripGps.TRIP_GPS_TAG, "======================");
        Log.d(TripGps.TRIP_GPS_TAG, "Layout - LAT:" + mGpsLayout.LAT + ", LONG: " + mGpsLayout.LONG);
        Log.d(TripGps.TRIP_GPS_TAG, "Layout - zoom: " + mGpsLayout.zoom);
        Log.d(TripGps.TRIP_GPS_TAG, "Layout - bearing: " + mGpsLayout.bearing);
        Log.d(TripGps.TRIP_GPS_TAG, "Layout - tilt: " + mGpsLayout.tilt);
        Log.d(TripGps.TRIP_GPS_TAG, "Waypoint: ");
        for (int i = 0; i < mWaypoints.size(); i++)
        {
            GpsWaypoint wp = mWaypoints.get(i);
            Log.d(TripGps.TRIP_GPS_TAG, (i + 1) + ". LATLNG: " + wp.gps.toString() + " ALT: " + wp.ALT + " DIST: " + wp.DIST);
            Log.d(TripGps.TRIP_GPS_TAG, "Title: " + wp.title);
            Log.d(TripGps.TRIP_GPS_TAG, "Caption: " + wp.caption);
            Log.d(TripGps.TRIP_GPS_TAG, "Icon: " + wp.icon);
            Log.d(TripGps.TRIP_GPS_TAG, " ");
        }

        Log.d(TripGps.TRIP_GPS_TAG, "======================");
    }
}
