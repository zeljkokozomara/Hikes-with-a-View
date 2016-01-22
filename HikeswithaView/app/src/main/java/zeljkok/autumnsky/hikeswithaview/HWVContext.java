package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;

/**
 * Created by zeljkok on 16/01/2016.
 *
 * This class is the Singleton to hold various bits of data (such as parsed trip)
 * across activities, since Google Intent/Bundle mechanism is way to clumsy
 */
public class HWVContext
{
    private HWVContext (){;}

    private static HWVContext mInstance = null;
    private TripPack mTripPack = null;

    public  static HWVContext getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new HWVContext();
        }
        return mInstance;
    }

    public void      setCurrentTrip(TripPack tp){mTripPack = tp;}
    public TripPack  getCurrentTrip(){return mTripPack;}
}
