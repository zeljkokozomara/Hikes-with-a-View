package zeljkok.autumnsky.hikeswithaview;

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
    public class GpsLayout
    {
        public Integer zoom, bearing, tilt;
        public String  LAT, LONG;
    }

    public class GpsWaypoint
    {
        public String LAT, LONG, ALT, DIST;
        public String title, caption;
    }

    protected List<GpsWaypoint> mWaypoints = new ArrayList<GpsWaypoint>();
    public List<GpsWaypoint> getWaypoins (){return mWaypoints;}

    public void loadFromXML (String strPath)  throws IOException
    {
        // open gps file input stream
        InputStream stream = new FileInputStream(strPath);

        // parse gps file

        // close stream
        stream.close();
    }
}