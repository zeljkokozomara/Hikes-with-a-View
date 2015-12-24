package zeljkok.autumnsky.hikeswithaview;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zeljkok on 15/12/2015.
 *
 * This class wraps Notes from Trip asset file.  Knows how to load itself
 * from XML file, then provides public accessors to data members
 *
 * XML file must be serialized locally, not as URL
 */
public class TripNotes implements ITripData
{
    // generic code-description pair
    public class Rating
    {
        public String mCode;
        public String mDescription;
    }

    // trip metrics: Elevation is generally in meters, distance in kilometers
    // but we keep this as strings to have no coupling in code & be able to manage
    // entirely with data
    public class Metrics
    {
        public String mElevationTotal;
        public String mElevationStart;
        public String mElevationMax;

        public String mDistance;
    }

    protected TripNotes.Rating  mDifficulty;   // Trip Difficulty
    protected TripNotes.Rating  mSnowFactor;   // Snow-Factor

    protected String            mScenery;      // Scenery description
    protected TripNotes.Rating  mTime;         // Trip time

    protected TripNotes.Metrics mMetrics;      // trip metrics: elevation - distance
    protected String            mGear;         // required/recommended gear
    protected String            mWater;        // Water description

    protected TripNotes.Rating  mDog;          // Dogs feasibility

    protected String            mSummary;      // summary text, always non-empty
    protected String            mPhotoCorner;  // photo-corner text; can be empty
    protected String            mGoingFurther; // going further text; can be empty

    public void loadFromXML (String strPath)  throws IOException
    {
        // open notes.xml input stream
        InputStream stream = new FileInputStream(strPath);

        // parse notes

        // close stream
        stream.close();
    }
}
