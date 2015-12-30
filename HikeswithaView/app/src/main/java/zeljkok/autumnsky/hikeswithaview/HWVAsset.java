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
        REGIONS, CATALOG, TRIP, DIFFICULTY, SNOW_FACTOR
    };

    private IAssetDownload  mClientCallback = null;
    private DownloadState   mDownloadState;
    private Context         mContext = null;
    private String          mAssetName = null;
    private AssetType       mType;
    private File            mAssetFolder = null;
    private String          mAssetURL = null;

    public HWVAsset (Context c, String assetName, AssetType type)
    {
        mContext = c; mAssetName = new String (assetName); mType = type;
    }

    String                  mUpdateLocal = null;      // read from local update file for this asset
    private boolean         mDownloadAsset = false;   // flag indicating if we need to download asset based on update file

    static String AssetExtension (AssetType type)
    {
        String extension = null;

        if (type == AssetType.TRIP)
            extension = new String (HWVConstants.HWV_FILE_EXTENSION);

        else
            extension = new String(HWVConstants.XML_FILE_EXTENSION);


        return extension;
    }

    static String AssetSuffix (AssetType type)
    {
        String suffix = null;

        if (type == AssetType.CATALOG)
            suffix = new String(HWVConstants.HWV_CATALOG_SUFFIX);

        else if (type == AssetType.TRIP)
            suffix = new String (HWVConstants.HWV_TRIP_SUFFIX);

        else if (type == AssetType.DIFFICULTY)
            suffix = new String (HWVConstants.HWV_DIFFICULTY_SUFFIX);

        else if (type == AssetType.REGIONS)
            suffix = new String (HWVConstants.HWV_REGIONS_SUFFIX);

        else if (type == AssetType.SNOW_FACTOR)
            suffix = new String (HWVConstants.HWV_SNOW_FACTOR_SUFFIX);

        else assert false : type;

        return suffix;
    }

    static int AssetMessage (AssetType type)
    {
        int msgid = 0;

        if (type == AssetType.CATALOG)
            msgid = R.string.progress_catalog_download;

        else if (type == AssetType.TRIP)
            msgid = R.string.progress_trip_download;

        else if (type == AssetType.DIFFICULTY)
            msgid = R.string.progress_difficulty_download;

        else if (type == AssetType.REGIONS)
            msgid = R.string.progress_regions_download;

        else if (type == AssetType.SNOW_FACTOR)
            msgid = R.string.progress_snow_factor_download;

        else assert false : type;

        return msgid;
    }

    public File handleFolderCreation () throws IOException
    {
        mAssetFolder = mContext.getFilesDir();
        Log.d(HWV_ASSET_TAG, "Root Internal storage: " + mAssetFolder.getAbsolutePath());

        // this is root of user data; there will be some other junk there. So 1st time around
        // make sure we have our root
        String assetpath = mAssetFolder.getAbsolutePath() + File.separator + HWVAsset.AssetSuffix(mType);

        mAssetFolder = new File(assetpath);
        if (mAssetFolder.exists() == false)
            mAssetFolder.mkdir();

        if (mType != AssetType.TRIP) return mAssetFolder;

        // if asset type is trip, we have to do extra work. Up to HWVConstants.NUM_PERSISTED_ASSETS subfolders
        assetpath = mAssetFolder.getAbsolutePath() + File.separator + mAssetName;
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

            if (entry.getName().equalsIgnoreCase(mAssetName) == true)
            {
                assetFolder = entry;
                Log.d(HWV_ASSET_TAG, "Asset name: " + mAssetName + " cached before!");
            }
        }

        // if we don't have this folder, must create it. If we already have
        // max number of folders cached, delete the oldest one
        if (null == assetFolder)
        {
            if (iEntries >= HWVConstants.NUM_PERSISTED_ASSETS)
            {
                Log.d(HWV_ASSET_TAG, "Deleting asset folder: " + oldest.getAbsolutePath() + " to make space for: " + mAssetName);
                oldest.delete();
            }

            // now create folder
            assetFolder = new File(assetpath);
            assetFolder.mkdir();
        }

        mAssetFolder = assetFolder;
        return assetFolder;
    }

    public void handleAssetDownload (String assetURL, IAssetDownload callback)
    {
        mClientCallback = callback;
        mAssetURL = assetURL;

        if (mAssetURL.endsWith("/") == false)
            mAssetURL += "/";


        String updatefile = new String(mAssetFolder.getAbsolutePath() + File.separator + HWVConstants.UPDATE_FILE_NAME);

        String updateURL = mAssetURL + HWVConstants.UPDATE_FILE_NAME;
        mDownloadAsset = false;

        try   // this one will throw if it doesn't exist (i.e. 1st time, or has been recycled)
        {
            mUpdateLocal = HWVUtilities.readFile(updatefile);
        }
        catch (Exception ex)
        {
            Log.d(HWV_ASSET_TAG, "Exception thrown while trying to read update file for asset: " + mAssetName +
                    "Reason: " + ex.getLocalizedMessage() );

            mDownloadAsset = true;
        }

        // if we had update file, check if we have asset file. If not, must set that flag anyways
        if (mDownloadAsset == false)
        {
            File assetFile = new File(mAssetFolder.getAbsolutePath() + File.separator +
                 mAssetName + HWVAsset.AssetExtension(mType) );

            if (assetFile.exists() == false) mDownloadAsset = true;

        }
        // download new update file in each case. If we couldn't read existing one
        // we keep it for next time
        DownloadTask downloader = new DownloadTask();

        downloader.mParams.mCallback = this;
        downloader.mParams.mOutPath  = updatefile;
        downloader.mParams.mContext  = mContext;
        downloader.mParams.mMessageId = R.string.progress_update_download;

        mDownloadState = DownloadState.UPDATE;
        downloader.execute(updateURL);

    }

    // at this level we ensure we have up-to-date asset file
    // main purpose is to abstract all "update" handling from higher layers
    public void onDownloadComplete     (File assetPath,   int status)
    {
        if (status != HWVConstants.HWV_SUCCESS)
        {
            Log.e(HWV_ASSET_TAG, "Asset download failure");
            mClientCallback.onDownloadComplete(assetPath, status);

            return;
        }

        // If we were downloading asset, we are done
        if (mDownloadState == HWVAsset.DownloadState.ASSET)
        {
            mClientCallback.onDownloadComplete(assetPath, status);
            return;
        }

        String assetfile  = new String(mAssetFolder.getAbsolutePath() +
                File.separator + mAssetName + HWVAsset.AssetExtension(mType) );
        String updatefile = new String(mAssetFolder.getAbsolutePath() + File.separator + HWVConstants.UPDATE_FILE_NAME);

        // here we are dealing with downloaded update string. First check if flag
        // to download asset at any case was set
        if (mDownloadAsset == true)
        {
            Log.d(HWV_ASSET_TAG, "Downloading asset: " + mAssetName + " from URL: " + mAssetURL);

            DownloadTask downloader = new DownloadTask();

            downloader.mParams.mCallback = this;
            downloader.mParams.mOutPath  = assetfile;
            downloader.mParams.mContext  = mContext;
            downloader.mParams.mMessageId = HWVAsset.AssetMessage(mType);

            mDownloadState = DownloadState.ASSET;
            downloader.execute(mAssetURL + mAssetName + HWVAsset.AssetExtension(mType) );

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
            mClientCallback.onDownloadComplete(assetPath, HWVConstants.HWV_ERR_FILE_IO);

            return;

        }

        if (strUpdateFromURL.equalsIgnoreCase(mUpdateLocal ))
        {
            mClientCallback.onDownloadComplete(new File(assetfile), HWVConstants.HWV_SUCCESS);
            return;

        }

        // finally here we must download new asset file because cached is dated
        DownloadTask downloader = new DownloadTask();

        downloader.mParams.mCallback = this;
        downloader.mParams.mOutPath  = assetfile;
        downloader.mParams.mContext  = mContext;
        downloader.mParams.mMessageId = HWVAsset.AssetMessage(mType);

        mDownloadState = DownloadState.ASSET;
        downloader.execute(mAssetURL + mAssetName + HWVAsset.AssetExtension(mType) );

    }
}
