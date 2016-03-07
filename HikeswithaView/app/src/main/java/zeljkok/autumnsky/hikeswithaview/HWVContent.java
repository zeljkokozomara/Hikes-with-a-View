package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

/**
 * Created by zeljkok on 20/02/2016.
 */
public abstract class HWVContent implements IAssetDownload
{
    // standard tag for logging
    public static final String HWV_CONTENT_TAG = "HWV.HWVContent";

    protected Context      m_context = null;

    private HWVAsset     mContentAsset   = null;   // handles all the network & local file system stuff
    private IAssetStatus mClientCallback = null;   // client callback invoked when content is ready

    private String       mContentName    = null;
    private HWVAsset.AssetType mType     = null;


    public  HWVContent(Context c){m_context = c;}

    // public export to load particular content
    public void load(String contentName, String strAssetURL, IAssetStatus callback, HWVAsset.AssetType assetType)
    {
        mClientCallback = callback;
        mContentName    = new String(contentName);

        mContentAsset = new HWVAsset(m_context, contentName, assetType);
        mType = assetType;

        // ensure we have asset folder
        try
        {
            mContentAsset.handleFolderCreation();
        }
        catch (Exception ex)
        {
            Log.e(HWV_CONTENT_TAG, "Exception thrown while accessing asset folder. Cause: " + ex.getLocalizedMessage() +
               " Asset Type: " + HWVAsset.AssetType.toString(assetType));

            mClientCallback.onAssetComplete(HWVConstants.HWV_ERR_FILE_IO, mContentName, assetType);
        }

        // fetch hwv file; we will be notified async on success. We
        // report back to the client only when all downloading/unzipping/xml parsing has completed
        File contentFile = mContentAsset.handleAssetDownload(strAssetURL, false, this);
        if (null != contentFile)  // was available synchronously
            onDownloadComplete (contentFile, HWVConstants.HWV_SUCCESS);
    }

    public void onDownloadComplete (File contentFile, int status)
    {
        // if failure, just propagate to client. If successful,
        // we have to unzip then parse all the XMLs
        if (status != HWVConstants.HWV_SUCCESS)
        {
            mClientCallback.onAssetComplete(status, mContentName, mType);
            return;
        }

        if (contentFile.exists() == false)
        {
            if (!false) throw new AssertionError(contentFile);
        }

        // now parse xml assets. At base level we don't know how to do it
        try
        {
            parseAssets(contentFile);
        }
        catch (Exception ex)
        {
            mClientCallback.onAssetComplete(HWVConstants.HWV_ERR_XML, mContentName, mType);
            Log.e(HWV_CONTENT_TAG, "Exception thrown while parsing assets for content [" + mContentName + "]. Cause:" +
                    ex.getLocalizedMessage() + " Asset Type: " + HWVAsset.AssetType.toString(mType) );

            return;
        }

        // we are done -- trip is loaded!
        mClientCallback.onAssetComplete(HWVConstants.HWV_SUCCESS, mContentName, mType);
    }

    // each concrete content knows how to parse its asset file
    abstract void parseAssets(File contentFile) throws XmlPullParserException, IOException;
}
