package jp.sblo.pandora.aGrep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Settings extends Activity {

    private static final String KEY_IGNORE_CASE = "IgnoreCase";
    private static final String KEY_REGULAR_EXPRESSION = "RegularExpression";
    private static final String KEY_TARGET_EXTENSIONS = "TargetExtensions";
    private static final String KEY_TARGET_DIRECTORIES = "TargetDirectories";
    private static final String PACKAGE_NAME = "jp.sblo.pandora.aGrep";
    final static int REQUEST_CODE_ADDDIC = 0x1001;

    public static class Prefs {
        ArrayList<String> mDirList = new ArrayList<String>();
        ArrayList<String> mExtList = new ArrayList<String>();
        boolean mRegularExrpression = false;
        boolean mIgnoreCase = true;
    };

    private Prefs mPrefs;
    private LinearLayout mDirListView;
    private LinearLayout mExtListView;
    private View.OnLongClickListener mDirListener;
    private View.OnLongClickListener mExtListener;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = loadPrefes(this);

        setContentView(R.layout.main);

        // set Version Name to title field
        try {
            setTitle( getString( R.string.hello , getPackageManager().getPackageInfo( PACKAGE_NAME, 0).versionName  ));
        } catch (NameNotFoundException e) {
        }

        mDirListView = (LinearLayout)findViewById(R.id.listdir);
        mExtListView = (LinearLayout)findViewById(R.id.listext);

        mDirListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                final String strItem = (String) view.getTag();
                // Show Dialog
                new AlertDialog.Builder(Settings.this)
                .setTitle(R.string.label_remove_item_title)
                .setMessage( getString(R.string.label_remove_item , strItem ) )
                .setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mPrefs.mDirList.remove(strItem);
                        refreshDirList();
                        savePrefs(mPrefs);
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, null )
                .setCancelable(true)
                .show();
                return true;
            }
        };

        mExtListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                final String strItem = (String) view.getTag();
                // Show Dialog
                new AlertDialog.Builder(Settings.this)
                .setTitle(R.string.label_remove_item_title)
                .setMessage( getString(R.string.label_remove_item , strItem ) )
                .setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mPrefs.mExtList.remove(strItem);
                        refreshExtList();
                        savePrefs(mPrefs);
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, null )
                .setCancelable(true)
                .show();
                return true;
            }
        };

        refreshDirList();
        refreshExtList();

        ImageButton btnAddDir = (ImageButton) findViewById(R.id.adddir);
        ImageButton btnAddExt = (ImageButton) findViewById(R.id.addext);

        btnAddDir.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                // ファイル選択画面呼び出し
                Intent intent = new Intent( Settings.this , FileSelectorActivity.class );
                startActivityForResult(intent, REQUEST_CODE_ADDDIC);
            }
        });

        btnAddExt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                // Create EditText
                final EditText edtInput = new EditText(Settings.this);
                edtInput.setSingleLine();
                // Show Dialog
                new AlertDialog.Builder(Settings.this)
                .setTitle(R.string.label_addext)
                .setView(edtInput)
                .setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* OKボタンをクリックした時の処理 */

                        String ext = edtInput.getText().toString();
                        if (ext != null && ext.length()>0 ) {
                            // 二重チェック
                            for( String t : mPrefs.mExtList ){
                                if ( t.equalsIgnoreCase(ext)){
                                    return;
                                }
                            }
                            mPrefs.mExtList.add(ext);
                            refreshExtList();
                            savePrefs(mPrefs);
                        }
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, null )
                .setCancelable(true)
                .show();
            }
        });


        final CheckBox chkRe = (CheckBox)findViewById(R.id.checkre);
        final CheckBox chkIc = (CheckBox)findViewById(R.id.checkignorecase);

        chkRe.setChecked(mPrefs.mRegularExrpression);
        chkIc.setChecked(mPrefs.mIgnoreCase);

        chkRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mPrefs.mRegularExrpression = chkRe.isChecked();
                savePrefs(mPrefs);
            }
        });
        chkIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mPrefs.mIgnoreCase = chkIc.isChecked();
                savePrefs(mPrefs);
            }
        });

        final EditText edittext = (EditText) findViewById(R.id.EditText01);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP ) {
                    String text = edittext.getEditableText().toString();
                    Intent it = new Intent(Settings.this,Search.class);
                    it.setAction(Intent.ACTION_SEARCH);
                    it.putExtra(SearchManager.QUERY,text );
                    startActivity( it );
                    return true;
                }
                return false;
            }
        });
        edittext.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        ImageButton clrBtn = (ImageButton) findViewById(R.id.ButtonClear);
        clrBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view)
            {
                edittext.setText("");
                edittext.requestFocus();
            }
        });

        ImageButton searchBtn = (ImageButton) findViewById(R.id.ButtonSearch);
        searchBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view)
            {
                String text = edittext.getText().toString();
                Intent it = new Intent(Settings.this,Search.class);
                it.setAction(Intent.ACTION_SEARCH);
                it.putExtra(SearchManager.QUERY,text );
                startActivity( it );
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // 辞書選択画面からの応答
        if (requestCode == REQUEST_CODE_ADDDIC && resultCode == RESULT_OK && data != null) {
            final String dirname = data.getExtras().getString(FileSelectorActivity.INTENT_FILEPATH );
            if (dirname != null && dirname.length()>0 ) {
                // 二重チェック
                for( String t : mPrefs.mDirList ){
                    if ( t.equalsIgnoreCase(dirname)){
                        return;
                    }
                }
                mPrefs.mDirList.add(dirname);
                refreshDirList();
                savePrefs(mPrefs);
            }
        }

    }

    private void savePrefs(Prefs prefs)
    {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        Editor editor = sp.edit();

        // target directory
        StringBuilder dirs = new StringBuilder();
        for( String t : prefs.mDirList ){
            dirs.append(t);
            dirs.append('|');
        }
        if ( dirs.length() > 0 ){
            dirs.deleteCharAt(dirs.length()-1);
        }

        // target extensions
        StringBuilder exts = new StringBuilder();
        for( String t : prefs.mExtList ){
            exts.append(t);
            exts.append('|');
        }
        if ( exts.length() > 0 ){
            exts.deleteCharAt(exts.length()-1);
        }

        editor.putString(KEY_TARGET_DIRECTORIES, dirs.toString() );
        editor.putString(KEY_TARGET_EXTENSIONS, exts.toString() );
        editor.putBoolean(KEY_REGULAR_EXPRESSION, prefs.mRegularExrpression );
        editor.putBoolean(KEY_IGNORE_CASE, prefs.mIgnoreCase );

        editor.commit();
    }

    static public Prefs loadPrefes(Context ctx)
    {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        Prefs prefs = new Prefs();

        // target directory
        String dirs = sp.getString(KEY_TARGET_DIRECTORIES,"" );
        if ( dirs.length()>0 ){
            String[] dirsarr = dirs.split("\\|");
            prefs.mDirList	=  new ArrayList<String>( Arrays.asList(dirsarr) );
        }
        // target extensions
        String exts = sp.getString(KEY_TARGET_EXTENSIONS,"txt" );
        if ( exts.length()>0 ){
            String[] extsarr = exts.split("\\|");
            prefs.mExtList	=  new ArrayList<String>( Arrays.asList(extsarr) );
        }

        prefs.mRegularExrpression = sp.getBoolean(KEY_REGULAR_EXPRESSION, false );
        prefs.mIgnoreCase = sp.getBoolean(KEY_IGNORE_CASE, true );

        return prefs;
    }

    void setListItem( LinearLayout view , ArrayList<String> list , View.OnLongClickListener logclicklistener )
    {
        view.removeAllViews();
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String object1, String object2) {
                return object1.compareToIgnoreCase(object2);
            }
        });
        for( String s : list ){
            TextView v = (TextView)View.inflate(this, R.layout.list_dir, null);
            v.setText(s);
            v.setTag(s);
            v.setOnLongClickListener(logclicklistener);
            view.addView(v);
        }
    }

    private void refreshDirList(){
        setListItem( mDirListView , mPrefs.mDirList , mDirListener );
    }
    private void refreshExtList(){
        setListItem( mExtListView , mPrefs.mExtList , mExtListener );
    }


}
