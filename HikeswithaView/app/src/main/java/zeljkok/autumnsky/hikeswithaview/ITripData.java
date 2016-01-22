package zeljkok.autumnsky.hikeswithaview;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

/**
 * Created by zeljkok on 22/12/2015.
 */
public interface ITripData
{
    public void loadFromXML(File tripData) throws XmlPullParserException, IOException;
}
