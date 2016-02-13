package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
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
    // standard tag for logging
    public static final String TRIP_NOTES_TAG = "HWV.TripNotes";

    // notes.xml doesn't use namespaces
    private static final String xml_ns = null;

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

    private Context m_context = null;

    protected TripNotes.Rating  mDifficulty;   // Overall Trip Difficulty
    protected TripNotes.Rating  mSnowFactor;   // Snow-Factor
    protected TripNotes.Rating  mTrail;        // Trail Difficulty

    protected String            mScenery;      // Scenery description
    protected TripNotes.Rating  mTime;         // Trip time

    protected TripNotes.Metrics mMetrics;      // trip metrics: elevation - distance
    protected String            mGear;         // required/recommended gear
    protected String            mWater;        // Water description

    protected TripNotes.Rating  mDog;          // Dogs feasibility

    protected String            mSummary;      // summary text, always non-empty
    protected String            mPhotoCorner;  // photo-corner text; can be empty
    protected String            mGoingFurther; // going further text; can be empty

    public TripNotes (Context c)
    {
        m_context = c;
        mMetrics = new TripNotes.Metrics();
    }

    // just for debugging...
    void dump(String tripName)
    {
        Log.d(TripNotes.TRIP_NOTES_TAG, "Trip: " + tripName);
        Log.d(TripNotes.TRIP_NOTES_TAG, "======================");
        Log.d(TripNotes.TRIP_NOTES_TAG, "Difficulty: " + mDifficulty.mCode + ", " + mDifficulty.mDescription);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Snow Factor: " + mSnowFactor.mCode + ", " + mSnowFactor.mDescription);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Scenery: " + mScenery);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Time: " + mTime.mCode + " " + mTime.mDescription);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Elevation: Start " + mMetrics.mElevationStart + ", Max " + mMetrics.mElevationMax +
           ", Total " + mMetrics.mElevationTotal);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Distance: " + mMetrics.mDistance);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Gear: " + mGear);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Water: " + mWater);
        Log.d(TripNotes.TRIP_NOTES_TAG, "Dog: " + mDog.mCode + " " + mDog.mDescription);
        Log.d(TripNotes.TRIP_NOTES_TAG, " ");
        Log.d(TripNotes.TRIP_NOTES_TAG, "Summary: " + mSummary);
        Log.d(TripNotes.TRIP_NOTES_TAG, " ");
        Log.d(TripNotes.TRIP_NOTES_TAG, "Photo Corner: " + mPhotoCorner);
        Log.d(TripNotes.TRIP_NOTES_TAG, " ");
        Log.d(TripNotes.TRIP_NOTES_TAG, "Going Further: " + mGoingFurther);
        Log.d(TripNotes.TRIP_NOTES_TAG, "======================");

    }
    public void loadFromXML (File notesFile)    throws XmlPullParserException, IOException
    {
        InputStream in = new FileInputStream(notesFile);
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, TripNotes.xml_ns, m_context.getString(R.string.notes_docelem));
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                String name = parser.getName();

                if (name.equals( m_context.getString(R.string.notes_abstract) ))
                   readAbstract(parser);

                else if (name.equals( m_context.getString(R.string.notes_summary) ))
                    mSummary = HWVUtilities.readText(parser);

                else if (name.equals( m_context.getString(R.string.notes_photo_corner) ))
                    mPhotoCorner = HWVUtilities.readText(parser);

                else if (name.equals( m_context.getString(R.string.notes_going_further) ))
                    mGoingFurther = HWVUtilities.readText(parser);


            }
        }
        finally
        {
            in.close();
        }
    }

    private void readAbstract(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, TripNotes.xml_ns,
                m_context.getString(R.string.notes_abstract));         // redundant, following stack overflow

        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            if (name.equals(m_context.getString(R.string.notes_ratings_difficulty)))
                mDifficulty = HWVUtilities.readRating(m_context, name, TripNotes.xml_ns, parser);

            else if (name.equals(m_context.getString(R.string.notes_ratings_trail)))
                mTrail = HWVUtilities.readRating(m_context, name, TripNotes.xml_ns, parser);

            else if (name.equals(m_context.getString(R.string.notes_ratings_snow_factor)))
                mSnowFactor = HWVUtilities.readRating(m_context, name, TripNotes.xml_ns, parser);

            else if (name.equals(m_context.getString(R.string.notes_scenery)))
                mScenery = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.notes_time)))
                mTime = HWVUtilities.readRating(m_context, name, TripNotes.xml_ns, parser);

            else if (name.equals(m_context.getString(R.string.notes_elevation)))
            {
                mMetrics.mElevationMax   = parser.getAttributeValue(TripNotes.xml_ns, m_context.getString(R.string.notes_elevation_max));
                mMetrics.mElevationStart = parser.getAttributeValue(TripNotes.xml_ns, m_context.getString(R.string.notes_elevation_start));
                mMetrics.mElevationTotal = parser.getAttributeValue(TripNotes.xml_ns, m_context.getString(R.string.notes_elevation_total));

                parser.next();  // attribute?
            }

            else if (name.equals(m_context.getString(R.string.notes_distance)))
                mMetrics.mDistance = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.notes_gear)))
                mGear = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.notes_water)))
                mWater = HWVUtilities.readText(parser);

            else if (name.equals(m_context.getString(R.string.notes_dog)))
                mDog = HWVUtilities.readRating(m_context, name, TripNotes.xml_ns, parser);

            else
                HWVUtilities.skipXml(parser);
        }

    }



}
