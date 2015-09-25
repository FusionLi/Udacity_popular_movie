package com.example.android.udacity_popular_movie;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.android.udacity_popular_movie.Model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final int MAX_PAGES = 100;
    private int mPagesLoaded = 0;
    private boolean mIsLoading = false;
    private ImageAdapter mImageAdapter;

    public final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public MainActivityFragment() {
    }

    private class FetchPageTask extends AsyncTask<Integer, Void, Collection<Movie>> {

        @Override
        protected Collection<Movie> doInBackground(Integer... integers) {
            if (integers.length == 0) {
                return null;
            }
            int page = integers[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String responseJsonStr = null;
            try {
                final String API_BASE_URI = "http://api.themoviedb.org/3/movie";
                final String API_KEY = "api_key";
                final String API_PARAM_PAGE = "page";
                Uri buildUri = Uri.parse(API_BASE_URI).buildUpon()
                        .appendPath("popular")
                        .appendQueryParameter(API_PARAM_PAGE, String.valueOf(page))
                        .appendQueryParameter(API_KEY, "cfb1decdd632391492006dbbfbb3e73b")
                        .build();

//                Log.d(LOG_TAG, "URL: " + buildUri.toString());

                URL url = new URL(buildUri.toString());

                // Create the request to themoviedb api, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                responseJsonStr = buffer.toString();
                //Log.d(LOG_TAG, "JSON: " + responseJsonStr);

            } catch (Exception e) {
                Log.d(LOG_TAG, "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return fetchMoviesFromJson(responseJsonStr);
            } catch(JSONException e) {
                Log.d(LOG_TAG, "Can't parse JSON: " + responseJsonStr, e);
                return null;
            }
        }

        private Collection<Movie> fetchMoviesFromJson(String jsonStr) throws JSONException {
            final String KEY_MOVIES = "results";
            JSONObject json  = new JSONObject(jsonStr);
            JSONArray movies = json.getJSONArray(KEY_MOVIES);
            ArrayList result = new ArrayList<>();
            for (int i = 0; i < movies.length(); i++) {
                result.add(Movie.fromJson(movies.getJSONObject(i)));
            }
            return result;
        }

        @Override
        protected void onPostExecute(Collection<Movie> collection) {
            if (collection == null) {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.msg_server_error),
                        Toast.LENGTH_SHORT
                ).show();
                stopLoading();
                return;
            }
            mPagesLoaded++;
            stopLoading();
            mImageAdapter.addAll(collection);
        }
    }

    private void startLoading () {
        if (mIsLoading) {
            return;
        }
        if (mPagesLoaded >= MAX_PAGES) {
            return;
        }
        mIsLoading = true;

        new FetchPageTask().execute(mPagesLoaded + 1);
    }

    private void stopLoading() {
        if (!mIsLoading) {
            return;
        }
        mIsLoading = false;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mImageAdapter = new ImageAdapter(getActivity());
        GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        gridView.setAdapter(mImageAdapter);
        gridView.setOnScrollListener(
                new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView absListView, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        int lastInScreen = firstVisibleItem + visibleItemCount;
//                        Log.i(LOG_TAG, "first: " + firstVisibleItem + " visible: " + visibleItemCount + " total: " + totalItemCount);
                        if (lastInScreen == totalItemCount) {
                            startLoading();
                        }
                    }
                }
        );
        startLoading();
        return view;
    }
}