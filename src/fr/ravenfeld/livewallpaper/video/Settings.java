package fr.ravenfeld.livewallpaper.video;

import java.io.File;
import java.util.ArrayList;

import rajawali.wallpaper.Wallpaper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;

// Deprecated PreferenceActivity methods are used for API Level 10 (and lower) compatibility
// https://developer.android.com/guide/topics/ui/settings.html#Overview
@SuppressWarnings("deprecation")
public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final ArrayList<String> INCLUDE_EXTENSIONS_LIST = new ArrayList<String>();
	static {
		INCLUDE_EXTENSIONS_LIST.add(".mp4");
		INCLUDE_EXTENSIONS_LIST.add(".3gp");
		INCLUDE_EXTENSIONS_LIST.add(".MP4");
		INCLUDE_EXTENSIONS_LIST.add(".3GP");
	}
	private Preference mFile;
	private ListPreference mRendererMode;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// setContentView(R.layout.pref);
		getPreferenceManager().setSharedPreferencesName(Wallpaper.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.settings);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		mFile = findPreference("file");
		mRendererMode = (ListPreference) findPreference("rendererMode");

		fileText();
		rendererText();

		mFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {

				Intent mainIntent = new Intent(Settings.this, FileChooserActivity.class);
				mainIntent.putStringArrayListExtra(FileChooserActivity.EXTRA_FILTER_INCLUDE_EXTENSIONS, INCLUDE_EXTENSIONS_LIST);

				startActivityForResult(mainIntent, REQUEST_CHOOSER);

				return false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		fileText();
		rendererText();
	}

	private void fileText() {
		String uri = getPreferenceManager().getSharedPreferences().getString(
				"uri", "");
		File file = FileUtils.getFile(Uri.parse(uri));
		if (file == null || !file.isFile()) {
			uri = "";
			setUriPreference(uri);
		}
		if (!uri.equalsIgnoreCase("")) {
			String[] uri_split = uri.split("/");
			mFile.setSummary(getString(R.string.file_summary) + ": "
					+ uri_split[uri_split.length - 1]);
		} else {
			mFile.setSummary(getString(R.string.file_summary) + ": ");
		}
	}

	private void rendererText() {
		mRendererMode.setSummary(getString(R.string.renderer_mode_list_summary) + ": " + mRendererMode.getEntry());
	}

	private void setUriPreference(String uri) {
		SharedPreferences.Editor prefEditor = getPreferenceManager()
				.getSharedPreferences().edit();
		prefEditor.putString("uri", "" + uri);
		prefEditor.commit();
	}
	private static final int REQUEST_CHOOSER = 1234;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CHOOSER:
			if (resultCode == RESULT_OK) {
				Uri file = data.getData();
				setUriPreference(file.toString());
			}
		}
	}
}