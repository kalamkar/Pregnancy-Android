package care.dovetail.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.CardsGet;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Card;

public class BrowseFragment extends Fragment implements OnClickListener {
	private static final String TAG = "BrowseFragment";

	private App app;
	private Card cards[] = new Card[0];
	private int week;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
		week = app.getMother().getPregnancyWeek() + 1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_browse, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.prev).setOnClickListener(this);
		view.findViewById(R.id.next).setOnClickListener(this);

		((ListView) view.findViewById(R.id.cards)).setAdapter(new CardsAdapter());
		requestCards(week);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.prev:
			requestCards(--week);
			break;
		case R.id.next:
			requestCards(++week);
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void requestCards(int week) {
		week = week < 0 ? 1 : week > 40 ? 40 : week;
		// We don't have content for week 2, 3 and 4 so skip them
		week = week > 1 && week < 5 ? 5 : week;
		((TextView) getView().findViewById(R.id.title)).setText(
				String.format(getResources().getString(R.string.week_number), week));
		new CardsGet(app) {
			@Override
			protected void onPostExecute(ApiResponse result) {
				super.onPostExecute(result);
				if (result != null && result.cards != null) {
					cards = result.cards;
					((BaseAdapter) ((ListView) getView().findViewById(R.id.cards)).getAdapter())
							.notifyDataSetChanged();
				}
			}
		}.execute(Pair.create(CardsGet.PARAM_TAGS, String.format("week:%d", week)),
				  Pair.create(CardsGet.PARAM_PUBLIC, "true"));
	}

	private class CardsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return cards.length;
		}

		@Override
		public Card getItem(int position) {
			return cards[position];
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return CardUtils.getViewForCard(getItem(position), null, null,
					getActivity().getLayoutInflater(), null, app);
		}
	}
}