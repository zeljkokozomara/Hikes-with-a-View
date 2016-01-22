package zeljkok.autumnsky.hikeswithaview;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zeljkok on 07/12/2015.
 */
public class Ratings
{
    private Map<String, String> mDifficulty  = new HashMap<String, String>();
    private Map<String, String> mTrailFactor = new HashMap<String, String>();
    private Map<String, String> mSnowFactor  = new HashMap<String, String>();

    public void loadFromXML (File difficultyFile, File trailFile, File snowFile) throws IOException
    {
        // open difficulties input stream
        InputStream stream = new FileInputStream(difficultyFile);

        // parse difficulties
        stream.close();

        // open trail ratings input stream
        stream = new FileInputStream(trailFile);

        // parse trail ratings
        stream.close();

        // open snow factor input stream
        stream = new FileInputStream(snowFile);

        // parse snow factor
        stream.close();


    }

    public String getDifficulty (String key)
    {
        String strResult = new String();
        return strResult;

    }


    public String getSnowFactor(String key)
    {
        String strResult = new String();
        return strResult;
    }

    public String getTrailFactor(String key)
    {
        String strResult = new String();
        return strResult;
    }
}
