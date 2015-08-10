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
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.CardView;
import android.util.Pair;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.api.CardUpdate;
import care.dovetail.api.UserGet;
import care.dovetail.bluetooth.PairingActivity;
import care.dovetail.common.model.Card;

public class HomeFragment extends Fragment implements OnRefreshListener, OnClickListener,
		OnLongClickListener, OnDragListener {
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

	private float dragStartX = -1;
	private float dragEndX = -1;

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
		view.setOnDragListener(this);
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

	@Override
	public boolean onLongClick(View view) {
		DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
		view.startDrag(ClipData.newPlainText("", ""), shadowBuilder, view, 0);
		view.setVisibility(View.INVISIBLE);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onDrag(View view, DragEvent event) {
	    switch (event.getAction()) {
	    case DragEvent.ACTION_DRAG_STARTED:
	    	dragStartX = event.getX();
	    	break;
	    case DragEvent.ACTION_DRAG_ENTERED:
	    	break;
	    case DragEvent.ACTION_DRAG_EXITED:
	    	break;
	    case DragEvent.ACTION_DROP:
    		View cardView = (View) event.getLocalState();
	    	if (Math.abs(dragStartX - event.getX()) > 500) {
	    		Card card = (Card) cardView.getTag();
	    		cards.remove(card);
	    		new CardUpdate(app).execute(Pair.create(CardUpdate.PARAM_CARD_ID, card.id),
						Pair.create(CardUpdate.PARAM_TAG, Card.TAGS.ARCHIVED.name()));
	    	} else {
	    		cardView.setVisibility(View.VISIBLE);
	    	}
		    dragStartX = -1;
	    	break;
	    case DragEvent.ACTION_DRAG_ENDED:
	    	((BaseAdapter) ((ListView) view.findViewById(R.id.cards)).getAdapter())
	    			.notifyDataSetChanged();
	    	break;
	    }
	    return true;
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
			String title = card.getTitle();
			String text = card.getText();
			Card.Action actionType = card.getAction();
			Card.Type type = card.getType();
			if (type == Card.Type.SIZE) {
				text = getResources().getString(R.string.thats_how_big_baby_is);
			} else if (type == Card.Type.POLL && title == null) {
				title = text;
			}

			View view = getActivity().getLayoutInflater().inflate(getCardLayout(card), null);
			TextView titleView = (TextView) view.findViewById(R.id.title);
			TextView textView = (TextView) view.findViewById(R.id.text);
			ImageView iconView = (ImageView) view.findViewById(R.id.icon);
			ImageView photoView = (ImageView) view.findViewById(R.id.photo);
			TextView actionView = (TextView) view.findViewById(R.id.action);
			ImageView actionIconView = (ImageView) view.findViewById(R.id.action_icon);
			ViewGroup optionsView = (ViewGroup) view.findViewById(R.id.options);
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
				for (String option : card.options) {
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
					ViewGroup.MarginLayoutParams params =
							(MarginLayoutParams) optionView.getLayoutParams();
					params.width = params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
					params.rightMargin = params.topMargin =
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
			cardView.setOnLongClickListener(HomeFragment.this);
			return cardView;
		}
	}

	public static int getCardLayout(Card card) {
		switch(card.getType()) {
		case SIZE:
			return R.layout.card_size;
		case TIP:
		case MILESTONE:
			return R.layout.card_tip;
		case CARE:
			return R.layout.card_action;
		case SYMPTOM:
			return R.layout.card_symptom;
		case POLL:
			return R.layout.card_poll;
		default:
			return card.getAction() == Card.Action.NONE ? R.layout.card_basic : R.layout.card_action;
		}
	}
}