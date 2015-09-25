package com.example.android.udacity_popular_movie;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.udacity_popular_movie.Model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lizeq_000 on 9/22/2015.
 */
public class ImageAdapter extends BaseAdapter {
    public final String LOG_TAG = ImageAdapter.class.getSimpleName();

    private Context mContext;
    private final ArrayList<Movie> mMovies;
    private final int mHeight;
    private final int mWidth;

    public ImageAdapter(Context c) {
        mContext = c;
        mMovies = new ArrayList<>();
        mHeight = Math.round(mContext.getResources().getDimension(R.dimen.poster_height));
        mWidth = Math.round(mContext.getResources().getDimension(R.dimen.poster_width));
    }

    public void addAll(Collection<Movie> xs) {
        mMovies.addAll(xs);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    @Override
    public Object getItem(int i) {
        if (i < 0 || i >= mMovies.size()) {
            return null;
        }
        return mMovies.get(i);
    }

    @Override
    public long getItemId(int i) {
        Movie movie = (Movie) getItem(i);
        if (movie == null) {
            return -1L;
        }

        return movie.id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Movie movie = (Movie) getItem(i);
        if (movie == null) {
            return null;
        }

        ImageView imageView;
        if (view == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(mWidth, mHeight));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) view;
        }

        Uri posterUri = movie.buildPosterUri(mContext.getString(R.string.api_poster_default_size));
        Picasso.with(mContext)
                .load(posterUri)
                .into(imageView);

        return imageView;
    }
}