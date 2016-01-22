package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeljkok on 22/12/2015.
 */
public class TripPhotos implements ITripData
{
    // standard tag for logging
    public static final String TRIP_PHOTOS_TAG = "HWV.TripPhotos";

    // photos.xml doesn't use namespaces
    private static final String xml_ns = null;

    private Context m_context = null;

    public class PhotoTupple
    {
        public File   mPhotoFile;
        public String mPhotoCaption;
    }

    public TripPhotos (Context c){m_context = c;}
    protected List<PhotoTupple> mPhotoList = new ArrayList<PhotoTupple>();   // Extracted list of photo-description pairs from asset file
    public List<PhotoTupple> getPhotos(){return mPhotoList;}

    public void loadFromXML (File photoFile)  throws XmlPullParserException, IOException
    {
        InputStream in = new FileInputStream(photoFile);
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, TripPhotos.xml_ns, m_context.getString(R.string.photos_docelem));
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                String name = parser.getName();

                if (name.equals( m_context.getString(R.string.photos_image) ))
                   readImage(parser, photoFile.getParent());
            }
        }
        finally
        {
            in.close();
        }
    }

    private void readImage (XmlPullParser parser, String parentPath)  throws XmlPullParserException, IOException
    {
        PhotoTupple pt = new PhotoTupple();
        parser.require(XmlPullParser.START_TAG, TripPhotos.xml_ns,
                m_context.getString(R.string.photos_image));         // redundant, following stack overflow

        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            if (name.equals(m_context.getString(R.string.photos_file)))
            {
                pt.mPhotoFile = new File(parentPath + File.separator + HWVUtilities.readText(parser));
                if (pt.mPhotoFile.exists() == false)
                    if (!false) throw new AssertionError(pt.mPhotoFile);
            }

            else if (name.equals(m_context.getString(R.string.photos_caption)))
                pt.mPhotoCaption = HWVUtilities.readText(parser);

            else
                HWVUtilities.skipXml(parser);
        }

        mPhotoList.add(pt);
    }
}
