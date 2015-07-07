package care.dovetail;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private String drawerMenuTitles[];

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

		drawerMenuTitles = getResources().getStringArray(R.array.drawer_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, drawerMenuTitles));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
			public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            @Override
			public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

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
		switch(item.getItemId()) {
		case R.id.action_drawer:
			// startActivity(new Intent(this, ProfileActivity.class));
			if (drawerLayout.isDrawerOpen(drawerList)) {
				drawerLayout.closeDrawer(drawerList);
			} else {
				drawerLayout.openDrawer(drawerList);
			}
			break;
		case R.id.action_search:
			startActivity(new Intent(this, SearchActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        Log.i(TAG, String.format("Item no. %d clicked", position));
	    }
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
