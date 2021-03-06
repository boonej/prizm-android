package co.higheraltitude.prizm.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.SearchResultsFragment;
import co.higheraltitude.prizm.network.PrizmAPIService;
import retrofit.http.GET;

/**
 * Created by boonej on 11/11/15.
 */
public class Post implements Parcelable{

    private static final String POST_HOME_FEED_FORMAT = "/users/%s/home";
    private static final String POST_LIKE_FORMAT = "/posts/%s/likes";
    private static final String POST_UNLIKE_FORMAT = "/posts/%s/likes/%s";
    private static final String POST_SINGLE_FORMAT = "/posts/%s?requestor=%s";
    private static final String POST_PROFILE_FORMAT = "/users/%s/posts?requestor=%s";
    private static final String HASH_TAG_REQUEST_FORMAT = "/hashtags?filter=%s&format=tags_only&limit=5";
    private static final String HASH_TAG_COUNTS_FORMAT = "/hashtags/?filter=%s&format=counts&limit=15&skip=%s";
    private static final String EXPLORE_FORMAT = "/posts?requestor=%s&limit=21";
    private static final String BARE_POSTS_FORMAT = "/posts/%s";

    public static final String CATEGORY_ASPIRATION = "aspiration";
    public static final String CATEGORY_PASSION = "passion";
    public static final String CATEGORY_INSPIRATION = "inspiration";
    public static final String CATEGORY_EXPERIENCE = "experience";
    public static final String CATEGORY_ACHIEVEMENT = "achievement";
    public static final String CATEGORY_PRIVATE = "personal";

    public static final String EXPLORE_TYPE_FEATURED = "featured";
    public static final String EXPLORE_TYPE_POPULAR = "popular";

    public static final String VISIBILITY_PRIVATE = "private";
    public static final String VISIBILITY_PUBLIC = "public";
    public static final String VISIBILITY_TRUST = "trust";

    public String uniqueId;
    public String category;
    public String externalProvider;
    public String hashTags;
    public int commentsCount;
    public int likesCount;
    public String filePath;
    public double locationLatitude;
    public double locationLongitude;
    public String createDate;
    public String text;
    public String timeSince;
    public String creatorId;
    public String creatorType;
    public String creatorSubtype;
    public String creatorProfilePhotoUrl;
    public boolean isLiked;
    public String creatorName;
    public boolean ownPost;
    public String scope;

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("_id", "uniqueId");
            put("create_date", "createDate");
            put("text", "text");
            put("category", "category");
            put("external_provider", "externalProvider");
            put("hash_tags", "hashTags");
            put("comments_count", "commentsCount");
            put("likes_count", "likesCount");
            put("file_path", "filePath");
            put("location_latitude", "locationLatitude");
            put("location_longitude", "locationLongitude");
            put("time_since", "timeSince");
            put("creator_id", "creatorId");
            put("creator_type", "creatorType");
            put("creator_subtype", "creatorSubtype");
            put("creator_profile_photo_url", "creatorProfilePhotoUrl");
            put("creator_name", "creatorName");
            put("is_liked", "isLiked");
            put("own_post", "ownPost");
            put("scope", "scope");
        }};
        return map;
    }

    public Post(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Post.class;
        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Field field;
            if (object.has((String)pair.getKey())) {
                try {
                    field = c.getDeclaredField((String) pair.getValue());
                    field.setAccessible(true);
                    Object value = object.get((String) pair.getKey());
                    if (value instanceof JSONArray) {
                        ArrayList<String> valueList = new ArrayList<>();
                        JSONArray array = (JSONArray)value;
                        for (int i = 0; i != array.length(); ++i) {
                            String v = array.getString(i);
                            valueList.add(v);
                        }
                        field.set(this, valueList);
                    } else {
                        if (value != JSONObject.NULL)
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public Post(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Post.class;
        if (in != null) {
            Bundle bundle = in.readBundle();
            while (iterator.hasNext()) {
                String key = (String)iterator.next();
                try {
                    Field field = c.getField(key);
                    Object value = null;
                    if (field.getType() == boolean.class) {
                        value = bundle.getBoolean(key);
                        field.set(this, value);
                    } else if (field.getType() == String.class) {
                        value = bundle.getString(key);
                        field.set(this, value);
                    } else if (field.getType() == int.class) {
                        value = bundle.getInt(key);
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();

        Bundle bundle = new Bundle();
        Iterator iterator = properties.iterator();
        Class<?> c = Post.class;
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            try {
                Field field = c.getField(key);
                Object value = field.get(this);
                if (value != null) {
                    if (value.getClass().equals(Boolean.class)) {
                        bundle.putBoolean(key, (boolean) value);
                    } else if (value instanceof String) {
                        bundle.putString(key, (String) value);
                    } else if (value.getClass().equals(Integer.class)) {
                        bundle.putInt(key, (int)value);
                    } else if (value.getClass().equals(Double.class)) {
                        bundle.putDouble(key, (double)value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Post createFromParcel(Parcel in) {
                    return new Post(in);
                }

                public Post[] newArray(int size) {
                    return new Post[size];
                }
            };


    private static ArrayList<Post> processPostList(Object obj)
    {
        ArrayList<Post> results = new ArrayList<>();
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray)obj;
            int length = array.length();
            for (int i = 0; i != length; ++i) {
                try {
                    Post post = new Post(array.getJSONObject(i));
                    results.add(post);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return results;
    }

    public static void fetchHomeFeed(String lastDate, final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(POST_HOME_FEED_FORMAT, User.getCurrentUser().uniqueID);
        if (lastDate != null && !lastDate.isEmpty()) {
            path = path + "?last=" + lastDate;
        }
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new PostListDelegate(delegate));

    }

    public static void fetchProfileFeed(String user, String before, String after,
                                        final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(POST_PROFILE_FORMAT, user, User.getCurrentUser().uniqueID);
        if (before != null) {
            path = path + "&before=" + before;
        }
        if (after != null) {
            path = path + "&after=" + after;
        }
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new PostListDelegate(delegate));
    }

    public static void fetchPost(String postId, final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(POST_SINGLE_FORMAT, postId, User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new SinglePostDelegate(delegate));
    }

    public static void likePost(Post post, final Handler handler) {
        String path = String.format(POST_LIKE_FORMAT, post.uniqueId);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user", User.getCurrentUser().uniqueID);
        PrizmAPIService.getInstance().performAuthorizedRequest(path, data, HttpMethod.PUT,
                new SinglePostHandler(handler), true);
    }

    public static void unlikePost(Post post, final Handler handler) {
        String path = String.format(POST_UNLIKE_FORMAT, post.uniqueId, User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, data, HttpMethod.DELETE,
                new SinglePostHandler(handler), true);
    }

    public static void getLikes(Post post, final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(POST_LIKE_FORMAT, post.uniqueId);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, data, HttpMethod.GET, new User.UserListDelegate(delegate));
    }

    public static void createPost(MultiValueMap<String, String> parameters, final Handler handler) {
        String path = "/posts";
        PrizmAPIService.getInstance().performAuthorizedRequest(path, parameters, HttpMethod.POST,
                handler, true);
    }

    public static void searchHashtags(String searchText,
                                      final PrizmDiskCache.CacheRequestDelegate delegate) {

        searchHashtags(searchText, false, 0, delegate);
    }

    public static void searchHashtags(String searchText, Boolean counts, int skip,
                                      final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = counts?String.format(HASH_TAG_COUNTS_FORMAT, searchText,
                String.valueOf(skip)):
                String.format(HASH_TAG_REQUEST_FORMAT, searchText);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, data, HttpMethod.GET, new PrizmTagDelegate(counts, delegate));
    }

    public static void updatePost(String id, MultiValueMap<String, String> parameters,
                                  Handler handler) {
        String path = String.format(BARE_POSTS_FORMAT, id);
        PrizmAPIService.getInstance().performAuthorizedRequest(path, parameters, HttpMethod.PUT,
                handler, true);
    }

    public static void deletePost(String id,
                                  Handler handler) {
        String path = String.format(BARE_POSTS_FORMAT, id);
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, parameters, HttpMethod.PUT,
                handler, true);
    }

    public static void getExplore(@Nullable String mode, @Nullable String before,
                                  @Nullable String after, @Nullable String tag,
                                  final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(EXPLORE_FORMAT, User.getCurrentUser().uniqueID);
        if (mode != null) {
            path = String.format("%s&mode=%s", path, mode);
        }
        if (before != null) {
            path = String.format("%s&before=%s", path, before);
        }
        if (after != null) {
            path = String.format("%s&after=%s", path, after);
        }
        if (tag != null) {
            path = String.format("%s&hashtag=%s", path, tag);
        }


        PrizmDiskCache.getInstance(null).performCachedRequest(path,
                new LinkedMultiValueMap<String, String>(), HttpMethod.GET,
                new PostListDelegate(delegate));
    }

    private static class PrizmTagDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;
        private Boolean mCounts;

        public PrizmTagDelegate(boolean counts, PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
            mCounts = counts;
        }

        @Override
        public void cached(String path, Object data) {
            if (mCounts) {
                mDelegate.cached(path, processCounts(data));
            } else {
                mDelegate.cached(path, process(data));
            }
        }

        @Override
        public void cacheUpdated(String path, Object data) {
            if (mCounts) {
                mDelegate.cacheUpdated(path, processCounts(data));
            } else {
                mDelegate.cacheUpdated(path, process(data));
            }
        }

        private ArrayList<String> process(Object object) {
            ArrayList<String> data = new ArrayList<>();
            if (object instanceof JSONArray) {
                JSONArray arr = (JSONArray)object;
                for (int i = 0; i != arr.length(); ++i) {
                    try {
                        String tag = arr.getString(i);
                        data.add(tag);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return data;
        }

        private ArrayList<Map<String, String>> processCounts(Object object) {
            ArrayList<Map<String, String>> data = new ArrayList<>();
            if (object instanceof JSONArray) {
                JSONArray arr = (JSONArray)object;
                for (int i = 0; i != arr.length(); ++i) {
                    try {
                        JSONObject tag = arr.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<>();
                        map.put("tag", "#" + tag.getString("tag"));
                        map.put("count", String.valueOf(tag.getInt("count")));
                        data.add(map);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return data;
        }
    }

    private static class SinglePostHandler extends Handler {

        private Handler mHandler;

        public SinglePostHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj instanceof JSONObject) {
                Post p = new Post((JSONObject)message.obj);
                Message m = mHandler.obtainMessage(1, p);
                mHandler.sendMessage(m);
            }
        }
    }

    private static class SinglePostDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public SinglePostDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object)
        {
            mDelegate.cached(path, process(object));
        }

        @Override
        public void cacheUpdated(String path, Object object)
        {
            mDelegate.cacheUpdated(path, process(object));
        }

        private Post process(Object object) {
            Post p = null;
            if (object instanceof JSONObject) {
                p = new Post((JSONObject)object);
            }
            return p;
        }
    }

    private static class PostListDelegate implements PrizmDiskCache.CacheRequestDelegate
    {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public PostListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object)
        {
           mDelegate.cached(path, processPostList(object));
        }

        @Override
        public void cacheUpdated(String path, Object object)
        {
            mDelegate.cacheUpdated(path, processPostList(object));
        }
    }
}
