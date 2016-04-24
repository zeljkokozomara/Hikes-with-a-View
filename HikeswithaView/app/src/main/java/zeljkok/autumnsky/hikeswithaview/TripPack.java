package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.content.res.Resources;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 *  Represents single trip. Knows how to load itself from network
  */
public class TripPack extends HWVContent
{
    // standard tag for logging
    public static final String TRIP_PACK_TAG = "HWV.TripPack";

    // assets.xml doesn't use namespaces
    private static final String xml_ns = null;

    private TripNotes  mNotes  = null;            // trip description is mandatory
    private TripGps    mGps    = null;            // GPS is optional
    private TripPhotos mPhotos = null;            // photos are optional

    public TripPack(Context c, String tripCaption)
    {
        super(c, tripCaption);
    }

    public TripPhotos getPhotos(){return mPhotos;}
    public TripGps    getGps   (){return mGps;}
    public TripNotes  getNotes (){return mNotes;}



    void parseAssets (File assetFile) throws XmlPullParserException, IOException
    {
        String path = assetFile.getParent();

        InputStream in = new FileInputStream(assetFile);
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, TripPack.xml_ns, m_context.getString(R.string.assets_docelem));
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                String name = parser.getName();

                if (name.equals( m_context.getString(R.string.trip_description) ))
                {
                    mNotes = new TripNotes(m_context);
                    mNotes.loadFromXML(new File(path + File.separator +
                            parser.getAttributeValue(TripPack.xml_ns, m_context.getString(R.string.trip_attribute_file) )) );

                   // mNotes.dump(mTripName);
                }

                else if (name.equals( m_context.getString(R.string.trip_gps) ))
                {
                    mGps = new TripGps (m_context);
                    mGps.loadFromXML(new File(path + File.separator +
                            parser.getAttributeValue(TripPack.xml_ns, m_context.getString(R.string.trip_attribute_file) )) );

                    // mGps.dump(mTripName);
                }

                else if (name.equals( m_context.getString(R.string.trip_photos) ))
                {
                    mPhotos = new TripPhotos (m_context);
                    mPhotos.loadFromXML(new File(path + File.separator +
                            parser.getAttributeValue(TripPack.xml_ns, m_context.getString(R.string.trip_attribute_file) )) );
                }
                else
                    HWVUtilities.skipXml (parser);

                parser.nextTag ();
            }

        }
        finally
        {
            in.close();
        }

    }



}
