package care.dovetail.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.Utils;
import care.dovetail.api.CardAdd;
import care.dovetail.api.CardUpdate;
import care.dovetail.api.UserGet;
import care.dovetail.bluetooth.PairingActivity;
import care.dovetail.common.model.Card;
import care.dovetail.common.model.Event;
import care.dovetail.fragments.CardUtils.Action;

import com.google.android.gms.analytics.HitBuilders;

public class HomeFragment extends Fragment implements OnRefreshListener, OnClickListener {
	private static final String TAG = "HomeFragment";

	private App app;
	private List<Card> cards = new ArrayList<Card>();
	private Map<Card.Action, Action> actions = new HashMap<Card.Action, Action>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
		actions.put(Card.Action.DUE_DATE,
				new Action(R.string.due_date, R.drawable.ic_date, new Runnable() {
					@Override
					public void run() {
						new DueDateFragment().show(getChildFragmentManager(), null);
						Utils.trackEvent(app, "Card", "Action",
								getResources().getString(R.string.due_date));
					}
				}));
		actions.put(Card.Action.CONNECT_SCALE,
				new Action(R.string.pair_scale, R.drawable.ic_action_pair, new Runnable() {
					@Override
					public void run() {
						startActivity(new Intent(getActivity(), PairingActivity.class));
						Utils.trackEvent(app, "Card", "Action",
								getResources().getString(R.string.pair_scale));
					}
				}));
		actions.put(Card.Action.CONNECT_HEALTH_DATA,
				new Action(R.string.pair_google_fit, R.drawable.ic_heart, new Runnable() {
					@Override
					public void run() {
						startActivity(new Intent(getActivity(), PairingActivity.class));
						Utils.trackEvent(app, "Card", "Action",
								getResources().getString(R.string.pair_google_fit));
					}
				}));
		actions.put(Card.Action.TO_DO,
				new Action(R.string.action_add_to_do, R.drawable.ic_todo, new Runnable() {
					@Override
					public void run() {
						// TODO(abhi): Add TO-Do Action here
						Toast.makeText(app, "Adding to do", Toast.LENGTH_SHORT).show();
						Utils.trackEvent(app, "Card", "Action",
								getResources().getString(R.string.action_add_to_do));
					}
				}));
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
		app.tracker.setScreenName(TAG);
		app.tracker.send(new HitBuilders.ScreenViewBuilder().build());
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
			Utils.trackEvent(app, "Card", "Click",
					getResources().getString(R.string.action_settings));
			return;
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
			Card card = getItem(position);
			return CardUtils.getViewForCard(card, HomeFragment.this, new OptionClickListener(card),
					getActivity().getLayoutInflater(), actions, getResources(), app.imageLoader);
		}
	}

	private class OptionClickListener implements OnClickListener {
		private final Card card;
		private OptionClickListener(Card card) {
			this.card = card;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onClick(View view) {
			Toast.makeText(app, ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
			String pollId = Utils.digest(card.text);
			String tags = Event.Type.VOTE.name().toLowerCase() + "," + Utils.join(card.tags) +
					"," + pollId;

			// Add an event recording the vote
			app.events.add(new Event(tags.split(","), System.currentTimeMillis(),
					((TextView) view).getText().toString()));

			// Create a card for results of the poll votes
			new CardAdd(app).execute(Pair.create(CardAdd.PARAM_TAGS, tags),
					Pair.create(CardAdd.PARAM_TEXT, card.text),
					Pair.create(CardAdd.PARAM_IMAGE, String.format("%s?tags=%s",
							Config.EVENT_CHART_URL, pollId)),
					Pair.create(CardAdd.PARAM_PRIORITY, Integer.toString(card.priority)));


			// Archive the poll / symptom card
			new CardUpdate(app).execute(Pair.create(CardUpdate.PARAM_CARD_ID, card.id),
					Pair.create(CardUpdate.PARAM_TAG, Card.TAGS.ARCHIVED.name()));
			Utils.trackEvent(app, "Card", "Option", ((TextView) view).getText().toString());
		}
	};
}