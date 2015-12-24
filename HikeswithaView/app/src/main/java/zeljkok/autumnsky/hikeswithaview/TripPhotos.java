package zeljkok.autumnsky.hikeswithaview;

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
    public class PhotoTupple
    {
        public String mPhotoFile;
        public String mPhotoDescription;
    }

    protected List<PhotoTupple> mPhotoList = new ArrayList<PhotoTupple>();   // Extracted list of photo-description pairs from asset file
    public List<PhotoTupple> getPhotos(){return mPhotoList;}

    public void loadFromXML (String strPath)  throws IOException
    {
        // open xml input stream
        InputStream stream = new FileInputStream(strPath);

        // parse photo list

        // close stream
        stream.close();
    }
}
