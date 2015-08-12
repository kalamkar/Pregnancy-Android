package care.dovetail.fragments;

import java.util.Map;

import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.CardView;
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
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.common.model.Card;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class CardUtils {

	public static class Action {
		int title;
		int icon;
		Runnable runnable;
		public Action (int title, int icon, Runnable runnable) {
			this.title = title;
			this.icon = icon;
			this.runnable = runnable;
		}
	}

	public static View getViewForCard(final Card card, OnClickListener menuListener,
			OnClickListener optionListener,
			LayoutInflater inflater, Map<Card.Action, Action> actions, Resources resources,
			ImageLoader imageLoader) {
		String title = card.getTitle();
		String text = card.getText();
		Card.Action actionType = card.getAction();
		Card.Type type = card.getType();
		if (type == Card.Type.SIZE) {
			text = resources.getString(R.string.thats_how_big_baby_is);
		} else if (type == Card.Type.POLL && title == null) {
			title = text;
		}

		View view = inflater.inflate(getCardLayout(card), null);
		TextView titleView = (TextView) view.findViewById(R.id.title);
		final TextView textView = (TextView) view.findViewById(R.id.text);
		ImageView iconView = (ImageView) view.findViewById(R.id.icon);
		ImageView photoView = (ImageView) view.findViewById(R.id.photo);
		TextView actionView = (TextView) view.findViewById(R.id.action);
		ImageView actionIconView = (ImageView) view.findViewById(R.id.action_icon);
		ViewGroup optionsView = (ViewGroup) view.findViewById(R.id.options);
		final TextView selectionValueView = (TextView) view.findViewById(R.id.selectionValue);
		SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);

		if (title != null && titleView != null) {
			titleView.setText(title);
		} else if (titleView != null) {
			titleView.setVisibility(View.GONE);
		}

		if (text != null && !text.trim().isEmpty() && textView != null) {
			textView.setText(text);
			titleView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					int visibility = textView.getVisibility();
					textView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
				}
			});
		} else if (textView != null) {
			textView.setVisibility(View.GONE);
		}

		if (iconView != null && card.icon != null) {
			// TODO(abhi): Make this NetworkImageView
			iconView.setImageURI(Uri.parse(card.icon));
		} else if (iconView != null) {
			iconView.setVisibility(View.GONE);
		}

		if (photoView != null && type != Card.Type.SIZE && photoView instanceof NetworkImageView) {
			if (card.image != null && imageLoader != null) {
				((NetworkImageView) photoView).setImageUrl(card.image, imageLoader);
			} else {
				int index = card.hashCode() % Config.BACKGROUND_IMAGES.length;
				((NetworkImageView) photoView).setImageUrl(Config.BACKGROUND_IMAGES[index], imageLoader);
			}
		}

		if (actionView != null && actionType != Card.Action.NONE && actions != null) {
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
					actionIconView.setImageDrawable(resources.getDrawable(action.icon));
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
						resources.getDimensionPixelOffset(R.dimen.medium_margin);
			}
		}

		if (seekBar != null && card.options != null && card.options.length > 0
				&& selectionValueView != null) {
			selectionValueView.setText(card.options[0]);
			seekBar.setMax((card.options.length - 1) * 20);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
					int index = Math.round(card.options.length * progress / bar.getMax());
					index = Math.min(index, card.options.length - 1);
					selectionValueView.setText(card.options[index]);
				}
				@Override public void onStartTrackingTouch(SeekBar bar) {}
				@Override public void onStopTrackingTouch(SeekBar bar) {}
			});
		}

		if (type == Card.Type.SIZE) {
			ProgressBar weekBar = (ProgressBar) view.findViewById(R.id.weekBar);
			TextView week = (TextView) view.findViewById(R.id.week);
			try {
				int weekNumber = 0;
				for (String tag : card.tags) {
					if (tag.toLowerCase().startsWith("week:")) {
						weekNumber = Integer.parseInt(tag.split(":")[1]);
						break;
					}
				}
				if (weekNumber > 0) {
					int trimester = weekNumber / 13;
					week.setText(String.format("%s %d. %s", resources.getString(R.string.week),
							weekNumber,
							resources.getStringArray(R.array.trimester_options)[trimester]));
					weekBar.setProgress(Math.min(weekNumber, weekBar.getMax()));
				}
			} catch(Exception ex) {
				weekBar.setVisibility(View.GONE);
				week.setVisibility(View.GONE);
			}
		}

		View cardView = inflater.inflate(R.layout.list_item_card, null);
		((CardView) cardView.findViewById(R.id.card)).addView(view);
		cardView.setTag(card);
		if (menuListener != null) {
			cardView.findViewById(R.id.menu_button).setTag(card);
			cardView.findViewById(R.id.menu_button).setOnClickListener(menuListener);
		} else {
			cardView.findViewById(R.id.menu_button).setVisibility(View.GONE);
		}
		return cardView;
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
