package care.dovetail.fragments;

import java.util.Map;

import android.support.v7.widget.CardView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.Utils;
import care.dovetail.common.model.Card;

import com.android.volley.toolbox.NetworkImageView;

public class CardUtils {

	public interface CardProcessor {
		public void process(Card card);
	}

	public static class Action {
		private final int title;
		private final int icon;
		final CardProcessor processor;
		public Action (int title, int icon, CardProcessor processor) {
			this.title = title;
			this.icon = icon;
			this.processor = processor;
		}
	}

	public static View getViewForCard(final Card card, OnClickListener menuListener,
			final OnClickListener optionListener, LayoutInflater inflater,
			Map<Card.Action, Action> actions, App app) {
		String title = card.getTitle();
		String text = card.getText();
		Card.Action actionType = card.getAction();
		Card.Type type = card.getType();
		if (type == Card.Type.POLL && title == null) {
			title = text;
		}

		View cardView = inflater.inflate(R.layout.list_item_card, null);
		Pair<Integer,Integer> layout = getCardLayout(card);
		View view = inflater.inflate(layout.first, null);
		view.setTag(card);
		TextView titleView = (TextView) view.findViewById(R.id.title);
		final TextView textView = (TextView) view.findViewById(R.id.text);
		ImageView iconView = (ImageView) view.findViewById(R.id.icon);
		ImageView photoView = (ImageView) view.findViewById(R.id.photo);
		TextView actionView = (TextView) view.findViewById(R.id.action);
		ImageView actionIconView = (ImageView) view.findViewById(R.id.action_icon);
		ViewGroup optionsView = (ViewGroup) view.findViewById(R.id.options);
		final TextView selectionValueView = (TextView) view.findViewById(R.id.selectionValue);
		SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
		final TextView submitButton = (TextView) view.findViewById(R.id.submit);
		ImageView decorView = (ImageView) view.findViewById(R.id.decor);

		if (layout.second > 0 && decorView != null) {
			decorView.setImageResource(layout.second);
		} else if (layout.second > 0) {
			view.setBackgroundResource(layout.second);
		}

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

		if (iconView != null && card.icon != null && iconView instanceof NetworkImageView) {
			((NetworkImageView) iconView).setImageUrl(card.icon, app.imageLoader);
		} else if (iconView != null) {
			iconView.setVisibility(View.GONE);
		}

		if (photoView != null && photoView instanceof NetworkImageView) {
			if (card.image != null && app.imageLoader != null) {
				((NetworkImageView) photoView).setImageUrl(card.image, app.imageLoader);
			} else {
				int index = card.hashCode() % Config.BACKGROUND_IMAGES.length;
				((NetworkImageView) photoView).setImageUrl(
						Config.BACKGROUND_IMAGES[index], app.imageLoader);
			}
		}

		if (actionView != null && actionType != Card.Action.NONE && actions != null) {
			final Action action = actions.get(actionType);
			if (action != null) {
				actionView.setText(action.title);
				actionView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						action.processor.process(card);
					}
				});
				if (actionIconView != null) {
					actionIconView.setImageDrawable(app.getResources().getDrawable(action.icon));
				}
			}
		} else if (actionView != null) {
			actionView.setVisibility(View.GONE);
		}

		if (optionsView != null && card.options != null) {
			for (String option : card.options) {
				View optionView = inflater.inflate(R.layout.option, null);
				((TextView) optionView.findViewById(R.id.text)).setText(option);
				optionView.setOnClickListener(optionListener);
				optionsView.addView(optionView);
				ViewGroup.MarginLayoutParams params =
						(MarginLayoutParams) optionView.getLayoutParams();
				params.width = params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
				params.rightMargin = params.topMargin =
						app.getResources().getDimensionPixelOffset(R.dimen.medium_margin);
			}
		}

		if (seekBar != null && card.options != null && card.options.length > 0
				&& selectionValueView != null && submitButton != null) {
			selectionValueView.setText(card.options[0]);
			seekBar.setMax((card.options.length - 1) * 20);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
					int index = Math.round(card.options.length * progress / bar.getMax());
					index = Math.min(index, card.options.length - 1);
					selectionValueView.setText(card.options[index]);
					submitButton.setVisibility(View.VISIBLE);
				}
				@Override public void onStartTrackingTouch(SeekBar bar) {}
				@Override public void onStopTrackingTouch(SeekBar bar) {}
			});
			submitButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					optionListener.onClick(selectionValueView);
				}
			});
		}

		if (type == Card.Type.SIZE) {
			titleView.setText(card.text);
			ProgressBar weekBar = (ProgressBar) view.findViewById(R.id.weekBar);
			TextView week = (TextView) view.findViewById(R.id.week);
			TextView trimester = (TextView) view.findViewById(R.id.trimester);
			try {
				int weekNumber = 0;
				for (String tag : card.tags) {
					if (tag.toLowerCase().startsWith("week:")) {
						weekNumber = Integer.parseInt(tag.split(":")[1]);
						break;
					}
				}
				if (weekNumber > 0) {
					int trimesterNumber = weekNumber / 13;
					week.setText(Integer.toString(weekNumber));
					trimester.setText(app.getResources()
							.getStringArray(R.array.trimester_options)[trimesterNumber]);
					weekBar.setProgress(Math.min(weekNumber, weekBar.getMax()));
				}
			} catch(Exception ex) {
				weekBar.setVisibility(View.GONE);
				week.setVisibility(View.GONE);
				trimester.setVisibility(View.GONE);
				view.findViewById(R.id.week_label).setVisibility(View.GONE);
				view.findViewById(R.id.trimester_label).setVisibility(View.GONE);
			}
		}

		((CardView) cardView.findViewById(R.id.card)).addView(view);
		ViewGroup menuView = (ViewGroup) cardView.findViewById(R.id.menu);

		// For cards with photo backgrounds make the text colapsible
		if (photoView != null && textView != null && text != null && !text.trim().isEmpty()) {
			cardView.setOnClickListener(new CardClickListener(app, textView));
		} else {
			cardView.setOnClickListener(new CardClickListener(app, null));
		}

		for (int i = 0; i < menuView.getChildCount(); i++) {
			menuView.getChildAt(i).setOnClickListener(menuListener);
			menuView.getChildAt(i).setTag(cardView);
		}
		menuView.setVisibility(View.GONE);
		return cardView;
	}

	private static class CardClickListener implements OnClickListener {
		private final App app;
		private final TextView textView;

		private CardClickListener(App app, TextView textView) {
			this.app = app;
			this.textView = textView;
		}

		@Override
		public void onClick(View v) {
			Utils.trackEvent(app, "Card", "Click",
					app.getResources().getString(R.string.action_settings));
			if (textView != null) {
				int visibility = textView.getVisibility();
				textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
			}
			View menu = v.findViewById(R.id.menu);
			int visibility = menu.getVisibility();
			menu.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
		}
	}

	public static Pair<Integer, Integer> getCardLayout(Card card) {
		switch(card.getType()) {
		case SIZE:
			return Pair.create(R.layout.card_size,
					Utils.getRandom(Config.BOTTOM_LEFT_DECOR, card.hashCode()));
		case TODO:
			return Pair.create(R.layout.card_todo, -1);
		case VOTE:
			return Pair.create(R.layout.card_chart, -1);
		case TIP:
		case MILESTONE:
			return Pair.create(R.layout.card_tip, -1);
		case CARE:
			return Pair.create(R.layout.card_action,
					Utils.getRandom(Config.BOTTOM_LEFT_DECOR, card.hashCode()));
		case SYMPTOM:
			return Pair.create(R.layout.card_symptom,
					Utils.getRandom(Config.BOTTOM_RIGHT_DECOR, card.hashCode()));
		case POLL:
			return Pair.create(R.layout.card_poll,
					Utils.getRandom(Config.BOTTOM_RIGHT_DECOR, card.hashCode()));
		case INSIGHT:
			return Pair.create(R.layout.card_insight,
					Utils.getRandom(Config.CENTER_DECOR, card.hashCode()));
		default:
			return card.getAction() == Card.Action.NONE ?
					Pair.create(R.layout.card_basic,
							Utils.getRandom(Config.CENTER_DECOR, card.hashCode()))
					: Pair.create(R.layout.card_action,
							Utils.getRandom(Config.BOTTOM_LEFT_DECOR, card.hashCode()));
		}
	}
}
