package co.higheraltitude.prizm.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import co.higheraltitude.prizm.EditProfile;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.PartnerInfoFragment;
import co.higheraltitude.prizm.fragments.ProfileInfoFragment;
import co.higheraltitude.prizm.fragments.ProfileMainFragment;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/24/15.
 */
public class ProfileHeaderView extends RelativeLayout implements View.OnClickListener,
        ViewPager.OnPageChangeListener {

    private ProfileHeaderViewDelegate mDelegate;

    private View mEditProfileButton;
    private View mEditProfileArea;
    private View mPostsButton;
    private ImageView mCoverPhotoView = null;
    private ViewPager mViewPager = null;
    private View mBlackOverlay = null;
    private TextView mPostsCount;
    private TextView mFollowingCount;
    private TextView mFollowersCount;
    private ImageView mGridIcon;
    private ImageView mFullIcon;
    private FragmentManager mFragmentManager;
    private ImageView mTagsFilter;
    private ImageView mLocationFilter;
    private boolean mGridSelected = true;
    private boolean mTagsFilterSelected = false;
    private boolean mLocationFilterSelected = false;
    private View mFollowingButton;
    private TextView mFollowingText;
    private View mFollowersButton;
    private TextView mFollowersText;
    private View mFollowingArea;

    private View mFollowButton;
    private ImageView mFollowImage;
    private TextView mFollowText;

    private User mUser;

    public interface ProfileHeaderViewDelegate {
        void gridViewClicked();
        void fullViewClicked();
        void locationFilterClicked(boolean selected);
        void tagsFilterClicked(boolean selected);
        void editProfileClicked();
        void followingClicked();
        void followersClicked();
        void followUser();
        void unfollowUser();
        void postButtonClicked();
    }

    public static ProfileHeaderView inflate(ViewGroup parent) {
        ProfileHeaderView tagView = (ProfileHeaderView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_header_view, parent, false);
        return tagView;
    }

    public ProfileHeaderView(Context context){
        this(context, null);
    }

    public ProfileHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProfileHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setFollowingImage(Boolean selected){
        if (selected) {
            mFollowText.setText(getResources().getString(R.string.action_following));
            mFollowImage.setImageResource(R.drawable.following_icon);
        } else {
            mFollowText.setText(getResources().getString(R.string.action_follow));
            mFollowImage.setImageResource(R.drawable.follow_icon);
        }
    }

    public void setDelegate(ProfileHeaderViewDelegate delegate) {
        mDelegate = delegate;
    }

    private void setViews() {
        mFollowingArea = findViewById(R.id.follow_area);
        mCoverPhotoView = (ImageView)findViewById(R.id.profile_cover_photo);
        mBlackOverlay = findViewById(R.id.black_overlay);
        mPostsCount = (TextView)findViewById(R.id.posts_count);
        mFollowersCount = (TextView)findViewById(R.id.followers_count);
        mFollowingCount = (TextView)findViewById(R.id.following_count);
        mEditProfileArea = findViewById(R.id.edit_profile_area);
        mEditProfileButton = findViewById(R.id.edit_profile_button);
        mEditProfileButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.editProfileClicked();
                }
            }
        });

        mViewPager = (ViewPager)findViewById(R.id.profile_view_pager);
        mViewPager.setAdapter(new ProfilePager(mFragmentManager, getContext()));
        mViewPager.addOnPageChangeListener(this);
        mGridIcon = (ImageView)findViewById(R.id.grid_button);
        mFullIcon = (ImageView)findViewById(R.id.full_button);
        mGridIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGridSelected) {
                    mGridSelected = true;
                    mGridIcon.setImageResource(R.drawable.grid_view_selected);
                    mFullIcon.setImageResource(R.drawable.full_view);
                    if (mDelegate != null) {
                        mDelegate.gridViewClicked();
                    }
                }
            }
        });
        mFullIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGridSelected) {
                    mGridSelected = false;
                    mGridIcon.setImageResource(R.drawable.grid_view);
                    mFullIcon.setImageResource(R.drawable.full_view_selected);
                    if (mDelegate != null) {
                        mDelegate.fullViewClicked();
                    }
                }
            }
        });
        mTagsFilter = (ImageView)findViewById(R.id.tags_filter);
        mLocationFilter = (ImageView)findViewById(R.id.location_filter);
        mTagsFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagsFilterSelected = !mTagsFilterSelected;
                if (mTagsFilterSelected) {
                    mTagsFilter.setImageResource(R.drawable.usertag_icon_selected);
                } else {
                    mTagsFilter.setImageResource(R.drawable.usertag_icon);
                }
                if (mDelegate != null) {
                    mDelegate.tagsFilterClicked(mTagsFilterSelected);
                }
            }
        });
        mLocationFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationFilterSelected = !mLocationFilterSelected;
                if (mLocationFilterSelected) {
                    mLocationFilter.setImageResource(R.drawable.locationpin_icon_selected);
                } else {
                    mLocationFilter.setImageResource(R.drawable.locationpin_icon);
                }
                if (mDelegate != null) {
                    mDelegate.locationFilterClicked(mLocationFilterSelected);
                }
            }
        });
        mFollowingButton = findViewById(R.id.following_button);
        mFollowingText = (TextView)findViewById(R.id.following_text);
        mFollowingButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int brownGrey = Color.parseColor("#616161");
                    mFollowingText.setTextColor(brownGrey);
                    mFollowingCount.setTextColor(brownGrey);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN){
                    int coolBlue = Color.parseColor("#5188b9");
                    mFollowingText.setTextColor(coolBlue);
                    mFollowingCount.setTextColor(coolBlue);
                }
                return false;
            }
        });
        mFollowingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.followingClicked();
                }
            }
        });
        mFollowersButton = findViewById(R.id.followers_button);
        mFollowersText = (TextView)findViewById(R.id.followers_text);
        mFollowersButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int brownGrey = Color.parseColor("#616161");
                    mFollowersText.setTextColor(brownGrey);
                    mFollowersCount.setTextColor(brownGrey);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN){
                    int coolBlue = Color.parseColor("#5188b9");
                    mFollowersText.setTextColor(coolBlue);
                    mFollowersCount.setTextColor(coolBlue);
                }
                return false;
            }
        });
        mFollowersButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.followersClicked();
                }
            }
        });
        mFollowingButton = findViewById(R.id.follow_button);
        mFollowImage = (ImageView)findViewById(R.id.follow_image);
        mFollowText = (TextView)findViewById(R.id.follow_text);
        mFollowingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    if (mUser.isFollowing) {
                        mDelegate.unfollowUser();
                    } else {
                        mDelegate.followUser();
                    }
                }
                mUser.isFollowing = !mUser.isFollowing;
                setFollowingImage(mUser.isFollowing);
            }
        });
        mPostsButton = findViewById(R.id.posts_button);
        mPostsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.postButtonClicked();
                }
            }
        });


    }

    public void onClick(View view) {

    }



    public void setFragmentManager(FragmentManager manager) {
        mFragmentManager = manager;
    }

    public void setUser(User user) {
        setViews();
        mUser = user;
        ((ProfilePager)mViewPager.getAdapter()).setUser(mUser);
        if (mUser.uniqueID.equals(User.getCurrentUser().uniqueID)) {
            mEditProfileArea.setVisibility(View.VISIBLE);
            mFollowingArea.setVisibility(View.GONE);
        }
        PrizmDiskCache cache = PrizmDiskCache.getInstance(getContext());
        if (mUser.coverPhotoURL != null && !mUser.coverPhotoURL.isEmpty()) {

            cache.fetchBitmap(mUser.coverPhotoURL, mCoverPhotoView.getWidth(), new CoverGradientHandler(mCoverPhotoView,
                    (ImageView) findViewById(R.id.cover_photo_gradient)));

        }
        mPostsCount.setText(String.valueOf(mUser.postsCount));
        mFollowersCount.setText(String.valueOf(mUser.followersCount));
        mFollowingCount.setText(String.valueOf(mUser.followingCount));
        setFollowingImage(mUser.isFollowing);

    }



    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private ProfileHeaderView mPostView;
        private ImageView mImageView;

        public ImageHandler(ProfileHeaderView view, ImageView iv, String id) {
            mInstanceId = id;
            mPostView = view;
            mImageView = iv;
        }

        public void handleMessage(Message msg) {

                Bitmap bmp = (Bitmap)msg.obj;
                mImageView.setImageBitmap(bmp);

        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        private String field;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args) {
            Bitmap coverImage = null;
            try {
                coverImage = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
                field = args[1];

            } catch (Exception e) {
                e.printStackTrace();
            }
            return coverImage;
        }

        protected void onPostExecute(Bitmap image) {
            if(image != null){
                if (field == "cover") {
                    mCoverPhotoView.setImageBitmap(image);
                } else if (field == "avatar") {

                }
            }else{
                Log.d("DEBUG", "Image Does Not exist or Network Error");
            }
        }
    }

    public class ProfilePager extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;

        private Context context;

        public ProfilePager(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ProfileMainFragment();
            } else {
                if (mUser != null && mUser.type != null && mUser.type.equals("institution_verified")) {
                    return new PartnerInfoFragment();
                } else {
                    return new ProfileInfoFragment();
                }
            }
        }

        public void setUser(User user) {
            ((ProfileMainFragment)getItem(0)).setUser(user);
            if (user.type != null && user.type.equals("institution_verified")) {
                ((PartnerInfoFragment) getItem(1)).setUser(user);
            } else {
                ((ProfileInfoFragment) getItem(1)).setUser(user);
            }
            setFollowingImage(mUser.isFollowing);

        }
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            mBlackOverlay.setVisibility(View.INVISIBLE);
            Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            mBlackOverlay.startAnimation(fadeOut);
        } else {
            if (mBlackOverlay.getVisibility() == View.INVISIBLE) {
                mBlackOverlay.setVisibility(View.VISIBLE);
                mBlackOverlay.setAlpha(0);
                final OvershootInterpolator oi = new OvershootInterpolator();
                ViewCompat.animate(mBlackOverlay).alpha(1.f).withLayer().setDuration(600).setInterpolator(oi).start();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class CoverGradientHandler extends Handler {

        private ImageView mImageView;
        private  ImageView mGradientView;

        public CoverGradientHandler(ImageView imageView, ImageView gradientView) {
            mImageView = imageView;
            mGradientView = gradientView;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj != null) {
                mGradientView.setVisibility(View.VISIBLE);
                Bitmap bmp = (Bitmap)message.obj;
                mImageView.setImageBitmap(bmp);
            }
        }
    }


}
