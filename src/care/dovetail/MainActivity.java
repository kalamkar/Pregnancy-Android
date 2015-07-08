package care.dovetail;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import care.dovetail.api.GroupsGet;
import care.dovetail.api.UserGet;
import care.dovetail.fragments.BabyFragment;
import care.dovetail.fragments.GroupsFragment;
import care.dovetail.fragments.HomeFragment;
import care.dovetail.messaging.GCMUtils;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";

	private App app;
	private PagerAdapter adapter;
	private ViewPager pager;

	private DrawerLayout drawerLayout;
    private View drawer;
    private ActionBarDrawerToggle drawerToggle;

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

		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new PagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		pager.setCurrentItem(1);

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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawer);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

	public class PagerAdapter extends FragmentStatePagerAdapter {
		private Fragment fragments[] = { new GroupsFragment(), new HomeFragment(),
				new BabyFragment() };
		private int titles[] = {R.string.messages, R.string.home, R.string.baby};

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments[position];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getString(titles[position]).toUpperCase(Locale.getDefault());
		}
	}
}
