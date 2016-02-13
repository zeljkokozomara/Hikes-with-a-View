package zeljkok.autumnsky.hikeswithaview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class HikerFront extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, IAssetStatus
{

    public static final String HIKER_FRONT_TAG = "HWV.HikerFront";


    private GoogleMap mMap = null;
    private GoogleApiClient mGoogleApiClient = null;

    private Location mLastLocation = null;
    private LocationRequest mLocationRequest = null;

    private int mLocationAware = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hiker_front);

        checkPermissions();

        getMap();
        getLocation();
    }


    @Override
    protected void onStop()
    {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void getLocation ()
    {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(HWVConstants.LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(HWVConstants.LOCATION_UPDATE_INTERVAL_FASTEST);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

    }
    protected void getMap ()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        showMyLocation();

        setupMap();

    }

    private void setupMap()
    {
        if (null == mMap) return;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);


        final HikerFront ctxt = this;
        LatLng stawamusbackside = new LatLng(49.695, -123.137);
        mMap.addMarker(new MarkerOptions().position(stawamusbackside).title("Stawamus Backside").icon(
                BitmapDescriptorFactory.fromResource(R.drawable.hiking)));

        LatLng harvey = new LatLng(49.475, -123.2014);
        mMap.addMarker(new MarkerOptions().position(harvey).title("Mt. Harvey").icon(
                BitmapDescriptorFactory.fromResource(R.drawable.hiking)));

        LatLng pos = new LatLng(49.56, -123.16);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, HWVConstants.REGION_MAP_ZOOM));

        final TripPack tp = new TripPack(this);
        HWVContext.getInstance().setCurrentTrip(tp);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
        {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                try
                {
                    if (marker.getTitle().equals("Stawamus Backside"))
                    {
                        tp.load("stawamus_backside", "Stawamus Backside",
                                "https://sites.google.com/site/hikeswithaview/bc-coast-mountains/sea-to-sky/stawamus-country/stawamus-backside", ctxt);
                    }
                    else if (marker.getTitle().equals("Mt. Harvey"))
                    {
                        tp.load("harvey", "Mt. Harvey",
                                "https://sites.google.com/site/hikeswithaview/bc-coast-mountains/sea-to-sky/lions-bay/harvey", ctxt);
                    }
                }
                catch (Exception ex)
                {
                    Log.e(HIKER_FRONT_TAG, "Exception thrown while trying to load boomlake trip. Cause: " + ex.getLocalizedMessage());
                }
            }
        });

    }

    // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint)
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,  mLocationRequest, this);
    }

    public void onConnectionSuspended(int cause)
    {
    }

    // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed (ConnectionResult result)
    {
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        showMyLocation();
    }


    protected void showMyLocation ()
    {
        if ( (null != mLastLocation) && ( (null != mMap) ))
        {
            LatLng pos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude() );
            mMap.addMarker(new MarkerOptions().position(pos).title("You are here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        }
    }



    // called at startup to determine if we can run location aware
    protected int checkPermissions ()
    {
        mLocationAware = 0;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    HWVConstants.PERMISSIONS_REQUEST_FINE_LOCATION);

        }

        return 1;  // ok
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case HWVConstants.PERMISSIONS_REQUEST_FINE_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    mLocationAware = 1;
                }
                else
                {
                    mLocationAware = 0;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // callback when asset has been loaded. If successful, we have data &
    // start new activity / asset viewer
    public void onAssetComplete (int status, String assetName, HWVAsset.AssetType type)
    {
        // what we want to do here is to launch activity that handles type of asset being retrieved
        if (status != HWVConstants.HWV_SUCCESS)
        {
            Toast.makeText(this, "Asset: " + assetName +
                    " retrieval complete. Status: " + Integer.toString(status), Toast.LENGTH_LONG).show();

            return;
        }
     /*   // debug test
        TripPack tp = HWVContext.getInstance().getCurrentTrip();
        TripPhotos mPhotos = tp.getPhotos ();
        String strPhotos = "Trip Pack retrieval complete! Photo list: ";
        List<TripPhotos.PhotoTupple> photolist = mPhotos.getPhotos();
        for (TripPhotos.PhotoTupple pt : photolist)
        {
            strPhotos += pt.mPhotoFile.getName();
            strPhotos += " ";
        }
        Toast.makeText(this, strPhotos, Toast.LENGTH_SHORT).show(); */

        Intent intent = new Intent(this, TripViewActivity.class);
        startActivity(intent);


    }

}
