package zeljkok.autumnsky.hikeswithaview;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zeljkok on 24/12/2015.
 */

    /* Subclass AsnycTask for support of file download with progress bar */
public class DownloadTask extends AsyncTask<String /* Download URL */, Integer /* Progress - percentage */, Integer /* Result */>
{
    private static final String TAG_DOWNLOAD_TASK = "HWV.DownloadTask";

    public class DownloadParams
    {
        public IAssetDownload mCallback;
        public String         mOutPath;
        public int            mMessageId;
        public Context        mContext;
    }

    DownloadParams            mParams = new DownloadParams();    // client must configure. This is just to bypass Java hyper
                                                                // anal iner class syntax bs
    ProgressDialog  mProgressDialog;

    @Override
    protected Integer doInBackground(String...urls)
    {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try
        {
            URL url = new URL(urls[0]);    // TODO: Revisit if we ever want to use multiple downloads with same call
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                Log.e(DownloadTask.TAG_DOWNLOAD_TASK, "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());

                mParams.mCallback.onDownloadComplete(null, HWVConstants.HWV_ERR_DOWNLOAD);
                return HWVConstants.HWV_ERR_DOWNLOAD;
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(mParams.mOutPath);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1)
            {
                // allow canceling with back button
                if (isCancelled())
                {
                    input.close();

                    mParams.mCallback.onDownloadComplete(null, HWVConstants.HWV_ERR_CANCEL);
                    return HWVConstants.HWV_ERR_CANCEL;
                }

                total += count;

                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));

                output.write(data, 0, count);
            }
        }

        catch (Exception e)
        {
            mProgressDialog.dismiss();

            Log.e(DownloadTask.TAG_DOWNLOAD_TASK, "Exception thrown during file download. " + e.getLocalizedMessage());

            mParams.mCallback.onDownloadComplete(null, HWVConstants.HWV_ERR_DOWNLOAD);
            return HWVConstants.HWV_ERR_DOWNLOAD;
        }

        finally
        {
            try
            {
                if (output != null)
                    output.close();

                if (input != null)
                    input.close();

            }
            catch (IOException ignored)
            {
            }

            if (connection != null)
                connection.disconnect();
        }

        //mParams.mCallback.onDownloadComplete(new File(mParams.mOutPath), HWVConstants.HWV_SUCCESS);
        return HWVConstants.HWV_SUCCESS;
    }


    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        mProgressDialog = new ProgressDialog(mParams.mContext);
        mProgressDialog.setMessage(mParams.mContext.getString(mParams.mMessageId));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        mProgressDialog.dismiss();
        if (result != HWVConstants.HWV_SUCCESS)
            Toast.makeText(mParams.mContext, "Download error: " + result, Toast.LENGTH_LONG).show();
        else
            mParams.mCallback.onDownloadComplete(new File(mParams.mOutPath), HWVConstants.HWV_SUCCESS);


    }
}
