package care.dovetail.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.common.model.Event;

public class NoteFragment extends DialogFragment implements OnClickListener {
	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_note, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.ok).setOnClickListener(this);
		view.findViewById(R.id.cancel).setOnClickListener(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.ok) {
			String text = ((TextView) getView().findViewById(R.id.text)).getText().toString();
			// TODO(abhi): Add extra data in event to set the note text.
			app.events.add(new Event(new String[] {""}, System.currentTimeMillis()));
		} else if (view.getId() == R.id.cancel) {
			dismiss();
		}
	}
}