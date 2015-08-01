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
import care.dovetail.common.Config;
import care.dovetail.common.model.Card;
import care.dovetail.common.model.Event;

import com.android.volley.toolbox.NetworkImageView;

public class HomeFragment extends Fragment implements OnClickListener {
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
		updateCards();
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
			updateCards();
			((BaseAdapter) ((ListView) getView()).getAdapter()).notifyDataSetChanged();
		}
	};

	private void updateCards() {
		cards.clear();
		cards.add(makeHelloCard());
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
			cards.add(card);
		}
	}

	@Override
	public void onClick(View view) {
		Object tag = view.getTag();
		if (view.getId() == R.id.close && tag != null && tag instanceof Card) {
			cards.remove(tag);
			app.events.add(new Event(Event.Type.CARD_ARCHIVED.name(), System.currentTimeMillis(),
					Config.GSON.toJson(tag)));
			((BaseAdapter) ((ListView) getView()).getAdapter()).notifyDataSetChanged();
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
				view.findViewById(R.id.hint).setVisibility(View.GONE);
				view.findViewById(R.id.time).setVisibility(View.GONE);
				view.findViewById(R.id.menu_button).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						View menu = ((View) v.getParent()).findViewById(R.id.menu);
						menu.setVisibility(menu.getVisibility() == View.INVISIBLE ?
								View.VISIBLE : View.INVISIBLE);
					}
				});
			} else {
				view = convertView;
			}
			view.findViewById(R.id.menu).setVisibility(View.INVISIBLE);

			Card card = getItem(position);
			String iconUrl = getIcon(card);
			if (iconUrl == null) {
				view.findViewById(R.id.icon).setVisibility(View.GONE);
			} else {
				view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
				((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
						iconUrl, app.imageLoader);
			}
			((TextView) view.findViewById(R.id.title)).setText(card.text);
			view.findViewById(R.id.close).setOnClickListener(HomeFragment.this);
			view.findViewById(R.id.close).setTag(card);
			view.setTag(card);
			return view;
		}
	}

	private static String getIcon(Card card) {
		if (card == null || card.tags == null) {
			return null;
		}
		for (String tag : card.tags) {
			if (tag != null && tag.toLowerCase().startsWith("image")) {
				return tag.replaceFirst("image:", "");
			}
		}
		return null;
	}

	private Card makeHelloCard() {
		String firstName = app.getMother().name != null ? app.getMother().name.split(" ")[0] : "";
		Card card = new Card();
		card.text = String.format(getResources().getString(R.string.hello_text), firstName);
		card.priority = 0;
		return card;
	}
}