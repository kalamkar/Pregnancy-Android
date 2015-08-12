package care.dovetail.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.Utils;
import care.dovetail.api.CardsGet;
import care.dovetail.api.EventsGet;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Card;
import care.dovetail.common.model.Event;
import care.dovetail.common.model.Measurement;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer.GridStyle;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class HistoryFragment extends Fragment {
	private static final String TAG = "HistoryFragment";
	private static final SimpleDateFormat DAY_OF_WEEK = new SimpleDateFormat("EE");
	private static final SimpleDateFormat MONTH_DAY = new SimpleDateFormat("MMM dd");

	private App app;
	private GraphView graph;
	private Card cards[] = new Card[0];

	private BarGraphSeries<DataPoint> dataSeries = new BarGraphSeries<DataPoint>();


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_history, container, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		graph = ((GraphView) view.findViewById(R.id.graph));
		graph.addSeries(dataSeries);
		customizeGraphUI();

		((ListView) view.findViewById(R.id.cards)).setAdapter(new CardsAdapter());

		long endTime = System.currentTimeMillis();
		long startTime = Utils.getMidnightMillis() - Config.GRAPH_DAYS * 24L * 60L * 60L * 1000L;
		new EventsGet(app, Event.Type.STEPS.name(), startTime, endTime) {
			@Override
			protected void onPostExecute(ApiResponse result) {
				super.onPostExecute(result);
				if (result != null && result.events != null) {
					updateGraph(result.events);
				}
			}
		}.execute();

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
		}.execute(Pair.create(CardsGet.PARAM_TAGS, Card.TAGS.ARCHIVED.name()));
	}

	private void updateGraph(Event[] events) {
		long maxY = 0;
		List<DataPoint> dataPoints = new ArrayList<DataPoint>();
		for (Event event : events) {
			try {
				Measurement steps = Config.GSON.fromJson(event.data, Measurement.class);
				DataPoint data = new DataPoint(event.time, steps.value);
				if (!dataPoints.contains(data)) {
					// Add at the beginning as events are sorted descending
					dataPoints.add(0, data);
					maxY = steps.value > maxY ? steps.value : maxY;
				}
			} catch(Exception ex) {
				Log.w(TAG, ex);
			}
		}
		if (dataPoints.size() > 0) {
			graph.getViewport().setXAxisBoundsManual(true);
			graph.getViewport().setMaxX(dataPoints.get(dataPoints.size() -1).getX());
			graph.getViewport().setMinX(dataPoints.get(0).getX());
			if (dataPoints.size() < 7) {

			}
		}
		graph.getViewport().setYAxisBoundsManual(true);
		graph.getViewport().setMaxY(maxY + (int) (maxY * 0.1));

		dataSeries.resetData(dataPoints.toArray(new DataPoint[0]));
	}

	private void customizeGraphUI() {
		int barColor = getResources().getColor(R.color.graph_bar);
		int graphTextColor = getResources().getColor(R.color.graph_text);

		dataSeries.setSpacing(50); // Spacing in percentage of bar width
		dataSeries.setDrawValuesOnTop(true);
		dataSeries.setValuesOnTopColor(graphTextColor);
		dataSeries.setColor(barColor);

		graph.getGridLabelRenderer().setHorizontalLabelsColor(graphTextColor);
		graph.getGridLabelRenderer().setNumHorizontalLabels(
				getResources().getInteger(R.integer.num_graph_labels));
		graph.getGridLabelRenderer().setGridColor(barColor);
		graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
		graph.getGridLabelRenderer().setGridStyle(GridStyle.HORIZONTAL);
		graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
			@Override
			public String formatLabel(double value, boolean isValueX) {
				return isValueX ? DAY_OF_WEEK.format(new Date((long) value))
						: String.format("%.0fk", value / 1000);
			}
		});
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
					getActivity().getLayoutInflater(), null, getResources(), app.imageLoader);
		}
	}
}
