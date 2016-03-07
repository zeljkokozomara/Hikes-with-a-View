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
public class TripGps implements IHWVContent
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

    public class GpsTrekPoint
    {
        public LatLng gps;
        public String ALT;
    }
    public class GpsWaypoint
    {
        public GpsTrekPoint tpt;
        public String DIST;
        public String title, caption, icon;
    }

    private Context   m_context;
    private GpsLayout mGpsLayout = new GpsLayout();
    public  GpsLayout getGpsLayout(){return mGpsLayout;}

    private File      mGarmin = null;
    public  File      getGpsFile(){return mGarmin;}

    private List<GpsWaypoint> mWaypoints = new ArrayList<GpsWaypoint>();
    public List<GpsWaypoint> getWaypoints (){return mWaypoints;}

    private List<GpsTrekPoint> mTrekPoints = new ArrayList<GpsTrekPoint>();
    public  List<GpsTrekPoint> getTrekPoints(){return mTrekPoints;}

    public TripGps(Context c){m_context = c;}
    public void loadFromXML (File garminFile)  throws XmlPullParserException, IOException
    {
        // remember as UI will allow wireless transfer from Device to Garmin
        mGarmin = garminFile;

        InputStream in = new FileInputStream(garminFile);
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

                if (name.equals( m_context.getString(R.string.gps_waypoint) ))
                    readWaypoint(parser);

                else if (name.equals( m_context.getString(R.string.gps_trek) ))
                    readTrek(parser);

                else
                    HWVUtilities.skipXml(parser);
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

        // get the layout string
        String layout = HWVUtilities.readText(parser);

        // parse the layout string
        // CENTERLAT=N49° 41.415';CENTERLONG=W123° 07.613';ZOOM=14;BEARING=90;TILT=45
        String [] tokens = layout.split(m_context.getString(R.string.gps_layout_token_separator) );
        for (String item: tokens)
        {
            String [] nameval = item.split(m_context.getString(R.string.gps_layout_name_value_separator));

            if (nameval[0].equals(m_context.getString(R.string.gps_layout_center_lat) ) )
                mGpsLayout.LAT = nameval[1];

            else if (nameval[0].equals(m_context.getString(R.string.gps_layout_center_long) ) )
                mGpsLayout.LONG = nameval[1];

            else if (nameval[0].equals(m_context.getString(R.string.gps_layout_zoom) ) )
                mGpsLayout.zoom = nameval[1];

            else if (nameval[0].equals(m_context.getString(R.string.gps_layout_bearing) ) )
                mGpsLayout.bearing = nameval[1];

            else if (nameval[0].equals(m_context.getString(R.string.gps_layout_tilt) ) )
                mGpsLayout.tilt = nameval[1];

            else
                throw new IOException("Invalid paramenter in GPS Layout: " + nameval[0]);
        }

    }

    private void readWaypoint(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, TripGps.xml_ns,
                m_context.getString(R.string.gps_waypoint));         // redundant, following stack overflow

        // allocate new waypoint
        GpsWaypoint wp = new GpsWaypoint();
        wp.tpt = new GpsTrekPoint();

        // lat and long are attributes of current element
        String LAT = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_lat));
        String LONG = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_long));

        wp.tpt.gps = new LatLng(Double.parseDouble(LAT), Double.parseDouble(LONG) );

        // rest are child elements
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();

            if (name.equals(m_context.getString(R.string.gps_elevation)))
                wp.tpt.ALT = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.gps_waypoint_title)))
                wp.title = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.gps_waypoint_caption)))
                wp.caption = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.gps_waypoint_icon)))
                wp.icon = HWVUtilities.readText(parser);

            else
                HWVUtilities.skipXml(parser);
        }

        // finally add this waypoint
        mWaypoints.add(wp);

    }

    private void readTrekpoint(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, TripGps.xml_ns,
                m_context.getString(R.string.gps_trek_point));         // redundant, following stack overflow

        // allocate new trekpoint
        GpsTrekPoint tpt = new GpsTrekPoint();

        // lat and long are attributes of current element
        String LAT = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_lat));
        String LONG = parser.getAttributeValue(TripGps.xml_ns, m_context.getString(R.string.gps_long));

        tpt.gps = new LatLng(Double.parseDouble(LAT), Double.parseDouble(LONG) );

        // rest are child elements
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();

            if (name.equals(m_context.getString(R.string.gps_elevation)))
                tpt.ALT = HWVUtilities.readText(parser);

            else
                HWVUtilities.skipXml(parser);
        }

        // finally add this trekpoint
        mTrekPoints.add(tpt);

    }

    private void readTrek(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, TripGps.xml_ns,
                m_context.getString(R.string.gps_trek));         // redundant, following stack overflow

        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();

            if (name.equals(m_context.getString(R.string.gps_layout)))
            {
                readLayout(parser);

                // advance to next tag
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

            }

            else if (name.equals(m_context.getString(R.string.gps_trek_segment)))
            {
                while (parser.next() != XmlPullParser.END_TAG)
                {
                    if (parser.getEventType() != XmlPullParser.START_TAG)
                        continue;

                    name = parser.getName();

                     if (name.equals(m_context.getString(R.string.gps_trek_point)))
                        readTrekpoint(parser);
                    else
                        HWVUtilities.skipXml(parser);
                }
            }
            else   // not interested in rest of garmin stuff
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
        Log.d(TripGps.TRIP_GPS_TAG, "Waypoints: ");
        for (int i = 0; i < mWaypoints.size(); i++)
        {
            GpsWaypoint wp = mWaypoints.get(i);
            Log.d(TripGps.TRIP_GPS_TAG, (i + 1) + ". LATLNG: " + wp.tpt.gps.toString() + " ALT: " + wp.tpt.ALT + " DIST: " + wp.DIST);
            Log.d(TripGps.TRIP_GPS_TAG, "Title: " + wp.title);
            Log.d(TripGps.TRIP_GPS_TAG, "Caption: " + wp.caption);
            Log.d(TripGps.TRIP_GPS_TAG, "Icon: " + wp.icon);
            Log.d(TripGps.TRIP_GPS_TAG, " ");
        }

        Log.d(TripGps.TRIP_GPS_TAG, "Trekpoints: ");
        for (int i = 0; i < mTrekPoints.size(); i++)
        {
            GpsTrekPoint tpt = mTrekPoints.get(i);
            Log.d(TripGps.TRIP_GPS_TAG, (i + 1) + ". LATLNG: " + tpt.gps.toString() + " ALT: " + tpt.ALT);
            Log.d(TripGps.TRIP_GPS_TAG, " ");
        }

        Log.d(TripGps.TRIP_GPS_TAG, "======================");
    }
}
