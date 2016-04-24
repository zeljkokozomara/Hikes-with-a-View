package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by zeljkok on 27/12/2015.
 */
public class HWVAsset implements IAssetDownload
{
    // standard tag for logging
    public static final String HWV_ASSET_TAG = "HWV.HWVAsset";

    public enum DownloadState
    {
        UPDATE, ASSET
    };

    public enum AssetType
    {
        REGION, CATALOG, TRIP, RATING;
        public static String toString(AssetType type)
        {
            String ret = "";
            if (type == AssetType.REGION) ret = "Region";
            else if (type == AssetType.CATALOG) ret = "Catalog";
            else if (type == AssetType.TRIP) ret = "Trip";
            else if (type == AssetType.RATING) ret = "Rating";
            else assert false : type;

            return ret;
        }

        public static String AssetSuffix (AssetType type)
        {
            String suffix = null;

            if (type == AssetType.CATALOG)
                suffix = new String(HWVConstants.HWV_CATALOGS_SUFFIX);

            else if (type == AssetType.TRIP)
                suffix = new String (HWVConstants.HWV_TRIPS_SUFFIX);

            else if (type == AssetType.RATING)
                suffix = new String (HWVConstants.HWV_RATINGS_SUFFIX);

            else if (type == AssetType.REGION)
                suffix = new String (HWVConstants.HWV_REGIONS_SUFFIX);

            else assert false : type;

            return suffix;
        }

        public static int AssetMessage (AssetType type)
        {
            int msgid = 0;

            if (type == AssetType.CATALOG)
                msgid = R.string.progress_catalog_download;

            else if (type == AssetType.TRIP)
                msgid = R.string.progress_trip_download;

            else if (type == AssetType.RATING)
                msgid = R.string.progress_ratings_download;

            else if (type == AssetType.REGION)
                msgid = R.string.progress_regions_download;

            else assert false : type;

            return msgid;
        }
    };



    private IAssetDownload  mClientCallback = null;
    private DownloadState   mDownloadState;
    private Context         mContext = null;
    private String          mAssetCaption = null;
    private String          mAssetName = null;
    private AssetType       mType;
    private File            mAssetFolder = null;
    private String          mAssetURL = null;

    public HWVAsset (Context c, String assetName, String assetCaption, AssetType type)
    {
        mContext = c;
        mAssetName = new String (assetName);
        mAssetCaption = new String (assetCaption);
        mType = type;
    }

    String                  mUpdateLocal = null;      // read from local update file for this asset
    private boolean         mDownloadAsset = false;   // flag indicating if we need to download asset based on update file



    public File handleFolderCreation () throws IOException
    {
        mAssetFolder = mContext.getFilesDir();
        Log.d(HWV_ASSET_TAG, "Root Internal storage: " + mAssetFolder.getAbsolutePath());

        // this is root of user data; there will be some other junk there. So 1st time around
        // make sure we have our root
        String assetpath = mAssetFolder.getAbsolutePath() + File.separator + AssetType.AssetSuffix(mType);

        mAssetFolder = new File(assetpath);
        if (mAssetFolder.exists() == false)
            mAssetFolder.mkdir();


        // Up to HWVConstants.NUM_PERSISTED_ASSETS subfolders
        assetpath = mAssetFolder.getAbsolutePath() + File.separator + mAssetCaption;
        Log.d(HWV_ASSET_TAG, "Trip asset Path: " + assetpath);

        // enumerate folders in root to see if we have this one
        // remember the oldest along the way, if we have to remove it
        int iEntries = 0;
        File oldest = null;
        File assetFolder = null;

        for (File entry: mAssetFolder.listFiles() )
        {
            Log.d(HWV_ASSET_TAG, "Asset Folder: " + entry.getName() );
            iEntries++;

            if (null == oldest) oldest = entry;

            if (entry.lastModified() < oldest.lastModified() )
                oldest = entry;

            if (entry.getName().equalsIgnoreCase(mAssetCaption) == true)
            {
                assetFolder = entry;
                Log.d(HWV_ASSET_TAG, "Asset name: " + mAssetCaption + " cached before!");
            }
        }

        // if we don't have this folder, must create it. If we already have
        // max number of folders cached, delete the oldest one
        if (null == assetFolder)
        {
            if (iEntries >= HWVConstants.NUM_PERSISTED_ASSETS)
            {
                Log.d(HWV_ASSET_TAG, "Deleting asset folder: " + oldest.getAbsolutePath() + " to make space for: " + mAssetCaption);
                oldest.delete();
            }

            // now create folder
            assetFolder = new File(assetpath);
            assetFolder.mkdir();
        }

        mAssetFolder = assetFolder;
        return assetFolder;
    }

    public File handleAssetDownload (String assetURL, boolean checkUpdate, IAssetDownload callback)
    {
        mClientCallback = callback;
        mAssetURL = assetURL;

        if (mAssetURL.endsWith("/") == false)
            mAssetURL += "/";

        String hwvfile  = new String(mAssetFolder.getAbsolutePath() +
                File.separator + mAssetName + HWVConstants.HWV_FILE_EXTENSION);
        String updatefile = new String(mAssetFolder.getAbsolutePath() + File.separator + HWVConstants.UPDATE_FILE_NAME);
        String updateURL = mAssetURL + HWVConstants.UPDATE_FILE_NAME;
        mDownloadAsset = false;

        File assetFile = new File(mAssetFolder.getAbsolutePath() + File.separator +
                HWVConstants.ASSETS_FILE_NAME + HWVConstants.XML_FILE_EXTENSION );

        if (assetFile.exists() == false) mDownloadAsset = true;

        if (checkUpdate == true)
        {
            try   // this one will throw if it doesn't exist (i.e. 1st time, or has been recycled)
            {
                mUpdateLocal = HWVUtilities.readFile(updatefile);
            }
            catch (Exception ex)
            {
                Log.d(HWV_ASSET_TAG, "Exception thrown while trying to read update file for asset: " + mAssetName +
                        "Reason: " + ex.getLocalizedMessage());

                mDownloadAsset = true;
            }

            // download update in each case when update flag is set
            DownloadTask downloader = new DownloadTask();

            downloader.mParams.mCallback = this;
            downloader.mParams.mOutPath = updatefile;
            downloader.mParams.mContext = mContext;
            downloader.mParams.mMessageId = R.string.progress_update_download;
            downloader.mParams.mZipped = false;   // update file is never zipped

            mDownloadState = DownloadState.UPDATE;
            downloader.execute(updateURL);

            return null;
        }

        // if here, update flag was not set. If we don't have asset file, trigger its download
        if (mDownloadAsset == true)
        {
            DownloadTask downloader = new DownloadTask();

            downloader.mParams.mCallback  = this;
            downloader.mParams.mOutPath   = hwvfile;
            downloader.mParams.mContext   = mContext;
            downloader.mParams.mMessageId = AssetType.AssetMessage(mType);
            downloader.mParams.mZipped    = true;

            mDownloadState = DownloadState.ASSET;
            downloader.execute(mAssetURL + mAssetName + HWVConstants.HWV_FILE_EXTENSION );

            return null;
        }

        // finally if here, asset file was cached & we were not asked to check update file
        return assetFile;
    }

    // at this level we ensure we have up-to-date asset file
    // main purpose is to abstract all "update" handling from higher layers
    public void onDownloadComplete     (File assetPath,   int status)
    {
        // If download failed (regardless if it is update of HWV), just throw back an error
        if (status != HWVConstants.HWV_SUCCESS)
        {
            Log.e(HWV_ASSET_TAG, "Asset download failure");
            mClientCallback.onDownloadComplete(null, status);

            return;
        }

        // If we were downloading HWV file, unpack it
        if (mDownloadState == HWVAsset.DownloadState.ASSET)
        {
            File assetFile = null;
            try
            {
                assetFile = HWVUtilities.unpackAssets(assetPath);
            }
            catch (Exception ex)
            {
                Log.e(HWV_ASSET_TAG, "Exception thrown while upacking HWV file. Cause: " + ex.getLocalizedMessage() );
                mClientCallback.onDownloadComplete(null, HWVConstants.HWV_ERR_UNPACK);

                return;
            }

            mClientCallback.onDownloadComplete(assetFile, status);
            return;
        }

        // if here, we were downloading update file
        String hwvfile  = new String(mAssetFolder.getAbsolutePath() +
                File.separator + mAssetName + HWVConstants.HWV_FILE_EXTENSION);
        String updatefile = new String(mAssetFolder.getAbsolutePath() + File.separator + HWVConstants.UPDATE_FILE_NAME);

        // first check if download asset flag was set
        if (mDownloadAsset == true)
        {
            Log.d(HWV_ASSET_TAG, "Downloading asset: " + mAssetName + " from URL: " + mAssetURL);

            DownloadTask downloader = new DownloadTask();

            downloader.mParams.mCallback  = this;
            downloader.mParams.mOutPath   = hwvfile;
            downloader.mParams.mContext   = mContext;
            downloader.mParams.mMessageId = AssetType.AssetMessage(mType);
            downloader.mParams.mZipped    = true;

            mDownloadState = DownloadState.ASSET;
            downloader.execute(mAssetURL + mAssetName + HWVConstants.HWV_FILE_EXTENSION );

            return;
        }

        // here we have new update string, but old one was available
        // compare the two; if no difference, simply return cached asset
        String strUpdateFromURL = "";
        try
        {
            strUpdateFromURL = HWVUtilities.readFile(updatefile);
        }
        catch (Exception ex)
        {
            Log.d(HWV_ASSET_TAG, "Exception thrown while trying to read update file for asset: " + mAssetName +
                    "Reason: " + ex.getLocalizedMessage() );

            // treat this as unrecoverable error for now, as we could not read file
            // we just downloaded!
            mClientCallback.onDownloadComplete(null, HWVConstants.HWV_ERR_FILE_IO);

            return;

        }

        // if update strings are identical, return asset xml file. At this stage
        // we MUST have it
        if (strUpdateFromURL.equalsIgnoreCase(mUpdateLocal ))
        {
            File assetFile = new File(mAssetFolder.getAbsolutePath() + File.separator +
                    HWVConstants.ASSETS_FILE_NAME + HWVConstants.XML_FILE_EXTENSION );

            if (assetFile.exists() == false)
            {
                if (!false) throw new AssertionError(assetFile);
            }

            mClientCallback.onDownloadComplete(assetFile, HWVConstants.HWV_SUCCESS);
            return;

        }

        // finally here we must download new asset file because cached is dated
        DownloadTask downloader = new DownloadTask();

        downloader.mParams.mCallback  = this;
        downloader.mParams.mOutPath   = hwvfile;
        downloader.mParams.mContext   = mContext;
        downloader.mParams.mMessageId = AssetType.AssetMessage(mType);
        downloader.mParams.mZipped    = true;

        mDownloadState = DownloadState.ASSET;
        downloader.execute(mAssetURL + mAssetName + HWVConstants.HWV_FILE_EXTENSION );

    }
}
