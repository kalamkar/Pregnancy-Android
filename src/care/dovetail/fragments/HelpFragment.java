package care.dovetail.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import care.dovetail.App;

public class HelpFragment extends Fragment {
	private static final String TAG = "HelpFragment";

	private static final String HELP_URL = "http://www.pregnansi.com/help/app";

	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return new WebView(app);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		WebView webView = (WebView) view;

		// getActivity().getWindow().requestFeature(Window.FEATURE_PROGRESS);
		// webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				// getActivity().setProgress(progress * 1000);
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode, String description,
										String failingUrl) {
				Log.w(TAG, description);
			}
		});

		webView.loadUrl(HELP_URL);
	}

	@Override
	public void onResume() {
		((WebView) getView()).onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		((WebView) getView()).onPause();
		super.onPause();
	}
}