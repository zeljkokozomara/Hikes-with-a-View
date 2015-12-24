package com.zeljkok.helloworld;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;
import java.util.List;

public class HikerFront extends FragmentActivity implements OnMapReadyCallback
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hiker_front);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        testMyLocation(map);
        testPolyline(map);
        testPolygon(map);

    }


    protected void testPolygon(GoogleMap map)
    {
        LatLng kerr = new LatLng(51.509, -116.599);
        LatLng takakkaw = new LatLng(51.5022, -116.4876);
        LatLng laughing = new LatLng(51.53156, -116.5075);
        LatLng hut = new LatLng(51.52568, -116.56395);
        LatLng kiwetinok = new LatLng(51.51665, -116.59898);

        LatLng hole = new LatLng(51.51, -116.49);
        List points = Arrays.asList(hole);

        PolygonOptions area = new PolygonOptions()
                .add(takakkaw, laughing, hut, kiwetinok, kerr)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(128, 255, 0, 0) )

                .geodesic(true)
               // .addHole(points)
                ;

        Polygon polygon = map.addPolygon(area);




    }
    protected void testPolyline(GoogleMap map)
    {

        LatLng kerr = new LatLng(51.509, -116.599);
        map.addMarker(new MarkerOptions().position(kerr).title("Mountain Kerr")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hiker))
        );

        LatLng takakkaw = new LatLng(51.5022, -116.4876);
        map.addMarker(new MarkerOptions().position(takakkaw).title("Takakkaw Falls")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hiker))
        );

        LatLng laughing = new LatLng(51.53156, -116.5075);
        map.addMarker(new MarkerOptions().position(laughing).title("Laughing Falls")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hiker))
        );

        LatLng hut = new LatLng(51.52568, -116.56395);
        map.addMarker(new MarkerOptions().position(hut).title("Stanley Mitchell Hut")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hiker))
        );

        LatLng kiwetinok = new LatLng(51.51665, -116.59898);
        map.addMarker(new MarkerOptions()
                        .position(kiwetinok)
                        .title("Kiwetinok Pass")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hiker))
                        .snippet("13.5km, 2450m (800m), 3hrs")
        );


        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Draw hike line
        map.addPolyline(new PolylineOptions().geodesic(true)
                .add(new LatLng(51.5022, -116.4876))    // Takakkaw Parking
                .add(new LatLng(51.53156, -116.5075))   // Laughing Falls
                .add(new LatLng(51.52568, -116.56395))  // Stanley Mitchell Hut
                .add(new LatLng(51.51665, -116.59898))  // Kiwetinok Pass
                .add(new LatLng(51.509, -116.599))      // Mt. Kerr
        );

        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(hut, 12.0f));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(51.52568, -116.56395) )      // Sets the center of the map to STM Hut
                .zoom(13)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(45)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }
    protected void testMyLocation (GoogleMap map)
    {
        map.setMyLocationEnabled(true);
        Location loc = null;

        try
        {
            loc = map.getMyLocation();
        }
        catch (IllegalStateException ex)
        {
            Toast.makeText(getApplicationContext(), "Exception getting my Location: " + ex.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (null == loc)
        {
            Toast.makeText(getApplicationContext(), "No location data available",
                    Toast.LENGTH_LONG).show();
            return;
        }

        LatLng mycoord = new LatLng(loc.getLatitude(), loc.getLongitude());
        map.addMarker(new MarkerOptions()
                        .position(mycoord)
                        .title("My Position")
        );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

}
