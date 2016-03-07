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

/**
 * Created by zeljkok on 20/02/2016.
 *
 * This class abstracts Trip Catalog for given region
 * Class does not care about the region (Rockies, Coast, etc.) -- it is simply
 * given URL by higher layers from where it loads itself
 */
public class HWVCatalog extends HWVContent
{
    // standard tag for logging
    public static final String HWV_CATALOG_TAG = "HWV.HWVCatalog";

    // assets.xml doesn't use namespaces
    private static final String xml_ns = null;

    public HWVCatalog(Context c) {super(c);}

    CatalogContent mContent = new CatalogContent(m_context);

    // each concrete content knows how to parse its asset file
    void parseAssets(File contentFile) throws XmlPullParserException, IOException
    {
        String path = contentFile.getParent();

        InputStream in = new FileInputStream(contentFile);
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, HWVCatalog.xml_ns, m_context.getString(R.string.assets_docelem));
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                    continue;

                String name = parser.getName();

                if (name.equals( m_context.getString(R.string.ct_catalog) ))
                {
                    mContent.loadFromXML(new File(path + File.separator +
                            parser.getAttributeValue(HWVCatalog.xml_ns, m_context.getString(R.string.ct_attribute_file) )) );

                    // mNotes.dump(mTripName);
                }

                else
                    HWVUtilities.skipXml (parser);

                parser.nextTag ();
            }

        }
        finally
        {
            in.close();
        }
    }

}
