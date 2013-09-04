package fr.ravenfeld.livewallpaper.video;

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

// Deprecated PreferenceActivity methods are used for API Level 10 (and lower) compatibility
// https://developer.android.com/guide/topics/ui/settings.html#Overview
@SuppressWarnings("deprecation")
public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final ArrayList<String> INCLUDE_EXTENSIONS_LIST = new ArrayList<String>();
	static {
		INCLUDE_EXTENSIONS_LIST.add(".mp4");
		INCLUDE_EXTENSIONS_LIST.add(".3gp");
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
		String file = getPreferenceManager().getSharedPreferences().getString("uri", "");
		if (!file.equalsIgnoreCase("")) {
			String[] files_split = file.split("/");
			mFile.setSummary(getString(R.string.file_summary) + ": " + files_split[files_split.length - 1]);
		}
	}

	private void rendererText() {
		String stringValue = mRendererMode.getValue();
		String string = "";
		if (stringValue.equalsIgnoreCase("classic")) {
			string = getString(R.string.classic);
		} else if (stringValue.equalsIgnoreCase("letter_boxed")) {
			string = getString(R.string.letter_boxed);
		} else if (stringValue.equalsIgnoreCase("stretched")) {
			string = getString(R.string.stretched);
		}

		mRendererMode.setSummary(getString(R.string.renderer_mode_list_summary) + ": " + string);
	}

	private static final int REQUEST_CHOOSER = 1234;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CHOOSER:
			if (resultCode == RESULT_OK) {

				Uri file = data.getData();
				SharedPreferences.Editor prefEditor = getPreferenceManager().getSharedPreferences().edit();
				prefEditor.putString("uri", "" + file);
				prefEditor.commit();
			}
		}
	}
}