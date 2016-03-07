package zeljkok.autumnsky.hikeswithaview;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class TripViewActivity extends AppCompatActivity
{

    public static final int NUM_TABS = 4;    // number of tabs in trip view activity

    // ordinal of tabs
    public static final int TAB_SUMMARY_INDEX  = 0;
    public static final int TAB_GPS_INDEX      = 1;
    public static final int TAB_PHOTOS_INDEX   = 2;
    public static final int TAB_NOTES_INDEX    = 3;

    public static final String TRIP_VIEW_ACTIVITY_TAG = "TripViewActivity";

    protected class TripTabsListener implements ViewPager.OnPageChangeListener
    {
        public void 	onPageScrollStateChanged(int state){}
        public void 	onPageScrolled(int position, float positionOffset, int positionOffsetPixels){}
        public void 	onPageSelected(int position){showFab(position);}

        private void showFab(int position)
        {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            switch (position)
            {
                case TripViewActivity.TAB_SUMMARY_INDEX:
                case TripViewActivity.TAB_PHOTOS_INDEX:
                case TripViewActivity.TAB_NOTES_INDEX:
                    fab.setVisibility(View.GONE);
                    break;

                case TripViewActivity.TAB_GPS_INDEX:
                    fab.setVisibility(View.VISIBLE);
                    break;
            }

        }

    }


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager() );

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // setup initial visibility and change listener of "fab" floating button that will be used for
        // wireless sharing of GPS track (and maybe some other data)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        mViewPager.addOnPageChangeListener(new TripTabsListener());

  /*

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        TripPack tp = HWVContext.getInstance().getCurrentTrip();
        this.setTitle(tp.getTripCaption() );

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }

    public static class TripGpsFragment extends SupportMapFragment
    {
        private GoogleMap mapView;
        public TripGpsFragment()
        {
        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState)
        {
            mapView = getMap();
            mapView.setMapType(GoogleMap.MAP_TYPE_SATELLITE);  //todo: FROM user preferences

            // put waypoint markers on the map
            TripGps gps = HWVContext.getInstance().getCurrentTrip().getGps();
            final List<TripGps.GpsWaypoint> wps = gps.getWaypoints();
            final List<TripGps.GpsTrekPoint> tpts = gps.getTrekPoints();

            for (int i = 0; i < wps.size(); i++)
            {
                TripGps.GpsWaypoint wp = wps.get(i);
                mapView.addMarker(new MarkerOptions()
                        .position(wp.tpt.gps)
                        .title(wp.title)
                        .snippet(wp.caption)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hiking)));  //TODO: Replace with icon based on gpx

            }

            mapView.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
            {
                @Override
                public View getInfoWindow(Marker arg0)
                {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker)
                {
                    Context context = getActivity();

                    LinearLayout info = new LinearLayout(context);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(context);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(context);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });

            // now add all the trekpoints to the line
            PolylineOptions line = new PolylineOptions();
            for (int i = 0; i < tpts.size(); i++)
            {
                TripGps.GpsTrekPoint tpt = tpts.get(i);
                line.add(tpt.gps);
            }

            // finally draw the line which should look exactly like in Garmin!
            line.geodesic(true);
            line.width(8.0f);
            line.color(Color.YELLOW);

            mapView.addPolyline(line);

            // reposition the map
            TripGps.GpsLayout layout = gps.getGpsLayout();
            LatLng start = HWVUtilities.gps_from_string(layout.LAT, layout.LONG);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(start )      // Sets the center of the map to STM Hut
                    .zoom(Float.parseFloat(layout.zoom))          // Sets the zoom
                    .bearing(Float.parseFloat(layout.bearing))     // Sets the orientation of the camera to east
                    .tilt(Float.parseFloat(layout.tilt))          // Sets the tilt of the camera
                    .build();            // Creates a CameraPosition from the builder

            mapView.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            super.onActivityCreated(savedInstanceState);
        }

        public static TripGpsFragment newInstance()
        {
            TripGpsFragment fragment = new TripGpsFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
          //  View view= inflater.inflate(R.layout.fragment_trip_gps, container, false);
            View view = super.onCreateView(inflater, container, savedInstanceState);

            return view;

        }

        @Override
        public void onInflate(Activity arg0, AttributeSet arg1, Bundle arg2)
        {
            super.onInflate(arg0, arg1, arg2);
        }

    }
    public static class TripSummaryFragment extends Fragment
    {
        private TextView mSummary;
        private TextView mPhotoCorner;
        private TextView mGoingFurther;

        public TripSummaryFragment()
        {
        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState)
        {
            TripNotes notes = HWVContext.getInstance().getCurrentTrip().getNotes();

            mSummary.setText(notes.mSummary);
            mPhotoCorner.setText(notes.mPhotoCorner);
            mGoingFurther.setText(notes.mGoingFurther);

            super.onActivityCreated(savedInstanceState);
        }

        public static TripSummaryFragment newInstance()
        {
            TripSummaryFragment fragment = new TripSummaryFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_trip_notes, container, false);
            mSummary           = (TextView)rootView.findViewById(R.id.tripfragmentsummary_description);
            mPhotoCorner       = (TextView)rootView.findViewById(R.id.tripfragmentphotocorner_description);
            mGoingFurther      = (TextView)rootView.findViewById(R.id.tripfragmentgoingfurther_description);

            // set labels bold
            TextView label = (TextView)rootView.findViewById(R.id.tripfragmentsummary_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmentphotocorner_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmentgoingfurther_label);
            label.setTypeface(null, Typeface.BOLD);


            return rootView;
        }

    }

    public static class TripPhotoFragment extends Fragment
    {
        private ViewFlipper mFlipper = null;
        private final GestureDetector detector = new GestureDetector(new SwipeGestureDetector());

        public TripPhotoFragment()
        {
        }

        class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener
        {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {
                try
                {
                    // right to left swipe
                    if ( (e1.getX() - e2.getX() > HWVConstants.FLIPPER_SWIPE_MIN_DISTANCE) && (Math.abs(velocityX) > HWVConstants.FLIPPER_SWIPE_THRESHOLD_VELOCITY) )
                    {
                        mFlipper.setInAnimation(AnimationUtils.loadAnimation(
                                getActivity(), R.anim.left_in));
                        mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.left_out));
                        mFlipper.showNext();
                        return true;
                    }
                    else if ((e2.getX() - e1.getX() > HWVConstants.FLIPPER_SWIPE_MIN_DISTANCE) && (Math.abs(velocityX) > HWVConstants.FLIPPER_SWIPE_THRESHOLD_VELOCITY) )
                    {
                        mFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.right_in));
                        mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.right_out));
                        mFlipper.showPrevious();

                        return true;
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return false;
            }
        }
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TripPhotoFragment newInstance()
        {
            TripPhotoFragment fragment = new TripPhotoFragment();
            return fragment;
        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState)
        {
            // if we are created, trip is guaranteed to have photos
            // construct trip photo view for each image and add to flipper
            TripViewActivity act = (TripViewActivity) getActivity();
            LayoutInflater inflater = act.getLayoutInflater();

            TripPhotos photos = HWVContext.getInstance().getCurrentTrip().getPhotos();
            List<TripPhotos.PhotoTupple> photolist = photos.getPhotos();
            for (TripPhotos.PhotoTupple pt : photolist)
            {
                View imageView    = inflater.inflate(R.layout.trip_photo_view, null);
                TextView caption  = (TextView)imageView.findViewById(R.id.flippercaption);
                ImageView image   = (ImageView)imageView.findViewById(R.id.flipperphoto);

                caption.setText(pt.mPhotoCaption);
                Drawable d = Drawable.createFromPath(pt.mPhotoFile.getAbsolutePath());
                image.setImageDrawable(d);
                // image.setRotation(270);

                //  getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mFlipper.addView(imageView);
            }

            View startflip = act.findViewById(R.id.flipperplay);
            startflip.setOnClickListener(new View.OnClickListener()
                                         {
                                             @Override
                                             public void onClick(View view)
                                             {
                                                 // sets auto flipping
                                                 mFlipper.showNext();
                                            /*     mFlipper.setAutoStart(true);
                                                 mFlipper.setFlipInterval(HWVConstants.FLIPPER_SLIDESHOW_INTERVAL);
                                                 mFlipper.startFlipping();*/
                                             }
                                         }
            );

            View stopflip = act.findViewById(R.id.flipperstop);
            stopflip.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                //mFlipper.stopFlipping();
                                                mFlipper.showPrevious();
                                            }
                                        }
            );

            mFlipper.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(final View view, final MotionEvent event)
                {
                    detector.onTouchEvent(event);
                    return true;
                }
            });

         /*   mFlipper.setAutoStart(true);
            mFlipper.setFlipInterval(HWVConstants.FLIPPER_SLIDESHOW_INTERVAL);
            mFlipper.startFlipping();   //TODO: manage via user preferences */


            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_photo_slideshow, container, false);
            mFlipper = (ViewFlipper)rootView.findViewById(R.id.tripflipper);


            return rootView;
        }

    }
    /**
     * Trip Abstract Fragment
     */
    public static class TripAbstractFragment extends Fragment
    {
        private TextView mDifficultyCode;
        private TextView mDifficultyDescription;

        private TextView mSnowFactorCode;
        private TextView mSnowFactorDescription;

        private TextView mSceneryDescription;

        private TextView mTrailCode;
        private TextView mTrailDescription;

        private TextView mTimeCode;
        private TextView mTimeDescription;

        private TextView mGearDescription;
        private TextView mWaterDescription;

        private TextView mDogCode;
        private TextView mDogDescription;

        private TextView mRoundTrip;
        private TextView mElevation;


        public TripAbstractFragment()
        {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TripAbstractFragment newInstance()
        {
            TripAbstractFragment fragment = new TripAbstractFragment();
            return fragment;
        }

        @Override
        public void onActivityCreated (Bundle savedInstanceState)
        {
            final TripNotes tn = HWVContext.getInstance().getCurrentTrip().getNotes();

            // Difficulty
            SpannableString content = new SpannableString(tn.mDifficulty.mCode);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            mDifficultyCode.setText(content);
            mDifficultyCode.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // launch popup that displays description for this code
                    Toast.makeText(getContext(), tn.mDifficulty.mDescription, Toast.LENGTH_LONG).show();
                }
            });

            mDifficultyDescription.setText(tn.mDifficulty.mDescription);

            // snow factor
            content = new SpannableString(tn.mSnowFactor.mCode);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            mSnowFactorCode.setText(content);
            mSnowFactorCode.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // launch popup that displays description for this code
                    Toast.makeText(getContext(), tn.mSnowFactor.mDescription, Toast.LENGTH_LONG).show();
                }
            });


            mSnowFactorDescription.setText(tn.mSnowFactor.mDescription);


            // trail/marking
            content = new SpannableString(tn.mTrail.mCode);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            mTrailCode.setText(content);
            mTrailCode.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // launch popup that displays description for this code
                    Toast.makeText(getContext(), tn.mTrail.mDescription, Toast.LENGTH_LONG).show();
                }
            });


            mTrailDescription.setText(tn.mTrail.mDescription);

            // round trip
            mRoundTrip.setText(tn.mMetrics.mDistance);

            // elevation
            String strHtml = "<b><i>" + getString(R.string.trip_abstract_elevation_start) + "</b></i> " + tn.mMetrics.mElevationStart + "  ";
            strHtml += "<b><i>" + getString(R.string.trip_abstract_elevation_max) + "</b></i> " + tn.mMetrics.mElevationMax + "<br>";
            strHtml += "<b><i>" + getString(R.string.trip_abstract_elevation_total) + "</b></i> " + tn.mMetrics.mElevationTotal;
            mElevation.setText(Html.fromHtml(strHtml) );

            // Suggested Time
            mTimeCode.setText(tn.mTime.mCode);
            mTimeDescription.setText(tn.mTime.mDescription);


            // scenery
            mSceneryDescription.setText(tn.mScenery);

            // gear
            mGearDescription.setText(tn.mGear);

            // water
            mWaterDescription.setText(tn.mWater);

            // dog friendly
            mDogCode.setText(tn.mDog.mCode);
            mDogDescription.setText(tn.mDog.mDescription);


            // and we are done!
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_trip_view, container, false);

            mDifficultyCode        = (TextView)rootView.findViewById(R.id.tripfragmentdifficulty_code);
            mDifficultyDescription = (TextView)rootView.findViewById(R.id.tripfragmentdifficulty_description);

            mSnowFactorCode        = (TextView)rootView.findViewById(R.id.tripfragmentsnowfactor_code);
            mSnowFactorDescription = (TextView)rootView.findViewById(R.id.tripfragmentsnowfactor_description);

            mSceneryDescription    = (TextView)rootView.findViewById(R.id.tripfragmentscenery_description);
            mGearDescription       = (TextView)rootView.findViewById(R.id.tripfragmentgear_description);
            mWaterDescription      = (TextView)rootView.findViewById(R.id.tripfragmentwater_description);

            mTrailCode             = (TextView)rootView.findViewById(R.id.tripfragmenttrail_code);
            mTrailDescription      = (TextView)rootView.findViewById(R.id.tripfragmenttrail_description);

            mTimeCode              = (TextView)rootView.findViewById(R.id.tripfragmenttime_code);
            mTimeDescription       = (TextView)rootView.findViewById(R.id.tripfragmenttime_description);

            mDogCode               = (TextView)rootView.findViewById(R.id.tripfragmentdog_code);
            mDogDescription        = (TextView)rootView.findViewById(R.id.tripfragmentdog_description);

            mRoundTrip             = (TextView)rootView.findViewById(R.id.tripfragmentroundtrip_value);
            mElevation             = (TextView)rootView.findViewById(R.id.tripfragmentelevation_value);

            TextView label = (TextView)rootView.findViewById(R.id.tripfragmentdifficulty_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmentsnowfactor_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmenttrail_label);
            label.setTypeface(null, Typeface.BOLD);

            // ============================================================
            label = (TextView)rootView.findViewById(R.id.tripfragmentelevation_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmentroundtrip_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmenttime_label);
            label.setTypeface(null, Typeface.BOLD);

            // =============================================================
            label = (TextView)rootView.findViewById(R.id.tripfragmentscenery_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmentgear_label);
            label.setTypeface(null, Typeface.BOLD);

            label = (TextView)rootView.findViewById(R.id.tripfragmentwater_label);
            label.setTypeface(null, Typeface.BOLD);

            // ==============================================================
            label = (TextView)rootView.findViewById(R.id.tripfragmentdog_label);
            label.setTypeface(null, Typeface.BOLD);


            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }


        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            Fragment frag = null;

            switch (position)
            {
                case TripViewActivity.TAB_SUMMARY_INDEX:
                    frag = TripAbstractFragment.newInstance();
                    break;

                case TripViewActivity.TAB_GPS_INDEX:
                    frag = TripGpsFragment.newInstance();
                    break;

                case TripViewActivity.TAB_PHOTOS_INDEX:
                    frag = TripPhotoFragment.newInstance();
                    break;

                case TripViewActivity.TAB_NOTES_INDEX:
                    frag = TripSummaryFragment.newInstance();
                    break;

                default:
                    throw new AssertionError(position);
            }


            return frag;
        }

        @Override
        public int getCount()
        {
            return TripViewActivity.NUM_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            String strResult = null;
            switch (position)
            {
                case TripViewActivity.TAB_SUMMARY_INDEX:
                    strResult = new String(getString(R.string.trip_view_summary_label) );
                    break;

                case TripViewActivity.TAB_NOTES_INDEX:
                    strResult = new String(getString(R.string.trip_view_notes_label) ) ;
                    break;

                case TripViewActivity.TAB_GPS_INDEX:
                    strResult = new String(getString(R.string.trip_view_gps_label) ) ;
                    break;

                case TripViewActivity.TAB_PHOTOS_INDEX:
                    strResult = new String(getString(R.string.trip_view_photos_label) ) ;
                    break;

                default:
                    throw new AssertionError(position);
            }

            return strResult;
        }
    }
}
