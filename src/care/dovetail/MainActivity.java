package care.dovetail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import care.dovetail.api.GroupsGet;
import care.dovetail.api.UserGet;
import care.dovetail.fragments.GroupNameFragment;
import care.dovetail.fragments.GroupsFragment;
import care.dovetail.fragments.HomeFragment;
import care.dovetail.messaging.GCMUtils;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";

	private App app;

	private DrawerLayout drawerLayout;
    private View drawer;
    private ActionBarDrawerToggle drawerToggle;

    private Fragment fragment;

	@SuppressLint("NewApi")
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		app = (App) getApplication();
		if (GCMUtils.checkPlayServices(this)) {
			app.requestPushToken();
		}

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer = findViewById(R.id.left_drawer);

        // Set the drawer toggle as the DrawerListener
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open,
        		R.string.drawer_close) {
            @Override
			public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            @Override
			public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        	getActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        }

		if (app.getUserUUID() == null || app.getUserAuth() == null) {
			startActivity(new Intent(this, SignUpActivity.class));
			finish();
		} else {
			new UserGet(app).execute();
			new GroupsGet(app).execute();
		}

		if (savedInstanceState != null) {
	        app.authInProgress = savedInstanceState.getBoolean(FitnessPollTask.AUTH_PENDING);
	    }

		FitnessPollTask.buildFitnessClient(this, app);

		setContentFragment(new HomeFragment(), false);
	}

	@Override
	protected void onStart() {
	    super.onStart();
	    Log.i(TAG, "Google API client connecting...");
	    if (!app.apiClient.isConnecting() && !app.apiClient.isConnected()) {
        	app.apiClient.connect();
        }
	}

	@Override
	protected void onStop() {
	    super.onStop();
	    if (app.apiClient.isConnected()) {
	    	app.apiClient.disconnect();
	    }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putBoolean(FitnessPollTask.AUTH_PENDING, app.authInProgress);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
	    }
		switch(item.getItemId()) {
		case R.id.action_search:
			startActivity(new Intent(this, SearchActivity.class));
			break;
		case R.id.action_create_group:
			new GroupNameFragment().show(getSupportFragmentManager(), null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawer);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        menu.findItem(R.id.action_create_group).setVisible(
        		(!drawerOpen) && (fragment instanceof GroupsFragment));
        return super.onPrepareOptionsMenu(menu);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == Config.ACTIVITY_REQUEST_OAUTH) {
	        app.authInProgress = false;
	        if (resultCode == RESULT_OK) {
	            // Make sure the app is not already connected or attempting to connect
	            if (!app.apiClient.isConnecting() && !app.apiClient.isConnected()) {
	            	app.apiClient.connect();
	            }
	        }
	    }
	}

	public void setContentFragment(Fragment fragment) {
		setContentFragment(fragment, true);
	}

	public void setContentFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction txn = getSupportFragmentManager().beginTransaction();
		txn.replace(R.id.content, fragment);
		if (addToBackStack) {
			txn.addToBackStack(fragment.getClass().getSimpleName());
		}
		txn.commit();
		drawerLayout.closeDrawers();
		this.fragment = fragment;
	}
}
