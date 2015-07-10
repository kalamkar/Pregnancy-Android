package care.dovetail.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.common.model.Tip;

import com.android.volley.toolbox.NetworkImageView;

public class HomeFragment extends Fragment implements OnClickListener {
	private static final String TAG = "HomeFragment";

	private App app;
	private List<Tip> tips = new ArrayList<Tip>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		updateTips();
		((ListView) view).setAdapter(new CardsAdapter());
	}

	@Override
	public void onResume() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(listener);
		super.onResume();
	}
	@Override
	public void onPause() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(listener);
		super.onPause();
	}

	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			updateTips();
			((BaseAdapter) ((ListView) getView()).getAdapter()).notifyDataSetChanged();
		}
	};

	private void updateTips() {
		tips.clear();
		tips.add(makeHelloTip());
		if (app.getMother().insights == null) {
			return;
		}
		Arrays.sort(app.getMother().insights, new Comparator<Tip>() {
			@Override
			public int compare(Tip lhs, Tip rhs) {
				return lhs.priority - rhs.priority;
			}
		});
		for (Tip tip : app.getMother().insights) {
			tips.add(tip);
		}
	}

	@Override
	public void onClick(View view) {
		Object tag = view.getTag();
		if (view.getId() == R.id.close && tag != null && tag instanceof Tip) {
			tips.remove(tag);
			((BaseAdapter) ((ListView) getView()).getAdapter()).notifyDataSetChanged();
		}
	}

	private class CardsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return tips.size();
		}

		@Override
		public Tip getItem(int position) {
			return tips.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.list_item_card, null);
				view.findViewById(R.id.hint).setVisibility(View.GONE);
				view.findViewById(R.id.time).setVisibility(View.GONE);
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						View menu = v.findViewById(R.id.menu);
						menu.setVisibility(menu.getVisibility() == View.INVISIBLE ?
								View.VISIBLE : View.INVISIBLE);
					}
				});
			} else {
				view = convertView;
			}
			view.findViewById(R.id.menu).setVisibility(View.INVISIBLE);

			Tip tip = getItem(position);
			String iconUrl = getIcon(tip);
			if (iconUrl == null) {
				view.findViewById(R.id.icon).setVisibility(View.GONE);
			} else {
				view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
				((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
						iconUrl, app.imageLoader);
			}
			((TextView) view.findViewById(R.id.title)).setText(tip.title);
			view.findViewById(R.id.close).setOnClickListener(HomeFragment.this);
			view.findViewById(R.id.close).setTag(tip);
			view.setTag(tip);
			return view;
		}
	}

	private String getIcon(Tip tip) {
		if (tip == null || tip.tags == null) {
			return null;
		}
		for (String tag : tip.tags) {
			if (tag != null && tag.toLowerCase().startsWith("image")) {
				return tag.replaceFirst("image:", "");
			}
		}
		return null;
	}

	private Tip makeHelloTip() {
		String firstName = app.getMother().name != null ? app.getMother().name.split(" ")[0] : "";
		return new Tip(
				String.format(getResources().getString(R.string.hello_text), firstName), null, 1);
	}
}