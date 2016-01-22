package zeljkok.autumnsky.hikeswithaview;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by zeljkok on 23/12/2015.
 *
 * Set of generic helper functions
 */
public class HWVUtilities
{

    private static final String TAG_HWV_UTILITIES = "HWV.HWVUtilities";

    // reads file into string buffer
    public static String readFile( String file ) throws IOException
    {
        BufferedReader reader = new BufferedReader( new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        while( ( line = reader.readLine() ) != null )
        {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        return stringBuilder.toString();
    }

    // this helper unzips hwv file and returns assets.xml
    public static File unpackAssets (File hwvFile) throws IOException
    {
        File assetFile = null;
        File destFolder = new File(hwvFile.getParent() );

        // delete anything left from before except update
        List<File> delList = new ArrayList<File>();
        for (File f : destFolder.listFiles())
        {
            if ( (f.getName().equals(hwvFile.getName() ) == false) &&
                    (f.getName().equals(HWVConstants.UPDATE_FILE_NAME) == false)  )
                delList.add(f);
        }

        ListIterator<File> it = delList.listIterator();
        while (it.hasNext() )
            it.next().delete();


        TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(hwvFile.getAbsolutePath())));
        TarEntry entry = tis.getNextEntry();

        while (entry != null)
        {
            String entrypath = destFolder + File.separator + entry.getName();
            FileOutputStream fos = new FileOutputStream(entrypath);

            BufferedOutputStream dest = new BufferedOutputStream(fos);

            byte data[] = new byte[2048];
            int  count = tis.read(data);

            while (count != -1)
            {
                dest.write(data, 0, count);
                count = tis.read(data);
            }

            dest.flush();
            dest.close();

            if (entry.getName().equals (HWVConstants.ASSETS_FILE_NAME + HWVConstants.XML_FILE_EXTENSION) == true)
            {
                if (assetFile != null) assert false : assetFile;
                assetFile = new File(entrypath);
            }

            //  Log.d(TRIP_PACK_TAG, entry.getName() );
            entry = tis.getNextEntry();
        }

        tis.close();
        hwvFile.delete();

        return assetFile;
    }


    public static String readText(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT)
        {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    public static void skipXml (XmlPullParser parser) throws XmlPullParserException, IOException
    {
        if (parser.getEventType() != XmlPullParser.START_TAG)
            throw new IllegalStateException();

        int depth = 1;
        while (depth != 0)
        {
            switch (parser.next())
            {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;

                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
