package zeljkok.autumnsky.hikeswithaview;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

/**
 * Created by zeljkok on 22/12/2015.
 */
public interface IHWVContent
{
    public void loadFromXML(File contentData) throws XmlPullParserException, IOException;
}

