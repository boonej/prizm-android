package co.higheraltitude.prizm.handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import co.higheraltitude.prizm.adapters.GroupAdapter;
import co.higheraltitude.prizm.fragments.MessageGroupFragment;
import co.higheraltitude.prizm.models.Group;

/**
 * Created by boonej on 10/15/15.
 */
public class GroupsHandler extends Handler {

    private Context mContext;
    private MessageGroupFragment mFragment;

    public GroupsHandler(Context context,
                              MessageGroupFragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            if (msg.obj != null) {
                if (msg.obj instanceof ArrayList) {
                    ArrayList<Group> obj = (ArrayList<Group>)msg.obj;
                    ArrayList<Group> groups = mFragment.getGroups();
                    if (groups.size() != obj.size()) {
                        mFragment.setGroups(obj);
                        groups = obj;
                        ArrayList<String> groupList = new ArrayList<>();
                        Iterator i = groups.iterator();
                        while (i.hasNext()) {
                            Group g = (Group)i.next();
                            groupList.add(g.name);
                        }
                        mFragment.setGroupNames(groupList);
                        GroupAdapter adapter = mFragment.getAdapter();
                        int size = adapter.getCount();
                        try {
                            for (int j = 0; j != groups.size(); ++j) {
                                Group a = groups.get(j);
                                if (j < size - 2) {
                                    Group b = adapter.getItem(j + 2);
                                    if (!a.uniqueID.equals(b.uniqueID)) {
                                        adapter.remove(b);
                                        adapter.insert(a, j + 2);
                                    }
                                } else {
                                    adapter.add(a);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(mContext, "Uh oh! There was a problem loading your groups.", Toast.LENGTH_SHORT).show();
        }
        mFragment.hideProgressBar();
    }
}