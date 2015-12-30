package zeljkok.autumnsky.hikeswithaview;

import java.io.File;

/**
 * Created by zeljkok on 28/12/2015.
 */
public interface IAssetDownload
{
    public void onDownloadComplete     (File assetPath,   int status);
}

