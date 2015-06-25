package care.dovetail;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import care.dovetail.api.GroupsGet;
import care.dovetail.api.UserGet;
import care.dovetail.fragments.BabyFragment;
import care.dovetail.fragments.GroupsFragment;
import care.dovetail.fragments.HomeFragment;
import care.dovetail.messaging.GCMUtils;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";

	private App app;
	PagerAdapter adapter;
	ViewPager pager;

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
		case R.id.action_profile:
			startActivity(new Intent(this, ProfileActivity.class));
			break;
		case R.id.action_search:
			startActivity(new Intent(this, SearchActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
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
