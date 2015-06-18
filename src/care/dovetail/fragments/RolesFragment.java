package care.dovetail.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.UserUpdate;
import care.dovetail.common.model.User;

public class RolesFragment extends DialogFragment {
	private static final String TAG = "RolesFragment";

	private App app;
	private List<String> roleNames = new ArrayList<String>();
	private ListView roles;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
		for (String role : getResources().getStringArray(R.array.user_roles)) {
			roleNames.add(role);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_roles, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		roles = (ListView) view.findViewById(R.id.roles);
        roles.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        roles.setAdapter(new RolesAdapter());

        User user = app.getMother();
        if (user.features.containsKey("ROLE")) {
        	for (String role : user.features.get("ROLE").split(",")) {
        		if (role != null && roleNames.indexOf(role.trim()) >= 0) {
        			roles.setItemChecked(roleNames.indexOf(role.trim()), true);
        		}
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
		SparseBooleanArray checked = roles.getCheckedItemPositions();
        ArrayList<String> selectedItems = new ArrayList<String>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                selectedItems.add(roleNames.get(checked.keyAt(i)));
            }
        }
        if (selectedItems.size() > 0) {
	        String roleString = Arrays.toString(selectedItems.toArray(new String[0]));
	        if (roleString.length() > 2) {
	        	roleString = roleString.substring(1, roleString.length() -1);
	        }
	        Log.i(TAG, roleString);
			new UserUpdate(app).execute(Pair.create(UserUpdate.PARAM_FEATURE, "ROLE=" + roleString));
        }
		super.onDismiss(dialog);
	}

	private class RolesAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return roleNames.size();
		}

		@Override
		public String getItem(int position) {
			return roleNames.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CheckedTextView view;
			if (convertView == null) {
				view = (CheckedTextView) getActivity().getLayoutInflater().inflate(
						R.layout.list_item_role, null);
			} else {
				view = (CheckedTextView) convertView;
			}

			view.setText(getItem(position));
			return view;
		}

	}
}