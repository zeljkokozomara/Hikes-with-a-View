package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *  Represents single trip. Knows how to load itself from network
  */
public class TripPack
{
    public static final String TRIP_PACK_TAG = "HikeswithaView.TripPack";

    protected TripNotes  mNotes  = new TripNotes(); // trip description is mandatory
    protected TripGps    mGps    = null;            // GPS is optional
    protected TripPhotos mPhotos = null;            // photos are optional

    protected String     mGarmin = null;            // garmin.gpx is optional; in either case we don't parse it

    protected String mAssetFile;   // local asset file, fetched from URL or internal cache
    protected String mTripName;    // trip name, passed from caller as read from catalog and verified in assets


    // public export to load hike asset. URL is passed from client, as extracted
    // from catalog.  This routine checks against local cache; if file is here & not changed
    // cached version is used.  Cache is always tar-ed and gziped;  extracted files are
    // valid only during lifetime of this object
    public void load(Context context, String tripName, String strAssetURL) throws IOException
    {
        File root = context.getFilesDir();
        Log.d(TRIP_PACK_TAG, "Root Internal storage: " + root.getAbsolutePath());

        String tripfolder = root.getAbsolutePath() + File.separator + tripName;
        Log.d(TRIP_PACK_TAG, "Trip Path: " + tripfolder);

        // enumerate folders in root
        int iTrips = 0;
        File cached = null;
        for (File entry: root.listFiles() )
        {
            Log.d(TRIP_PACK_TAG, "Trip Folder: " + entry.getName() );
            iTrips++;

            if (entry.getName().equalsIgnoreCase(tripName) == true)
            {
                cached = entry;
                Log.d(TRIP_PACK_TAG, "Trip name: " + tripName + " cached before!");
            }
        }

        if (null == cached) handleFolderCreation (iTrips, tripfolder);


    }


    protected void handleFolderCreation (int iTrips, String tripfolder)
    {
        // TODO: if we are at the limit, delete oldest before we create new one
        if (iTrips >= HWVConstants.NUM_PERSISTED_TRIPS)
        {

        }

        // now create folder
        File tf = new File(tripfolder);
        tf.mkdir();
    }
    protected void unzipAssets ()
    {

    }
}
