package io.github.jokoframework.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.jokoframework.R;
import io.github.jokoframework.constants.AppConstants;
import io.github.jokoframework.mboehaolib.util.Utils;
import io.github.jokoframework.model.UserData;
import io.github.jokoframework.singleton.MboehaoApp;

/**
 * Created by afeltes on 02/12/17.
 */

public class BaseActivity extends Activity {

    private static final String LOG_TAG = BaseActivity.class.getName();
    public static final int DEFAULT_WAIT_ON_NO_CONNECTION = 10;

    protected static MboehaoApp application;
    private String appVersionName;
    protected boolean dismisableWhenNoConnection = true;
    private ProgressDialog progressDialog;

    /**
     * La primera actividad default de la aplicación.
     */
    private Class<? extends Activity> firstActivity;
    private boolean withInternetConnection;


    public final synchronized MboehaoApp getApp() {
        if (application == null) {
            application = (MboehaoApp) getApplicationContext();
        }
        return application;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeProgress();
        getApp().setBaseActivity(this);
        if (Utils.isNetworkAvailable(this)) {
            setWithInternetConnection(true);
            //Pone la flecha de regresar atras en el action bar
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            startFirstActivity(this);
        } else {
            setWithInternetConnection(false);
            Utils.showStickyMessage(this, getString(R.string.no_network_connection), Style.INFO);
            finishActivityDelayed(BaseActivity.DEFAULT_WAIT_ON_NO_CONNECTION);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    protected void startFirstActivity(Activity activity) {
        Log.d(LOG_TAG, "Dummy");
    }

    public Class<? extends Activity> getFirstActivity() {
        return firstActivity;
    }

    public void setFirstActivity(Class<? extends Activity> firstActivity) {
        this.firstActivity = firstActivity;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    protected boolean isLandscapeMode() {
        return Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation;
    }

    protected void finishActivityDelayed(long secondsToWait) {
        new ActivityFinisherdWithDelay().execute(secondsToWait);
    }

    public void setWithInternetConnection(boolean withInternetConnection) {
        this.withInternetConnection = withInternetConnection;
    }

    public boolean isWithInternetConnection() {
        return withInternetConnection;
    }

    private class ActivityFinisherdWithDelay extends AsyncTask<Long, Void, Long> {
        @Override
        protected Long doInBackground(Long... params) {
            if (params != null && params.length > 0) {
                Utils.sleep(AppConstants.ONE_SECOND * params[0]);
            } else {
                Utils.sleep(AppConstants.ONE_SECOND * DEFAULT_WAIT_ON_NO_CONNECTION);
            }
            finish();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isWithInternetConnection()) {
            setWithInternetConnection(Utils.isNetworkAvailable(this));
        }
        if (!isWithInternetConnection()) {
            showNoConnectionAndQuit();
        }
    }

    public void showNoConnectionAndQuit() {
        final String msg = getString(R.string.no_network_connection);
        Utils.showStickyMessage(this, msg, Style.INFO);
        Toast.makeText(this, msg, Toast.LENGTH_LONG);
        setProgressMessage(msg);
        Log.d(LOG_TAG, msg);
        if (isDismisableWhenNoConnection()) {
            new QuitterNoConnection().execute(msg);
        }
    }

    private class QuitterNoConnection extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Utils.sleep(AppConstants.ONE_SECOND * DEFAULT_WAIT_ON_NO_CONNECTION);
            System.exit(0);
            return null;
        }
    }

    public boolean isDismisableWhenNoConnection() {
        return dismisableWhenNoConnection;
    }

    public void setDismisableWhenNoConnection(boolean dismisableWhenNoConnection) {
        this.dismisableWhenNoConnection = dismisableWhenNoConnection;
    }

    public void initializeProgress() {
        setProgressDialog(new ProgressDialog(this));
    }

    public void showProgress(final boolean show, final String message) {
        if (progressDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (show) {
                        progressDialog.setMessage(message);
                        progressDialog.show();
                    } else {
                        progressDialog.hide();
                    }
                }
            });
        }
    }

    public void showProgress(boolean b) {
        showProgress(false, null);
    }

    public void setProgressMessage(String message) {
        if (progressDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (StringUtils.isNotBlank(message) && !progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                    progressDialog.setMessage(message);
                }
            });
        }
    }


    public static void setApplication(MboehaoApp application) {
        BaseActivity.application = application;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getProgressDialog() != null) {
            getProgressDialog().dismiss();
        }
    }

    public final UserData getUserData() {
        return getApp().getUserData();
    }

}
