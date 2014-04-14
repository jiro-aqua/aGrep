package jp.sblo.pandora.aGrep;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

public class OptionActivity extends PreferenceActivity  {


    final public static int DefaultHighlightColor=0xFF00FFFF;

    private PreferenceScreen mPs = null;
    private PreferenceManager mPm;
    private Settings.Prefs  mPrefs;

    final private static int REQUEST_CODE_HIGHLIGHT = 0x1000;
    final private static int REQUEST_CODE_BACKGROUND = 0x1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled (true);

        mPrefs = Settings.loadPrefes(this);

        mPm = getPreferenceManager();
        mPs = mPm.createPreferenceScreen(this);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        {
            // フォントサイズ
            final ListPreference pr = new ListPreference(this);
            pr.setKey(Settings.KEY_FONTSIZE);
            pr.setSummary(sp.getString(pr.getKey(), ""));
            pr.setTitle(R.string.label_font_size);
            pr.setEntries(new String[] { "10", "14", "16", "18", "20", "24", "30", "36",  });
            pr.setEntryValues(new String[] { "10", "14", "16", "18", "20", "24", "30", "36",  });
            mPs.addPreference(pr);
        }
        createHighlightPreference( R.string.label_highlight_bg , REQUEST_CODE_HIGHLIGHT );
        createHighlightPreference( R.string.label_highlight_fg , REQUEST_CODE_BACKGROUND );

        {
            // Add Linenumber
            final CheckBoxPreference pr = new CheckBoxPreference(this);
            pr.setKey(Settings.KEY_ADD_LINENUMBER);
            pr.setSummary(R.string.summary_add_linenumber);
            pr.setTitle(R.string.label_add_linenumber);
            mPs.addPreference(pr);
        }

        {
            final Preference pr = new Preference(this);
            // set Version Name to title field
            try {
                pr.setTitle(getString(R.string.version, getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName));
            } catch (NameNotFoundException e) {
            }
            pr.setSummary(R.string.link);
            mPs.addPreference(pr);
        }
        setPreferenceScreen(mPs);

    }


    private void createHighlightPreference( final int resid , final int reqCode )
    {
        final Preference pr = new Preference(this);
        pr.setTitle(resid);

        pr.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent intent = new Intent( OptionActivity.this , ColorPickerActivity.class );
                intent.putExtra(ColorPickerActivity.EXTRA_TITLE, getString(resid));
                startActivityForResult(intent, reqCode);
                return true;
            }
        });

        mPs.addPreference(pr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode == RESULT_OK ){
            int color = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR, 0x00FFFF);
            if ( requestCode == REQUEST_CODE_HIGHLIGHT ){
                mPrefs.mHighlightFg = color;
            }else if ( requestCode == REQUEST_CODE_BACKGROUND ){
                mPrefs.mHighlightBg = color;
            }
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.edit()
            .putInt(Settings.KEY_HIGHLIGHTFG, mPrefs.mHighlightFg)
            .putInt(Settings.KEY_HIGHLIGHTBG, mPrefs.mHighlightBg)
            .commit();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
