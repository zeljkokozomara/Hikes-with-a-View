package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CatalogContent implements IHWVContent
{
    private static final String xml_ns = null;
    private Context m_context;

    public class CatalogSection
    {
        private ArrayList<CatalogArea> mAreas = new ArrayList<CatalogArea>();
        public ArrayList<CatalogArea>  getAreas(){return mAreas;}
    }

    public class CatalogArea
    {
        private ArrayList<TripPack> mTrips = new ArrayList<TripPack>();
        public ArrayList<TripPack>  getTrips(){return mTrips;}
    }

    private ArrayList<CatalogSection> mSections = new ArrayList<CatalogSection>();
    public  ArrayList<CatalogSection> getSections(){return mSections;}

    public CatalogContent(Context c){m_context = c;}

    public void loadFromXML (File catalogFile)  throws XmlPullParserException, IOException
    {
        InputStream in = new FileInputStream(catalogFile);

        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, CatalogContent.xml_ns,
                    m_context.getString(R.string.catalog_docelem));

            // read the region name and version
            // TODO: Make use of this info; right now I'm just reading it
            //       -- region name must match how we've been configured by the caller
            //       -- version is for future structural changes
            String region = parser.getAttributeValue(CatalogContent.xml_ns, m_context.getString(R.string.catalog_docelem_region));
            String version = parser.getAttributeValue(CatalogContent.xml_ns, m_context.getString(R.string.catalog_docelem_version));

            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

            }
        }
        finally
        {
            in.close();
        }
    }
}