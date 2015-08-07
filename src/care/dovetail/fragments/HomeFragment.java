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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.api.UserGet;
import care.dovetail.bluetooth.PairingActivity;
import care.dovetail.common.model.Card;

public class HomeFragment extends Fragment implements OnRefreshListener, OnClickListener {
	private static final String TAG = "HomeFragment";

	private static class Action {
		int title;
		int icon;
		Runnable runnable;
		Action (int title, int icon, Runnable runnable) {
			this.title = title;
			this.icon = icon;
			this.runnable = runnable;
		}
	}

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
					}
				}));
		actions.put(Card.Action.CONNECT_SCALE,
				new Action(R.string.pair_scale, R.drawable.ic_action_pair, new Runnable() {
					@Override
					public void run() {
						startActivity(new Intent(getActivity(), PairingActivity.class));
					}
				}));
		actions.put(Card.Action.CONNECT_HEALTH_DATA,
				new Action(R.string.pair_google_fit, R.drawable.ic_heart, new Runnable() {
					@Override
					public void run() {
						startActivity(new Intent(getActivity(), PairingActivity.class));
					}
				}));
		actions.put(Card.Action.TO_DO,
				new Action(R.string.action_add_to_do, R.drawable.ic_todo, new Runnable() {
					@Override
					public void run() {
						// TODO(abhi): Add TO-Do Action here
						Toast.makeText(app, "Adding to do", Toast.LENGTH_SHORT).show();
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
			Card card = getItem(position);
			String title = card.getTitle();
			String text = card.getText();
			Card.Action actionType = card.getAction();
			Card.Type type = card.getType();
			switch(type) {
			case SIZE:
				view = getActivity().getLayoutInflater().inflate(R.layout.card_size, null);
				text = getResources().getString(R.string.thats_how_big_baby_is);
				break;
			case TIP:
			case MILESTONE:
				view = getActivity().getLayoutInflater().inflate(R.layout.card_tip, null);
				break;
			case CARE:
				view = getActivity().getLayoutInflater().inflate(R.layout.card_care, null);
				break;
			case SYMPTOM:
				view = getActivity().getLayoutInflater().inflate(R.layout.card_symptom, null);
				break;
			case POLL:
				view = getActivity().getLayoutInflater().inflate(R.layout.card_poll, null);
				title = title == null ? text : title;
				break;
			default:
				view = getActivity().getLayoutInflater().inflate(R.layout.card_basic, null);
				break;
			}

			TextView titleView = (TextView) view.findViewById(R.id.title);
			TextView textView = (TextView) view.findViewById(R.id.text);
			ImageView iconView = (ImageView) view.findViewById(R.id.icon);
			ImageView photoView = (ImageView) view.findViewById(R.id.photo);
			TextView actionView = (TextView) view.findViewById(R.id.action);
			ImageView actionIconView = (ImageView) view.findViewById(R.id.action_icon);
			LinearLayout optionsView = (LinearLayout) view.findViewById(R.id.options);
			SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);

			if (title != null && titleView != null) {
				titleView.setText(title);
			} else if (titleView != null) {
				titleView.setVisibility(View.GONE);
			}

			if (text != null && !text.trim().isEmpty() && textView != null) {
				textView.setText(text);
			} else if (textView != null) {
				textView.setVisibility(View.GONE);
			}

			if (iconView != null && card.icon != null) {
				// TODO(abhi): Make this NetworkImageView
				iconView.setImageURI(Uri.parse(card.icon));
			} else if (iconView != null) {
				iconView.setVisibility(View.GONE);
			}

			if (photoView != null && card.image != null) {
				// TODO(abhi): Make this NetworkImageView
				photoView.setImageURI(Uri.parse(card.image));
			}

			if (actionView != null && actionType != Card.Action.NONE) {
				final Action action = actions.get(actionType);
				if (action != null) {
					actionView.setText(action.title);
					actionView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							action.runnable.run();
						}
					});
					if (actionIconView != null) {
						actionIconView.setImageDrawable(getResources().getDrawable(action.icon));
					}
				}
			}

			if (optionsView != null && card.options != null) {
				for (final String option : card.options) {
					View optionView =
							getActivity().getLayoutInflater().inflate(R.layout.option, null);
					((TextView) optionView.findViewById(R.id.text)).setText(option);
					optionView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO(abhi): Handle poll choice
							Toast.makeText(app, ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
						}
					});
					optionsView.addView(optionView);
					((LinearLayout.LayoutParams) optionView.getLayoutParams()).topMargin =
							getResources().getDimensionPixelOffset(R.dimen.medium_margin);
				}
			}
			if (seekBar != null) {
				seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
						// TODO(abhi): Do something here
					}
					@Override public void onStartTrackingTouch(SeekBar bar) {}
					@Override public void onStopTrackingTouch(SeekBar bar) {}
				});
			}

			View cardView = getActivity().getLayoutInflater().inflate(R.layout.list_item_card, null);
			((CardView) cardView.findViewById(R.id.card)).addView(view);
			cardView.setTag(card);
			cardView.findViewById(R.id.menu_button).setTag(card);
			cardView.findViewById(R.id.menu_button).setOnClickListener(HomeFragment.this);
			cardView.setOnClickListener(HomeFragment.this);
			return cardView;
		}
	}
}