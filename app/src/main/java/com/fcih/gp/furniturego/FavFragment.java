package com.fcih.gp.furniturego;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;


/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class FavFragment extends Fragment {

    private static final String OBJECT_KEY = "KEY";
    private ProgressBar mProgressView;
    private FirebaseRecyclerAdapter<FireBaseHelper.Favorites, viewholder> mAdapter = null;
    private RecyclerView recyclerView;
    private View mView;
    private BaseActivity activity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavFragment() {
    }

    public static FavFragment newInstance() {
        FavFragment fragment = new FavFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        // Set the adapter
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final Context context = view.getContext();
        activity.findViewById(R.id.tabs).setVisibility(View.GONE);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Query query = FireBaseHelper.Favorites.Ref.orderByChild(FireBaseHelper.Favorites.Table.User_id.text).equalTo(mAuth.getCurrentUser().getUid());
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading WhishList");
        progressDialog.show();
        new FireBaseHelper.Favorites().Where(FireBaseHelper.Favorites.Table.User_id, mAuth.getCurrentUser().getUid(), Data -> {
            if (Data.size() == 0) {
                progressDialog.dismiss();
                TextView textView = (TextView) view.findViewById(R.id.emptycategory);
                textView.setText("Your Wish List Is Empty");
                textView.setVisibility(View.VISIBLE);
                //showProgress(false);
            } else {
                mAdapter = new FirebaseRecyclerAdapter<FireBaseHelper.Favorites, viewholder>(
                        FireBaseHelper.Favorites.class, R.layout.fragment_item, viewholder.class, query) {
                    @Override
                    protected void populateViewHolder(viewholder viewHolder, FireBaseHelper.Favorites model, int position) {
                        new FireBaseHelper.Objects().Findbykey(model.object_id, Data -> {
                            viewHolder.mTitleView.setText(Data.name);
                            viewHolder.mCompanyView.setText(Data.companies.name);
                            viewHolder.mRateView.setText(getRate(Data.feedbacks));
                            Picasso.with(getContext()).load(Data.image_path).into(viewHolder.mImageView);
                            viewHolder.mView.setOnClickListener(v -> {
                                ModelFragment mod = ModelFragment.newInstance(Data.Key);
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent, mod).addToBackStack(null).commit();
                            });
                            viewHolder.mImageButton.setOnClickListener(v -> {
                                PopupMenu popup = new PopupMenu(context, viewHolder.mImageButton);
                                MenuInflater inflater1 = popup.getMenuInflater();
                                inflater1.inflate(R.menu.pop_menu, popup.getMenu());
                                popup.getMenu().findItem(R.id.item_favorite).setTitle("Remove From Favorite");
                                popup.setOnMenuItemClickListener(item -> {
                                    int id = item.getItemId();
                                    if (id == R.id.item_download) {
                                        //ToDo:Download
                                    } else if (id == R.id.item_favorite) {
                                        mAdapter.getRef(position).removeValue();
                                        mAdapter.notifyDataSetChanged();
                                    } else if (id == R.id.item_delete) {
                                        //ToDo:Delete
                                    }
                                    return true;
                                });
                                popup.show();
                            });
                            if (mAdapter.getItemCount() - 1 == position) {
                                progressDialog.dismiss();
                            }
                        });
                    }
                };
                recyclerView.setAdapter(mAdapter);
            }
        });
        return view;
    }

    private String getRate(List<FireBaseHelper.Feedbacks> lst) {

        if (lst.size() == 0) {
            return "0.0";
        } else {
            int sum = 0;

            for (FireBaseHelper.Feedbacks item : lst) {
                sum += Integer.parseInt(item.rate);
            }
            return String.format(Locale.ENGLISH, "%.1f", (double) sum / lst.size());
        }
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mCompanyView;
        public final TextView mRateView;
        public final ImageView mImageView;
        public final ImageButton mImageButton;

        public viewholder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.item_title);
            mCompanyView = (TextView) view.findViewById(R.id.item_company);
            mRateView = (TextView) view.findViewById(R.id.item_rate);
            mImageView = (ImageView) view.findViewById(R.id.item_image);
            mImageButton = (ImageButton) view.findViewById(R.id.item_menu);
        }
    }


}
