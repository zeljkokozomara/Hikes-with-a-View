package zeljkok.autumnsky.hikeswithaview;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

public class TripViewActivity extends AppCompatActivity
{

    public static final int NUM_TABS = 3;    // number of tabs in trip view activity

    // ordinal of tabs
    public static final int TAB_NOTES_INDEX  = 0;
    public static final int TAB_GPS_INDEX    = 1;
    public static final int TAB_PHOTOS_INDEX = 2;

    public static final String TRIP_VIEW_ACTIVITY_TAG = "TripViewActivity";

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



  /*      FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
        this.setTitle(tp.getTripCaption());


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
                                                 //sets auto flipping
                                                 mFlipper.setAutoStart(true);
                                                 mFlipper.setFlipInterval(HWVConstants.FLIPPER_SLIDESHOW_INTERVAL);
                                                 mFlipper.startFlipping();
                                             }
                                         }
            );

            View stopflip = act.findViewById(R.id.flipperstop);
            stopflip.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                mFlipper.stopFlipping();
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
     * A placeholder fragment generated by wizard; eventually replace with real code
     */
    public static class PlaceholderFragment extends Fragment
    {
        public PlaceholderFragment()
        {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance()
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_trip_view, container, false);
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
                case TripViewActivity.TAB_NOTES_INDEX:
                case TripViewActivity.TAB_GPS_INDEX:
                    frag = PlaceholderFragment.newInstance();
                    break;


                case TripViewActivity.TAB_PHOTOS_INDEX:
                    frag = TripPhotoFragment.newInstance();
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
