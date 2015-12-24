package zeljkok.autumnsky.hikeswithaview;

/**
 * Created by zeljkok on 09/12/2015.
 */
public class HWVConstants
{
    public static final int     PERMISSIONS_REQUEST_FINE_LOCATION = 1;       // location request id

    public static final int     LOCATION_UPDATE_INTERVAL          = 10000;
    public static final int     LOCATION_UPDATE_INTERVAL_FASTEST  = 5000;

    // how many trips we persist locally on device
    public static final int     NUM_PERSISTED_TRIPS               = 5;

    // file names
    public static final String  CATALOG_FILE_NAME                 = "catalog.xml";
    public static final String  ASSETS_FILE_NAME                  = "assets.xml";
    public static final String  UPDATE_FILE_NAME                  = "update";
}
