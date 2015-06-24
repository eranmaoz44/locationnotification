package ssdl.technion.ac.il.locationnotification;


import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;
import ssdl.technion.ac.il.locationnotification.Constants.Constants;
import ssdl.technion.ac.il.locationnotification.utilities.Reminder;
import ssdl.technion.ac.il.locationnotification.utilities.SQLUtils;
import ssdl.technion.ac.il.locationnotification.utils_ui.HidingScrollListener;


public class MainFragment extends Fragment {
    private RecyclerView notificationList;
    private ViewAdapter adapter;
    List<Reminder> list;
    int lastPosChange;
    View view;
    ScaleInAnimationAdapter animateAdater;
    OnDataPass dataPasser;
    //        private UserDetailsFragment userDetailsFragment;
//        private SelectReminderFragment selectReminderFragment;
        private Reminder currReminder;
        private Fragment userDetailsFragment;
        private ViewSwitcher viewSwitcher;

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        userDetailsFragment = new UserDetailsFragment();
//        selectReminderFragment= new SelectReminderFragment();
        currReminder = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        Toast.makeText(getActivity(),"is tablet horizantal "+getResources().getBoolean(R.bool.is_tablet_landscape),Toast.LENGTH_SHORT).show();
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        notificationList = (RecyclerView) layout.findViewById(R.id.notification_list);
        list = getList();
        lastPosChange = -1;
        adapter = new ViewAdapter(getActivity(), list);
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        animateAdater = new ScaleInAnimationAdapter(alphaAdapter);
        alphaAdapter.setDuration(500);
        notificationList.setAdapter(animateAdater);
        //notificationList.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransition();
        }
        view=layout;
        viewSwitcher=(ViewSwitcher)view.findViewById(R.id.view_switcher);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        notificationList.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                ((MainActivity) getActivity()).hideViews();
            }

            @Override
            public void onShow() {
                ((MainActivity) getActivity()).showViews();
            }
        });
        //((MainActivity)getActivity()).attachList(notificationList);
    }

    private void setTransition() {
        Transition transition = new ChangeTransform();
        transition.setDuration(1000);
        //transition.setInterpolator(new BounceInterpolator());
//        transition.excludeTarget(android.R.id.statusBarBackground, true);
//        transition.excludeTarget(android.R.id.navigationBarBackground, true);
        getActivity().getWindow().setExitTransition(null);
        getActivity().getWindow().setReenterTransition(null);
        //getActivity().getWindow().setAllowReturnTransitionOverlap(true);
        // getActivity().getWindow().setAllowEnterTransitionOverlap(true);
        //getActivity().getWindow().setTransitionBackgroundFadeDuration(1000);
        getActivity().getWindow().setEnterTransition(null);
        getActivity().getWindow().setSharedElementEnterTransition(transition);
        //getActivity().getWindow().setSharedElementsUseOverlay(false);
        getActivity().getWindow().setSharedElementExitTransition(null);
        getActivity().getWindow().setSharedElementReenterTransition(null);
        getActivity().getWindow().setSharedElementReturnTransition(transition);
    }

    private List<Reminder> getList() {
        SQLUtils sqlUtils = new SQLUtils(getActivity().getApplicationContext());
        return sqlUtils.getReminderList();
    }


    public class ViewAdapter extends RecyclerView.Adapter<InfoViewHolder> {
        private final LayoutInflater inflater;
        private List<Reminder> list = Collections.emptyList();
        private static final int TYPE_HEADER = 2;
        private static final int TYPE_ITEM = 1;


        public ViewAdapter(Context context, List<Reminder> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public InfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view;
            if (viewType == TYPE_ITEM) {
                view = inflater.inflate(R.layout.info, viewGroup, false);
            } else {
                view = inflater.inflate(R.layout.info_header, viewGroup, false);
            }
            InfoViewHolder holder = new InfoViewHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(InfoViewHolder viewHolder, int i) {
            Reminder curr = list.get(i);
            viewHolder.textView.setText(curr.getTitle());
            File image = new File(curr.getImgPath());
            viewHolder.setReminder(curr);

            //TODO: delete 'if' after adding support to default image
            if (image.exists()) {

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

                viewHolder.imageView.setImageBitmap(bitmap);
            } else {
                viewHolder.imageView.setImageResource(R.drawable.image_3);
            }
            viewHolder.onOff.setChecked(curr.getOnOff());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void updateItem(int i, Reminder reminder) {
            list.set(i, reminder);
        }

        public void setList(List<Reminder> list) {
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position)) {
                return TYPE_HEADER;
            }
            return TYPE_ITEM;
        }

        //added a method to check if given position is a header
        private boolean isPositionHeader(int position) {
            return position == 0 || position == 1;
        }


    }

    public class InfoViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnLongClickListener {

        TextView textView;
        ImageView imageView;
        Switch onOff;
        ImageView ivShare;
        Reminder reminder;
        View view;

        public InfoViewHolder(View itemView) {
            super(itemView);
//            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            view =itemView;
            textView = (TextView) itemView.findViewById(R.id.info_text);
            imageView = (ImageView) itemView.findViewById(R.id.info_image);
            onOff = (Switch) itemView.findViewById(R.id.s_on_off);
            ivShare = (ImageView) itemView.findViewById(R.id.iv_share_icon);

            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog shareDialog = new Dialog(getActivity());
                    shareDialog.setContentView(R.layout.popup_share);
                    shareDialog.setTitle("Share to:");
                    shareDialog.show();
                    GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                            shareDialog.findViewById(R.id.pb_share_wait).setVisibility(View.GONE);
                            try {
                                ((ListView) shareDialog.findViewById(R.id.lv_share_friends)).setAdapter(new ShareListAdapter(getActivity().getApplicationContext(), jsonArray, reminder, shareDialog));
                            } catch (FacebookException e) {
                                shareDialog.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("Please connect to facebook first!");
                                builder.setCancelable(true);
                                builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.connectToFacebook(getActivity());
                                    }
                                });
                                builder.create().show();
                            }
                        }
                    });
                    request.executeAsync();
                }
            });
            final InfoViewHolder temp = this;
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
                    int pos = temp.getPosition();
                    Reminder r = list.get(pos);
                    intent.putExtra(Constants.REMINDER_TAG, r);


                    lastPosChange = pos;
                    if (getResources().getBoolean(R.bool.is_tablet_landscape)) {
                        Log.v("MainFragmentReminder", "onclick tablet landscape prevId=" + (null != currReminder ? currReminder.getId() : "null") + " new id = " + r.getId());
//                        if(null==currReminder||!(currReminder.getId().equals(r.getId()))) {
//                            Bundle bundle = new Bundle();
//                            bundle.putParcelable(Constants.REMINDER_TAG, r);
//                            userDetailsFragment= new UserDetailsFragment();
//                            userDetailsFragment.setArguments(bundle);
//                        )
                       // view.setSelected(true);

                        Log.v("fuck", "mudda fucka is in user main activity");
                        dataPasser.onReminderPass(r);
//                            getFragmentManager().beginTransaction().replace(R.id.details_container, userDetailsFragment).commit();
//                        }
                    } else if (isPortrate()) {
                        startTransition(intent, r);
                    } else {
                        getActivity().startActivity(intent);
                    }
                    currReminder = r;
                }
            });

            onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    reminder.setOnOff(isChecked);
                    SQLUtils sqlUtils = new SQLUtils(getActivity());
                    sqlUtils.updateData(reminder);
                }
            });
            imageView.setOnLongClickListener(this);
        }

        private boolean isPortrate(){
            return getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        }

        public void setReminder(Reminder reminder) {
            this.reminder = reminder;
        }

        private void startTransition(Intent intent, Reminder r) {
            transitionFromActivityToActivity(intent);

            // Create new fragment to add (Fragment B)
//            Fragment fragment = new UserDetailsFragment();
//            fragment.setSharedElementEnterTransition(new ChangeTransform());
//            fragment.setEnterTransition(new ChangeTransform());
//
//            // Our shared element (in Fragment A)
//            View mProductImage   = imageView;
//
//            // Add Fragment B
//            final Bundle bundle = new Bundle();
//
//            bundle.putParcelable(Constants.REMINDER_TAG,r);
//            fragment.setArguments(bundle);
//            FragmentTransaction ft = getFragmentManager().beginTransaction()
//                    .replace(R.id.container, fragment)
////                    .addToBackStack("transaction")
////                    .addSharedElement(mProductImage, "tran1")
//            ft.commit();
        }

        private void transitionFromActivityToActivity(Intent intent) {
            View statusBar = getActivity().findViewById(android.R.id.statusBarBackground);
            View navigationBar = getActivity().findViewById(android.R.id.navigationBarBackground);
//            View toolbar = getActivity().findViewById(R.id.tool_bar);

            List<Pair<View, String>> pairs = new ArrayList<>();
            Pair<View, String> p1 = Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
            Pair<View, String> p2 = Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
            Pair<View, String> p3 = Pair.create((View) imageView, "tran2");
            Pair<View, String> p4 = Pair.create((View) textView, "tran3");
            pairs.add(p1);
            pairs.add(p2);
            //  pairs.add(Pair.create(toolbar, "tran1"));
            pairs.add(p3);
            pairs.add(p4);
            // pairs.add(Pair.create(v.findViewById(R.id.card_info), "tran1"));
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), p1, p2, p3, p4);
            // Pair.create(v.findViewById(R.id.imageView), "tran2"));
//            ActivityOptionsCompat options = ActivityOptionsCompat.
//                    makeSceneTransitionAnimation(getActivity(),
//                            Pair.create(v.findViewById(R.id.card_info), "tran1"),
//                            Pair.create(v.findViewById(R.id.imageView), "tran2"));
            getActivity().startActivity(intent, options.toBundle());
        }

        @Override
        public boolean onLongClick(View v) {
            Log.v("onLongClick", "long clicked recycler view");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            int pos = this.getPosition();
            final Reminder r = list.get(pos);

            builder.setTitle(r.getTitle() + ":");
            builder.setItems(new CharSequence[]{getActivity().getString(R.string.delete_reminder)},
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    SQLUtils sqlUtils = new SQLUtils(getActivity());
                                    sqlUtils.deleteData(r.getId());
//                                    if(null!=currReminder&&currReminder.getId().equals(r.getId())){
                                    currReminder = null;
                                    dataPasser.onReminderPass(null);
//                                        getFragmentManager().beginTransaction().replace(R.id.details_container, selectReminderFragment).commit();
//                                    }
                                    updateRecyclerView();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
            builder.show();
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        ((MainActivity)getActivity()).updateFragment();
//        list=getList();
//        adapter.setList(list);
//        if(lastPosChange==-1) {
//            adapter.notifyDataSetChanged();
//            animateAdater.notifyDataSetChanged();
//        }else{
//            adapter.notifyItemChanged(lastPosChange);
//            animateAdater.notifyItemChanged(lastPosChange);
//        }
        updateRecyclerView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().postponeEnterTransition();
            final View decor = getActivity().getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    public void updateRecyclerView() {
       Log.v("ChangeFragments","UpdateRecycler ViEW");
//        ((MainActivity)getActivity()).updateFragment();
        list=getList();
        if(list.size()==0){
            viewSwitcher.setDisplayedChild(0);
        } else {
            viewSwitcher.setDisplayedChild(1);
        }
        adapter.setList(list);
        if (lastPosChange == -1) {
            adapter.notifyDataSetChanged();
            animateAdater.notifyDataSetChanged();
        } else {
            adapter.notifyItemChanged(lastPosChange);
            animateAdater.notifyItemChanged(lastPosChange);
        }
    }

    public interface OnDataPass {
        public void onReminderPass(Reminder r);
    }


    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        dataPasser = (OnDataPass) a;
    }

}
