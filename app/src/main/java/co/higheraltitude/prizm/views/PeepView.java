package co.higheraltitude.prizm.views;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.ReadMessagesActivity;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.User;

/**
 * TODO: document your custom view class.
 */
public class PeepView extends RelativeLayout {

    private ImageView avatarView;
    private TextView authorView;
    private TextView textView;
    private TextView likesView;
    private TextView timeAgoView;
    private ImageView likeButton;
    private ImageView imageView;
    private View viewedSection;
    private TextView viewedCount;
    private Peep peep;

    private Thread thread;
    private String instanceId;
    private int likeCount;

    private Boolean isLiked;

    private PeepViewListener mDelegate;


    private static Pattern userTag;
    private static Pattern hashTag;
    private PrizmDiskCache mCache;


    public static PeepView inflate(ViewGroup parent) {
        PeepView peepView = (PeepView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.peep_view, parent, false);
        return peepView;
    }

    public PeepView(Context context){
        this(context, null);
    }

    public PeepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PeepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.group_view_children, this, true);
    }

    public void setDelegate(PeepViewListener delegate) {
        mDelegate = delegate;
    }


    public void setPeep(Peep peep) {
        mCache = PrizmDiskCache.getInstance(getContext());
        if (userTag == null) {
            userTag = Pattern.compile("@\\([^)]+\\)");
        }

        if (hashTag == null) {
            hashTag = Pattern.compile("#[\\S]+");
        }
        this.peep = peep;
        PrizmCache cache = PrizmCache.getInstance();
        instanceId = UUID.randomUUID().toString();
        setViews();
        String text = peep.text;
        Matcher matcher = userTag.matcher(text);

        ArrayList<Object> matches = new ArrayList<>();
        while (matcher.find()) {
            String match = matcher.group();
            if (match != null) {
                String m = match.substring(2, match.length() - 1);
                final String[] split = m.split("\\|");
                matches.add(split);
                text = text.replace(match, "@" + split[0]);
            }
        }




        SpannableString spanText = new SpannableString(text);
        Iterator i = matches.iterator();
        while (i.hasNext()) {
            String [] m = (String[])i.next();
            int idx = text.indexOf("@" + m[0]);
            int len = m[0].length() + 1;
            final String id = m[1];
            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (mDelegate != null) {
                        mDelegate.tagClicked(PeepViewListener.TAG_TYPE_USER, id);
                    }
                }
            };
            spanText.setSpan(span, idx, idx + len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Matcher hashMatcher = hashTag.matcher(spanText);
        while (hashMatcher.find()) {
            final String match = hashMatcher.group();
            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (mDelegate != null) {
                        mDelegate.tagClicked(PeepViewListener.TAG_TYPE_HASH, match);
                    }
                }
            };
            int idx = spanText.toString().indexOf(match);
            spanText.setSpan(span, idx, idx + match.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spanText);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        likeCount = peep.likesCount;
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        imageView.setVisibility(INVISIBLE);
        authorView.setText(peep.creatorName);
        avatarView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.user_missing_avatar));



        likesView.setText(String.valueOf(likeCount));
        if (peep.liked) {
            likeButton.setImageDrawable(getResources().getDrawable(R.drawable.like_selected_icon));
        } else {
            likeButton.setImageDrawable(getResources().getDrawable(R.drawable.like_icon));
        }
        timeAgoView.setText(peep.timeAgo());
        if (likeCount == 0) {
            likesView.setVisibility(INVISIBLE);
        } else {
            likesView.setVisibility(VISIBLE);
        }
        View likesArea = findViewById(R.id.likes_area);
        likesArea.setOnClickListener(new mLikeButtonListener(this));
        ImageHandler ih = new ImageHandler(this, avatarView, this.instanceId, ImageHandler.PEEP_IMAGE_TYPE_AVATAR);
        mCache.fetchBitmap(peep.creatorProfilePhotoUrl, 125, ih);
        if (peep.imageUrl != null && !peep.imageUrl.isEmpty()) {
            imageView.setVisibility(VISIBLE);
            mCache.fetchBitmap(peep.imageUrl, imageView.getMaxWidth(), new ImageHandler(this, imageView, this.instanceId,
                    ImageHandler.PEEP_IMAGE_TYPE_IMAGE));
        }
        final Peep mPeep = peep;
        avatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                User u = new User();
                u.uniqueID = mPeep.creatorId;
                u.name = mPeep.creatorName;
                u.type = mPeep.creatorType;
                u.subtype = mPeep.creatorSubtype;
                u.profilePhotoURL = mPeep.creatorProfilePhotoUrl;
                mDelegate.avatarClicked(u);
            }
        });
        viewedSection.setVisibility(INVISIBLE);
        if (peep.myPeep) {
            if (User.getCurrentUser().role != null && (User.getCurrentUser().role.equals("leader") || User.getCurrentUser().role.equals("owner")
                    || User.getCurrentUser().role.equals("ambassador"))) {
                if (peep.readCount > 0) {
                    int count = 0;
                    if (User.getCurrentUser().role.equals("owner")) {
                        count = peep.readCount - 1;
                        viewedCount.setText(String.valueOf(count));
                        if (count > 0) {
                            viewedSection.setVisibility(VISIBLE);
                        }
                    } else {
                        count = peep.readCount - 2;
                        viewedCount.setText(String.valueOf(count));
                        if (count > 0) {
                            viewedSection.setVisibility(VISIBLE);
                        }
                    }
                } else {
                    viewedSection.setVisibility(INVISIBLE);
                }
            }
        }
        viewedSection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.viewedButtonClicked(PeepView.this);
                }
            }
        });
    }

    public Peep getPeep() {
        return peep;
    }



    private void setViews() {
        avatarView = (ImageView) findViewById(R.id.peep_avatar);
        authorView = (TextView)findViewById(R.id.peep_author);
        textView = (TextView)findViewById(R.id.peep_text);
        likesView = (TextView)findViewById(R.id.peep_like_count);
        timeAgoView = (TextView)findViewById(R.id.peep_date);
        likeButton = (ImageView)findViewById(R.id.peep_like_button);
        imageView = (ImageView)findViewById(R.id.peep_image);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageClicked(v);
            }
        });
        viewedSection = findViewById(R.id.peep_viewed_section);
        viewedCount = (TextView)findViewById(R.id.peep_viewed_count);

    }

    public void onImageClicked(View view){
        mDelegate.peepImageClicked(this);
    }



    private static class mLikeButtonListener implements OnClickListener {

        private PeepView mView;

        public mLikeButtonListener(PeepView peepView) {
            mView = peepView;
        }

        public void onClick(View view) {
            if (mView.mDelegate != null) {
                mView.mDelegate.likeButtonClicked(mView);
            }
        }
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private PeepView mPeepView;
        private ImageView mImageView;
        private int mType;
        public static int PEEP_IMAGE_TYPE_AVATAR = 0;
        public static int PEEP_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(PeepView view, ImageView iv, String id, int type) {
            mInstanceId = id;
            mPeepView = view;
            mImageView = iv;
            mType = type;
        }

        public void handleMessage(Message msg) {
            if (mPeepView.instanceId.equals(mInstanceId)) {
                Bitmap bmp = (Bitmap)msg.obj;
                if (mType == PEEP_IMAGE_TYPE_AVATAR) {
                    AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mPeepView.getResources());
                    Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                    mImageView.setImageDrawable(avatarDrawable);
                } else if (mType == PEEP_IMAGE_TYPE_IMAGE) {
                    mImageView.setImageBitmap(bmp);
                }
            }
        }
    }

    private static class LikeHandler extends Handler {
        private PeepView mPeepView;

        public LikeHandler(PeepView view, String id) {
            mPeepView = view;
        }

        public void handleMessage(Message msg) {
            Peep mPeep = (Peep)msg.obj;

        }
    }


    public interface PeepViewListener {
        int TAG_TYPE_USER = 1;
        int TAG_TYPE_HASH = 0;

        void avatarClicked(User creator);
        void tagClicked(int tagType, String tag);
        void peepImageClicked(PeepView peepView);
        void likeButtonClicked(PeepView peepView);
        void viewedButtonClicked(PeepView peepView);
    }

}
