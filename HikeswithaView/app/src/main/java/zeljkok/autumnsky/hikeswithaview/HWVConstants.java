package zeljkok.autumnsky.hikeswithaview;

/**
 * Created by zeljkok on 09/12/2015.
 */
public class HWVConstants
{
    public static final int     PERMISSIONS_REQUEST_FINE_LOCATION = 1;       // location request id

    public static final int     LOCATION_UPDATE_INTERVAL          = 10000;
    public static final int     LOCATION_UPDATE_INTERVAL_FASTEST  = 5000;

    // how many assets (of same type, i.e trips) we persist locally on device
    public static final int     NUM_PERSISTED_ASSETS               = 5;

    // file names
    public static final String  HWV_TRIP_SUFFIX                   = "Trips";
    public static final String  HWV_DIFFICULTY_SUFFIX             = "Difficulty";
    public static final String  HWV_SNOW_FACTOR_SUFFIX            = "Snow-Factor";
    public static final String  HWV_REGIONS_SUFFIX                = "Regions";
    public static final String  HWV_CATALOG_SUFFIX                = "Catalog";  // keep flat for now

    public static final String  UPDATE_FILE_NAME                  = "update";

    public static final String  HWV_FILE_EXTENSION                = ".hwv";
    public static final String  XML_FILE_EXTENSION                = ".xml";

    // status codes
    public static final int     HWV_SUCCESS                       =  0;    // generic success

    public static final int     HWV_ERR_DOWNLOAD                  = -1;    // generic download error
    public static final int     HWV_ERR_CANCEL                    = -2;    // operation canceled by user
    public static final int     HWV_ERR_FILE_IO                   = -3;    // local file IO error



}
