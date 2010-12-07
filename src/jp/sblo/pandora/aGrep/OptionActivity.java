package jp.sblo.pandora.aGrep;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class OptionActivity extends PreferenceActivity implements ColorPickerDialog.OnColorChangedListener {


    final public static int DefaultHighlightColor=0xFF00FFFF;

    private PreferenceScreen mPs = null;
    private PreferenceManager mPm;
    private Settings.Prefs  mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        createHighlightPreference( R.string.label_highlight_bg , true );
        createHighlightPreference( R.string.label_highlight_fg , false );

        setPreferenceScreen(mPs);

    }

    @Override
    public void colorChanged(int fg,int bg) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        sp.edit()
        .putInt(Settings.KEY_HIGHLIGHTFG, fg)
        .putInt(Settings.KEY_HIGHLIGHTBG, bg)
        .commit();
        mPrefs.mHighlightFg = fg;
        mPrefs.mHighlightBg = bg;
    }


    private void createHighlightPreference( final int resid , final boolean bgmode )
    {   // ハイライト色
        final Preference pr = new Preference(this);
//        pr.setSummary();
        pr.setTitle(resid);

        pr.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {

                ColorPickerDialog cpd = new ColorPickerDialog(OptionActivity.this ,OptionActivity.this,
                        mPrefs.mHighlightFg,
                        mPrefs.mHighlightBg,
                        bgmode,
                        getString(resid)) ;
                cpd.show();
                return true;
            }
        });

        mPs.addPreference(pr);
    }


}
