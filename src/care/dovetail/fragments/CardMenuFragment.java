package care.dovetail.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.CardUpdate;
import care.dovetail.common.model.Card;

public class CardMenuFragment extends DialogFragment {
	private static final String TAG = "CardMenuFragment";

	private static final int[] GENERIC_MENU = new int[] {R.string.archive,
		R.string.add_to_favorites, R.string.share, R.string.learn_more};

	private App app;

	private Card card;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_card_menu, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        ((ListView) view).setAdapter(new MenuAdapter(GENERIC_MENU));
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	  Dialog dialog = super.onCreateDialog(savedInstanceState);
	  dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	  return dialog;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	private class MenuAdapter extends BaseAdapter implements OnClickListener {
		private final int[] itemIds;

		private MenuAdapter(int[] itemIds) {
			this.itemIds = itemIds;
		}

		@Override
		public int getCount() {
			return itemIds.length;
		}

		@Override
		public String getItem(int position) {
			return getResources().getString(itemIds[position]);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getActivity().getLayoutInflater().inflate(
						android.R.layout.simple_list_item_1, null);
				view.setOnClickListener(this);
			} else {
				view = convertView;
			}

			((TextView) view.findViewById(android.R.id.text1)).setText(getItem(position));
			view.setTag(position);
			return view;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onClick(View view) {
			switch(itemIds[(Integer) view.getTag()]) {
			case R.string.archive:
				List<Card> cards = new ArrayList<Card>();
				for (Card cardToAdd : app.getMother().cards) {
					if (!cardToAdd.equals(card)) {
						cards.add(cardToAdd);
					}
				}
				app.getMother().cards = cards.toArray(new Card[0]);
				app.setUser(app.getMother());
				new CardUpdate(app).execute(Pair.create(CardUpdate.PARAM_CARD_ID, card.id),
						Pair.create(CardUpdate.PARAM_TAG, Card.TAGS.ARCHIVED.name()));
				break;
			case R.string.add_to_favorites:
				break;
			case R.string.share:
				break;
			case R.string.learn_more:
				break;
			}
			dismiss();
		}

	}
}