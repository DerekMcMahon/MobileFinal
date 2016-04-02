package com.mysampleapp.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.mysampleapp.MainActivity;
import com.mysampleapp.MapsActivity;
import com.mysampleapp.R;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HomeDemoFragment extends DemoFragmentBase {

    //Login information
    private CognitoCachingCredentialsProvider credentialsProvider;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private static final String LOG_TAG = IdentityDemoFragment.class.getSimpleName();

    private MainActivity activity;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_demo_home, container, false);
        activity = (MainActivity)this.getActivity();

        /*FACEBOOK STUFF_________________________________________________________________________________
        FacebookSdk.sdkInitialize(activity);

        credentialsProvider = new CognitoCachingCredentialsProvider(activity, "87e455b8-be67-45a4-91fe-308d1ff39845", Regions.US_EAST_1);

        loginButton = (LoginButton)view.findViewById(R.id.login_button);
        loginButton.setFragment(this);

        callbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Context context = activity;
                CharSequence text = "onSuccess() was called...";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                if(AccessToken.getCurrentAccessToken().getToken() != null){
                    Context context2 = activity;
                    CharSequence text2 = "The access token exists...";
                    int duration2 = Toast.LENGTH_SHORT;
                    Toast toast2 = Toast.makeText(context2, text2, duration2);
                    toast2.show();
                }

                Map<String, String> logins = new HashMap<String, String>();
                logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
                credentialsProvider.setLogins(logins);
                //new GetIdentityTask().execute();
                fetchUserIdentity();
                Intent intent = new Intent(activity, MapsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        */

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final DemoListAdapter adapter = new DemoListAdapter(getActivity());
        adapter.addAll(DemoConfiguration.getDemoFeatureList());

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                final DemoConfiguration.DemoFeature item = adapter.getItem(position);
                final AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    final Fragment fragment = DemoInstructionFragment.newInstance(item.name);

                    activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment_container, fragment, item.name)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                    // Set the title for the fragment.
                    final ActionBar actionBar = activity.getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setTitle(item.titleResId);
                    }
                }
            }
        });
    }

    private static final class DemoListAdapter extends ArrayAdapter<DemoConfiguration.DemoFeature> {
        private LayoutInflater inflater;

        public DemoListAdapter(final Context context) {
            super(context, R.layout.list_item_icon_text_with_subtitle);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view;
            ViewHolder holder;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_item_icon_text_with_subtitle, parent, false);
                holder = new ViewHolder();
                holder.iconImageView = (ImageView) view.findViewById(R.id.list_item_icon);
                holder.titleTextView = (TextView) view.findViewById(R.id.list_item_title);
                holder.subtitleTextView = (TextView) view.findViewById(R.id.list_item_subtitle);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) convertView.getTag();
            }

            DemoConfiguration.DemoFeature item = getItem(position);
            holder.iconImageView.setImageResource(item.iconResId);
            holder.titleTextView.setText(item.titleResId);
            holder.subtitleTextView.setText(item.subtitleResId);

            return view;
        }
    }

    private static final class ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;
        TextView subtitleTextView;
    }

    private class GetIdentityTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void...params) {
            credentialsProvider.getIdentityId();
            return null;
        }
    }

    /*FACEBOOK STUFF___________________________________________________________
    private void fetchUserIdentity() {
        Log.d(LOG_TAG, "fetchUserIdentity");

        // Pre-fetched to avoid race condition where fragment is no longer active.
        final String unknownUserIdentityText =
                getString(R.string.identity_demo_unknown_identity_text);

        AWSMobileClient.defaultMobileClient()
                .getIdentityManager()
                .getUserID(new IdentityManager.IdentityHandler() {

                    @Override
                    public void handleIdentityID(String identityId) {

                        // We have successfully retrieved the user's identity. You can use the
                        // user identity value to uniquely identify the user. For demonstration
                        // purposes here, we will display the value in a text view.
                    }

                    @Override
                    public void handleError(Exception exception) {

                        // We failed to retrieve the user's identity. Set unknown user identitier
                        // in text view.

                        final Context context = getActivity();

                        if (context != null && isAdded()) {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.identity_demo_error_dialog_title)
                                    .setMessage(getString(R.string.identity_demo_error_message_failed_get_identity)
                                            + exception.getMessage())
                                    .setNegativeButton(R.string.identity_demo_dialog_dismiss_text, null)
                                    .create()
                                    .show();
                        }
                    }
                });
    }
    */
}
