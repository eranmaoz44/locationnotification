    package ssdl.technion.ac.il.locationnotification;


import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

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


public class MainFragment extends Fragment {
    private RecyclerView notificationList;
    private ViewAdapter adapter;
    List<Reminder> list;
    int lastPosChange;
    ScaleInAnimationAdapter animateAdater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        notificationList = (RecyclerView) layout.findViewById(R.id.notification_list);
        list = getList();
        lastPosChange=-1;
        adapter = new ViewAdapter(getActivity(), list);
        AlphaInAnimationAdapter alphaAdapter=new AlphaInAnimationAdapter(adapter);
        animateAdater=new ScaleInAnimationAdapter(alphaAdapter);
        alphaAdapter.setDuration(500);
        notificationList.setAdapter(animateAdater);
        //notificationList.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransition();
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).attachList(notificationList);
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
        SQLUtils sqlUtils=new SQLUtils(getActivity().getApplicationContext());
        return sqlUtils.getReminderList();
    }


    public class ViewAdapter extends RecyclerView.Adapter<InfoViewHolder> {
        private final LayoutInflater inflater;
        private List<Reminder> list = Collections.emptyList();


        public ViewAdapter(Context context, List<Reminder> list) {
            inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public InfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = inflater.inflate(R.layout.info, viewGroup, false);

            InfoViewHolder holder = new InfoViewHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(InfoViewHolder viewHolder, int i) {
            Reminder curr = list.get(i);
            viewHolder.textView.setText(curr.getTitle());
            File image = new File(curr.getImgPath());

            //TODO: delete 'if' after adding support to default image
            if(image.exists()) {

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

                viewHolder.imageView.setImageBitmap(bitmap);
            } else {
                viewHolder.imageView.setImageResource(R.drawable.image_3);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void updateItem(int i,Reminder reminder){
            list.set(i,reminder);
        }
        public void setList(List<Reminder> list){
            this.list=list;
        }


    }

    public class InfoViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener,RecyclerView.OnLongClickListener {
        TextView textView;
        ImageView imageView;

        public InfoViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            textView = (TextView) itemView.findViewById(R.id.info_text);
            imageView = (ImageView) itemView.findViewById(R.id.info_image);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
            int pos = this.getPosition();
            Reminder r=list.get(pos);
            intent.putExtra(Constants.REMINDER_TAG,r);

	    lastPosChange=pos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP&& getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                startTransition(intent);
            }else{
                getActivity().startActivity(intent);
            }
        }

        private void startTransition(Intent intent) {
            View statusBar = getActivity().findViewById(android.R.id.statusBarBackground);
            View navigationBar = getActivity().findViewById(android.R.id.navigationBarBackground);
            View toolbar = getActivity().findViewById(R.id.tool_bar);

            List<Pair<View, String>> pairs = new ArrayList<>();
            pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
            pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
            //  pairs.add(Pair.create(toolbar, "tran1"));
            pairs.add(Pair.create((View) imageView, "tran2"));
            pairs.add(Pair.create((View) textView, "tran3"));
            // pairs.add(Pair.create(v.findViewById(R.id.card_info), "tran1"));
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                    pairs.toArray(new Pair[pairs.size()]));
            // Pair.create(v.findViewById(R.id.imageView), "tran2"));
//            ActivityOptionsCompat options = ActivityOptionsCompat.
//                    makeSceneTransitionAnimation(getActivity(),
//                            Pair.create(v.findViewById(R.id.card_info), "tran1"),
//                            Pair.create(v.findViewById(R.id.imageView), "tran2"));
            getActivity().startActivity(intent, options.toBundle());

//            // Create new fragment to add (Fragment B)
//            Fragment fragment = new UserDetailsFragment();
//            fragment.setSharedElementEnterTransition(new ChangeTransform());
//            fragment.setEnterTransition(new ChangeTransform());
//
//            // Our shared element (in Fragment A)
//            View mProductImage   = v.findViewById(R.id.card_info);
//
//            // Add Fragment B
//            FragmentTransaction ft = getFragmentManager().beginTransaction()
//                    .replace(R.id.rl_user_details, fragment)
//                    .addToBackStack("transaction")
//                    .addSharedElement(mProductImage, "tran1");
//            ft.commit();
        }

        @Override
        public boolean onLongClick(View v) {
            Log.v("onLongClick","long clicked recycler view");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            int pos = this.getPosition();
            final Reminder r=list.get(pos);
            builder.setTitle(r.getTitle()+":");
            builder.setItems(new CharSequence[]{getActivity().getString(R.string.delete_reminder)},
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    SQLUtils sqlUtils = new SQLUtils(getActivity());
                                    sqlUtils.deleteData(r.getId());
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

    private void updateRecyclerView() {
       Log.v("ChangeFragments","UpdateRecycler ViEW");
        ((MainActivity)getActivity()).updateFragment();
        list=getList();
        adapter.setList(list);
        if(lastPosChange==-1) {
            adapter.notifyDataSetChanged();
            animateAdater.notifyDataSetChanged();
        }else{
            adapter.notifyItemChanged(lastPosChange);
            animateAdater.notifyItemChanged(lastPosChange);
        }
    }
}
