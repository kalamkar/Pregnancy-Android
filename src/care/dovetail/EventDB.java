package care.dovetail;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import care.dovetail.common.model.Event;

public class EventDB extends SQLiteOpenHelper {
	private static final String TAG = "EventDB";

	public static final String DATABASE_NAME = "Events.db";
	public static final int VERSION = 1;
	public static final String EVENTS_TABLE_NAME = "events";
	public static final String EVENTS_TIME = "timeMillis";
	public static final String EVENTS_TYPE = "type";
	public static final String EVENTS_DATA = "data";

	public EventDB(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(String.format("CREATE TABLE %s (%s integer primary key, %s text, %s text)",
				EVENTS_TABLE_NAME, EVENTS_TIME, EVENTS_TYPE, EVENTS_DATA));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", EVENTS_TABLE_NAME));
	    onCreate(db);
	}

	public void reset() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", EVENTS_TABLE_NAME));
	    onCreate(db);
	}

	public void add(Event event) {
	    ContentValues contentValues = new ContentValues();
	    contentValues.put(EVENTS_TIME, event.time);
	    contentValues.put(EVENTS_TYPE, event.type);
	    if (event.data != null) {
	    	contentValues.put(EVENTS_DATA, event.data);
	    }
	    try {
	    	getWritableDatabase().insert(EVENTS_TABLE_NAME, null, contentValues);
	    } catch (Throwable t) {
	    	Log.w(TAG, t);
	    }
	}

	public int size() {
		return (int) DatabaseUtils.queryNumEntries(getReadableDatabase(), EVENTS_TABLE_NAME);
	}

	public List<Event> getAll() {
		return getForQuery(String.format("SELECT * FROM %s", EVENTS_TABLE_NAME));
	}

	public List<Event> getLatest(long timeMillis) {
		return getForQuery(String.format("SELECT * FROM %s WHERE %s > '%s' ORDER BY %s ASC",
				EVENTS_TABLE_NAME, EVENTS_TIME, timeMillis, EVENTS_TIME));
	}

	private List<Event> getForQuery(String query) {
		List<Event> events = new ArrayList<Event>();
		Cursor cursor =  getReadableDatabase().rawQuery(query, null );
		if (cursor == null || cursor.isAfterLast()) {
			return events;
		}
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			events.add(makeEventFromCursor(cursor));
			cursor.moveToNext();
		}
		return events;
	}

	private static Event makeEventFromCursor(Cursor cursor) {
		Event event = new Event(cursor.getString(cursor.getColumnIndex(EVENTS_TYPE)),
				cursor.getLong(cursor.getColumnIndex(EVENTS_TIME)));
		if (cursor.getColumnIndex(EVENTS_DATA) >= 0) {
			event.data = cursor.getString(cursor.getColumnIndex(EVENTS_DATA));
		}
		return event;
	}
}
