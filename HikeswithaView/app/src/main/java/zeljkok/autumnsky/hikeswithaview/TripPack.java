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
public class TripPack implements IAssetDownload
{
    // standard tag for logging
    public static final String TRIP_PACK_TAG = "HWV.TripPack";

    // assets.xml doesn't use namespaces
    private static final String xml_ns = null;

    private Context    m_context = null;

    private TripNotes  mNotes  = null;            // trip description is mandatory
    private TripGps    mGps    = null;            // GPS is optional
    private TripPhotos mPhotos = null;            // photos are optional

    private File       mGarmin = null;            // garmin.gpx is optional; in either case we don't parse it

    private String     mTripName = null;          // trip name and caption are read from catalog
    private String     mTripCaption = null;

    public  String     getTripName(){return mTripName;}
    public  String     getTripCaption(){return mTripCaption;}

    private HWVAsset   mTripAsset = null;

    private IAssetStatus mClientCallback = null;

    public TripPack(Context c){m_context = c;}

    public TripPhotos getPhotos(){return mPhotos;}
    public TripGps    getGps   (){return mGps;}
    public TripNotes  getNotes (){return mNotes;}

    public File       getGarmin (){return mGarmin;}

    // public export to load hike asset. URL is passed from client, as extracted
    // from catalog.  This routine checks against local cache; if file is here & not changed
    // cached version is used.  Cache is always tar-ed and gziped;  extracted files are
    // valid only during lifetime of this object
    public void load(String tripName, String tripCaption, String strAssetURL, IAssetStatus callback)
    {
        mClientCallback = callback;

        mTripName    = new String(tripName);
        mTripCaption = new String(tripCaption);

        mTripAsset = new HWVAsset(m_context, tripName, HWVAsset.AssetType.TRIP);

        // ensure we have asset folder
        try
        {
            mTripAsset.handleFolderCreation();
        }
        catch (Exception ex)
        {
            Log.e(TRIP_PACK_TAG, "Exception thrown while accessing local trip folder. Cause: " + ex.getLocalizedMessage() );
            mClientCallback.onAssetComplete(HWVConstants.HWV_ERR_FILE_IO, mTripName, HWVAsset.AssetType.TRIP);
        }

        // fetch hwv file; we will be notified async on success. We
        // report back to the client only when all downloading/unzipping/xml parsing has completed
        File assetFile = mTripAsset.handleAssetDownload(strAssetURL, true, this);
        if (null != assetFile)  // was available synchronously
           onDownloadComplete (assetFile, HWVConstants.HWV_SUCCESS);
    }



    public void onDownloadComplete (File assetFile, int status)
    {
        // if failure, just propagate to client. If successful,
        // we have to unzip then parse all the XMLs
        if (status != HWVConstants.HWV_SUCCESS)
        {
            mClientCallback.onAssetComplete(status, mTripName, HWVAsset.AssetType.TRIP);
            return;
        }

        if (assetFile.exists() == false)
        {
            if (!false) throw new AssertionError(assetFile);
        }

        // now parse xml assets
        try
        {
            parseAssets(assetFile);
        }
        catch (Exception ex)
        {
            mClientCallback.onAssetComplete(HWVConstants.HWV_ERR_XML, mTripName, HWVAsset.AssetType.TRIP);
            Log.e(TRIP_PACK_TAG, "Exception thrown while parsing assets for trip [" + mTripName + "]. Cause:" +
               ex.getLocalizedMessage() );

            return;
        }

        // we are done -- trip is loaded!
        mClientCallback.onAssetComplete(HWVConstants.HWV_SUCCESS, mTripName, HWVAsset.AssetType.TRIP);
    }

    private void parseAssets (File assetFile) throws XmlPullParserException, IOException
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
                else if (name.equals( m_context.getString(R.string.trip_garmin) ))
                {
                    mGarmin = new File(path + File.separator + parser.getAttributeValue(TripPack.xml_ns, m_context.getString(R.string.trip_attribute_file) ));
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
