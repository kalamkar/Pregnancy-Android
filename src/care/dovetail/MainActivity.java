package care.dovetail;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import care.dovetail.api.GroupsGet;
import care.dovetail.api.UserGet;
import care.dovetail.fragments.BabyFragment;
import care.dovetail.fragments.GroupNameFragment;
import care.dovetail.fragments.GroupsFragment;
import care.dovetail.fragments.HomeFragment;
import care.dovetail.fragments.MomFragment;
import care.dovetail.messaging.GCMUtils;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";

	private App app;
	SectionsPagerAdapter adapter;
	ViewPager pager;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		adapter = new SectionsPagerAdapter(getSupportFragmentManager());

		app = (App) getApplication();
		if (GCMUtils.checkPlayServices(this)) {
			app.requestPushToken();
		}

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		pager.setCurrentItem(1);

		new UserGet(app).execute();
		new GroupsGet(app).execute();
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
		case R.id.action_add_group:
			new GroupNameFragment().show(getSupportFragmentManager(), null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private Fragment fragments[] = { new GroupsFragment(), new HomeFragment(),
				new MomFragment(), new BabyFragment() };
		private int titles[] = {R.string.messages, R.string.home, R.string.mom, R.string.baby};

		public SectionsPagerAdapter(FragmentManager fm) {
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
