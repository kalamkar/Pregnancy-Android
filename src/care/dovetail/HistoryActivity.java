package care.dovetail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.api.EventsGet;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Event;
import care.dovetail.common.model.Measurement;
import care.dovetail.common.model.Tip;

import com.android.volley.toolbox.NetworkImageView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer.GridStyle;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class HistoryActivity extends FragmentActivity {
	private static final String TAG = "HistoryActivity";
	private static final SimpleDateFormat DAY_OF_WEEK = new SimpleDateFormat("EE");
	private static final SimpleDateFormat MONTH_DAY = new SimpleDateFormat("MMM dd");

	private App app;
	private GraphView graph;
	private List<Tip> tips = new ArrayList<Tip>();

	private BarGraphSeries<DataPoint> dataSeries = new BarGraphSeries<DataPoint>();


	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

		app = (App) getApplication();

		graph = ((GraphView) findViewById(R.id.graph));
		graph.addSeries(dataSeries);
		customizeGraphUI();

		((ListView) findViewById(R.id.cards)).setAdapter(new CardsAdapter());

		long endTime = System.currentTimeMillis();
		long startTime = Utils.getMidnightMillis() - 7L * 24L * 60L * 60L * 1000L;
		new EventsGet(app, Event.Type.STEPS.name(), startTime, endTime) {
			@Override
			protected void onPostExecute(ApiResponse result) {
				super.onPostExecute(result);
				if (result != null && result.events != null) {
					updateGraph(result.events);
				}
			}
		}.execute();

		new EventsGet(app, Event.Type.CARD_ARCHIVED.name(), startTime, endTime) {
			@Override
			protected void onPostExecute(ApiResponse result) {
				super.onPostExecute(result);
				if (result != null && result.events != null) {
					updateCards(result.events);
				}
			}
		}.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateGraph(Event[] events) {
		long maxY = 0;
		List<DataPoint> dataPoints = new ArrayList<DataPoint>();
		for (Event event : events) {
			try {
				Measurement steps = Config.GSON.fromJson(event.data, Measurement.class);
				if (steps != null) {
					// Add at the beginning as events are sorted descending
					dataPoints.add(0, new DataPoint(event.time, steps.value));
					maxY = steps.value > maxY ? steps.value : maxY;
				}
			} catch(Exception ex) {
				Log.w(TAG, ex);
			}
		}
		if (dataPoints.size() > 0) {
			graph.getViewport().setMaxX(dataPoints.get(dataPoints.size() -1).getX());
			graph.getViewport().setMinX(dataPoints.get(0).getX());
			graph.getViewport().setMaxY(maxY + (int) (maxY * 0.1));
		}
		dataSeries.resetData(dataPoints.toArray(new DataPoint[0]));
	}

	private void updateCards(Event[] events) {
		tips.clear();
		for (int i = 0; i < events.length; i++) {
			try {
				Tip tip = Config.GSON.fromJson(events[i].data, Tip.class);
				tips.add(tip);
			} catch(Exception ex) {
				Log.w(TAG, ex);
			}
		}
		((BaseAdapter) ((ListView) findViewById(R.id.cards)).getAdapter()).notifyDataSetChanged();
	}

	private void customizeGraphUI() {
		int barColor = getResources().getColor(R.color.graph_bar);
		int graphTextColor = getResources().getColor(R.color.graph_text);

		dataSeries.setSpacing(getResources().getDimensionPixelOffset(R.dimen.small_margin));
		dataSeries.setDrawValuesOnTop(true);
		dataSeries.setValuesOnTopColor(graphTextColor);
		dataSeries.setColor(barColor);

		graph.getGridLabelRenderer().setHorizontalLabelsColor(graphTextColor);
		graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
		graph.getGridLabelRenderer().setGridStyle(GridStyle.HORIZONTAL);
		graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
			@Override
			public String formatLabel(double value, boolean isValueX) {
				return isValueX ? DAY_OF_WEEK.format(new Date((long) value))
						: String.format("%.0fk", value / 1000);
			}
		});
		graph.getViewport().setXAxisBoundsManual(true);
		graph.getViewport().setYAxisBoundsManual(true);
	}

	private class CardsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return tips.size();
		}

		@Override
		public Tip getItem(int position) {
			return tips.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.list_item_card, null);
				view.findViewById(R.id.hint).setVisibility(View.GONE);
				view.findViewById(R.id.time).setVisibility(View.GONE);
				view.findViewById(R.id.menu_button).setVisibility(View.GONE);
				view.findViewById(R.id.menu).setVisibility(View.INVISIBLE);
			} else {
				view = convertView;
			}

			Tip tip = getItem(position);
			String iconUrl = getIcon(tip);
			if (iconUrl == null) {
				view.findViewById(R.id.icon).setVisibility(View.GONE);
			} else {
				view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
				((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
						iconUrl, app.imageLoader);
			}
			((TextView) view.findViewById(R.id.title)).setText(tip.title);
			return view;
		}
	}

	private String getIcon(Tip tip) {
		if (tip == null || tip.tags == null) {
			return null;
		}
		for (String tag : tip.tags) {
			if (tag != null && tag.toLowerCase().startsWith("image")) {
				return tag.replaceFirst("image:", "");
			}
		}
		return null;
	}
}
