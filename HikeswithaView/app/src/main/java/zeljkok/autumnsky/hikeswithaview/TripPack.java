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
public class TripPack implements IAssetDownload
{
    // standard tag for logging
    public static final String TRIP_PACK_TAG = "HWV.TripPack";

    private TripNotes  mNotes  = new TripNotes(); // trip description is mandatory
    private TripGps    mGps    = null;            // GPS is optional
    private TripPhotos mPhotos = null;            // photos are optional

    private String     mGarmin = null;            // garmin.gpx is optional; in either case we don't parse it

    private String     mTripName = null;          // trip is asset, as passed from the client
    private HWVAsset   mTripAsset = null;

    private IAssetStatus mClientCallback = null;

    // public export to load hike asset. URL is passed from client, as extracted
    // from catalog.  This routine checks against local cache; if file is here & not changed
    // cached version is used.  Cache is always tar-ed and gziped;  extracted files are
    // valid only during lifetime of this object
    public void load(Context context, String tripName, String strAssetURL, IAssetStatus callback)
    {
        mClientCallback = callback;

        mTripName = new String(tripName);
        mTripAsset = new HWVAsset(context, tripName, HWVAsset.AssetType.TRIP);

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
        mTripAsset.handleAssetDownload(strAssetURL, this);

    }


    // this helper unzips hwv file and returns assets.xml
    private File unzipAssets (File hwvFile)
    {
        File assetFile = null;

        return assetFile;
    }


    public void onDownloadComplete (File hwvFile, int status)
    {
        // if failure, just propagate to client. If successful,
        // we have to unzip then parse all the XMLs
        if (status != HWVConstants.HWV_SUCCESS)
             mClientCallback.onAssetComplete(status, mTripName, HWVAsset.AssetType.TRIP);

        // unzip hwv file
        File assetFile = unzipAssets (hwvFile);

        // now parse asset XML file

        // ask contained helper objects to parse gps, photos, and notes files

        // we are done -- trip is loaded!
        mClientCallback.onAssetComplete(HWVConstants.HWV_SUCCESS, mTripName, HWVAsset.AssetType.TRIP);
    }
}
