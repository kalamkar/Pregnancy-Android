package care.dovetail.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.api.UserGet;
import care.dovetail.bluetooth.PairingActivity;
import care.dovetail.common.model.Card;

import com.android.volley.toolbox.NetworkImageView;

public class HomeFragment extends Fragment implements OnRefreshListener, OnClickListener {
	private static final String TAG = "HomeFragment";

	private App app;
	private List<Card> cards = new ArrayList<Card>();

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
		((SwipeRefreshLayout) view).setOnRefreshListener(this);
		updateCards();
		((ListView) view.findViewById(R.id.cards)).setAdapter(new CardsAdapter());
	}

	@Override
	public void onResume() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(listener);
		super.onResume();
	}

	@Override
	public void onDestroyView() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(listener);
		super.onDestroyView();
	}

	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if (App.USER_PROFILE.equals(key)) {
				updateCards();
				((BaseAdapter) ((ListView) getView().findViewById(R.id.cards)).getAdapter())
						.notifyDataSetChanged();
			}
		}
	};

	private void updateCards() {
		((SwipeRefreshLayout) getView()).setRefreshing(false);
		cards.clear();
		if (app.getMother().cards == null) {
			return;
		}
		Arrays.sort(app.getMother().cards, new Comparator<Card>() {
			@Override
			public int compare(Card lhs, Card rhs) {
				return lhs.priority - rhs.priority;
			}
		});
		for (Card card : app.getMother().cards) {
			if (card.expire_time <= 0 || card.expire_time > System.currentTimeMillis()) {
				cards.add(card);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onRefresh() {
		new UserGet(app).execute();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						((SwipeRefreshLayout) getView()).setRefreshing(false);
					}
				});
			}
		}, Config.REFRESH_TIMEOUT_MILLIS);
	}

	@Override
	public void onClick(View view) {
		Card card = (Card) view.getTag();
		if (view.getId() == R.id.menu_button) {
			CardMenuFragment fragment = new CardMenuFragment();
			fragment.setCard(card);
			fragment.show(getChildFragmentManager(), null);
			return;
		}
		String action = null;
		for (String tag : card.tags) {
			if (tag.toLowerCase().startsWith("action:")) {
				action = tag.replaceFirst("action:", "");
			}
		}
		if (Card.ACTION.DUE_DATE.name().equalsIgnoreCase(action)) {
			new DueDateFragment().show(getChildFragmentManager(), null);
		} else if (Card.ACTION.CONNECT_SCALE.name().equalsIgnoreCase(action)) {
			startActivity(new Intent(getActivity(), PairingActivity.class));
		} else if (Card.ACTION.CONNECT_HEALTH_DATA.name().equalsIgnoreCase(action)) {
			startActivity(new Intent(getActivity(), PairingActivity.class));
		}
	}

	private class CardsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return cards.size();
		}

		@Override
		public Card getItem(int position) {
			return cards.get(position);
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
				view.findViewById(R.id.menu_button).setOnClickListener(HomeFragment.this);
				view.setOnClickListener(HomeFragment.this);
			} else {
				view = convertView;
			}

			Card card = getItem(position);
			if (card.icon == null) {
				view.findViewById(R.id.icon).setVisibility(View.GONE);
			} else {
				view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
				((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
						card.icon, app.imageLoader);
			}
			((TextView) view.findViewById(R.id.title)).setText(card.text);
			view.setTag(card);
			view.findViewById(R.id.menu_button).setTag(card);
			return view;
		}
	}
}