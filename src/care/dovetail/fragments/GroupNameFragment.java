package care.dovetail.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.GroupUpdate;
import care.dovetail.common.model.Group;

public class GroupNameFragment extends DialogFragment {
	public static final String GROUP_ID = "GROUP_ID";

	private App app;
	private String groupId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_group_name, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		groupId = getArguments() == null ? null : getArguments().getString(GROUP_ID);
		if (groupId == null) {
			return;
		}
		for (Group group : app.groups) {
			if (groupId.equalsIgnoreCase(group.uuid)) {
				((TextView) view.findViewById(R.id.name)).setText(group.name);
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	  Dialog dialog = super.onCreateDialog(savedInstanceState);
	  dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	  return dialog;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onDismiss(DialogInterface dialog) {
		String name = ((TextView) getView().findViewById(R.id.name)).getText().toString();
		new GroupUpdate(app, groupId).execute(Pair.create(GroupUpdate.PARAM_NAME, name));
		super.onDismiss(dialog);
	}

}