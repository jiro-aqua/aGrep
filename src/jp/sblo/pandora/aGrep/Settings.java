package jp.sblo.pandora.aGrep;

import java.util.ArrayList;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Settings extends Activity {

    private static final String KEY_IGNORE_CASE = "IgnoreCase";
    private static final String KEY_REGULAR_EXPRESSION = "RegularExpression";
    private static final String KEY_TARGET_EXTENSIONS_OLD = "TargetExtensions";
    private static final String KEY_TARGET_DIRECTORIES_OLD = "TargetDirectories";
    private static final String KEY_TARGET_EXTENSIONS_NEW = "TargetExtensionsNew";
    private static final String KEY_TARGET_DIRECTORIES_NEW = "TargetDirectoriesNew";
    public static final String KEY_FONTSIZE = "FontSize";
    public static final String KEY_HIGHLIGHTFG = "HighlightFg";
    public static final String KEY_HIGHLIGHTBG = "HighlightBg";
    public static final String KEY_ADD_LINENUMBER = "AddLineNumber";

    private static final String PACKAGE_NAME = "jp.sblo.pandora.aGrep";
    final static int REQUEST_CODE_ADDDIC = 0x1001;

    public static class checkedString {
        boolean checked;
        String string;

        public checkedString(String _s){
            this(true,_s);
        }
        public checkedString(boolean _c,String _s){
            checked = _c;
            string = _s;
        }
        public String toString(){
            return (checked?"true":"false") + "|" + string;
        }

    };

    public static class Prefs {
        ArrayList<checkedString> mDirList = new ArrayList<checkedString>();
        ArrayList<checkedString> mExtList = new ArrayList<checkedString>();
        boolean mRegularExrpression = false;
        boolean mIgnoreCase = true;
        int mFontSize = 16;
        int mHighlightBg = 0xFF00FFFF;
        int mHighlightFg = 0xFF000000;
        boolean addLineNumber=false;
    };

    private Prefs mPrefs;
    private LinearLayout mDirListView;
    private LinearLayout mExtListView;
    private View.OnLongClickListener mDirListener;
    private View.OnLongClickListener mExtListener;
    private CompoundButton.OnCheckedChangeListener mCheckListener;

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
                final checkedString strItem = (checkedString) view.getTag();
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
                final String strText = (String) ((TextView)view).getText();
                final checkedString strItem = (checkedString) view.getTag();
                // Show Dialog
                new AlertDialog.Builder(Settings.this)
                .setTitle(R.string.label_remove_item_title)
                .setMessage( getString(R.string.label_remove_item , strText ) )
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

        mCheckListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final checkedString strItem = (checkedString) buttonView.getTag();
                strItem.checked = isChecked;
                savePrefs(mPrefs);
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
                            for( checkedString t : mPrefs.mExtList ){
                                if ( t.string.equalsIgnoreCase(ext)){
                                    return;
                                }
                            }
                            mPrefs.mExtList.add(new checkedString(ext));
                            refreshExtList();
                            savePrefs(mPrefs);
                        }
                    }
                })
                .setNeutralButton(R.string.label_no_extension, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* 拡張子無しボタンをクリックした時の処理 */

                        String ext = "*";
                        // 二重チェック
                        for( checkedString t : mPrefs.mExtList ){
                            if ( t.string.equalsIgnoreCase(ext)){
                                return;
                            }
                        }
                        mPrefs.mExtList.add(new checkedString(ext));
                        refreshExtList();
                        savePrefs(mPrefs);
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

        final AutoCompleteTextView edittext = (AutoCompleteTextView) findViewById(R.id.EditText01);
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

        ImageButton historyBtn = (ImageButton) findViewById(R.id.ButtonHistory);
        historyBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view)
            {
                edittext.showDropDown();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // ディレクトリ選択画面からの応答
        if (requestCode == REQUEST_CODE_ADDDIC && resultCode == RESULT_OK && data != null) {
            final String dirname = data.getExtras().getString(FileSelectorActivity.INTENT_FILEPATH );
            if (dirname != null && dirname.length()>0 ) {
                // 二重チェック
                for( checkedString t : mPrefs.mDirList ){
                    if ( t.string.equalsIgnoreCase(dirname)){
                        return;
                    }
                }
                mPrefs.mDirList.add(new checkedString(dirname));
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
        for( checkedString t : prefs.mDirList ){
            dirs.append(t.checked);
            dirs.append('|');
            dirs.append(t.string);
            dirs.append('|');
        }
        if ( dirs.length() > 0 ){
            dirs.deleteCharAt(dirs.length()-1);
        }

        // target extensions
        StringBuilder exts = new StringBuilder();
        for( checkedString t : prefs.mExtList ){
            exts.append(t.checked);
            exts.append('|');
            exts.append(t.string);
            exts.append('|');
        }
        if ( exts.length() > 0 ){
            exts.deleteCharAt(exts.length()-1);
        }

        editor.putString(KEY_TARGET_DIRECTORIES_NEW, dirs.toString() );
        editor.putString(KEY_TARGET_EXTENSIONS_NEW, exts.toString() );
        editor.remove(KEY_TARGET_DIRECTORIES_OLD);
        editor.remove(KEY_TARGET_EXTENSIONS_OLD);
        editor.putBoolean(KEY_REGULAR_EXPRESSION, prefs.mRegularExrpression );
        editor.putBoolean(KEY_IGNORE_CASE, prefs.mIgnoreCase );

        editor.commit();
    }

    static public Prefs loadPrefes(Context ctx)
    {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        Prefs prefs = new Prefs();

        // target directory
        String dirs = sp.getString(KEY_TARGET_DIRECTORIES_NEW,"" );
        prefs.mDirList	=  new ArrayList<checkedString>();
        if ( dirs.length()>0 ){
            String[] dirsarr = dirs.split("\\|");
            int size = dirsarr.length;
            for( int i=0;i<size;i+=2 ){
                boolean c = dirsarr[i].equals("true");
                String s = dirsarr[i+1];
                prefs.mDirList.add(new checkedString(c,s));
            }
        }else{
            dirs = sp.getString(KEY_TARGET_DIRECTORIES_OLD,"" );
            if ( dirs.length()>0 ){
                String[] dirsarr = dirs.split("\\|");
                int size = dirsarr.length;
                for( int i=0;i<size;i++ ){
                    prefs.mDirList.add(new checkedString(dirsarr[i]));
                }
            }
        }
        // target extensions
        String exts = sp.getString(KEY_TARGET_EXTENSIONS_NEW,"" );
        prefs.mExtList	=  new ArrayList<checkedString>();
        if ( exts.length()>0 ){
            String[] arr = exts.split("\\|");
            int size = arr.length;
            for( int i=0;i<size;i+=2 ){
                boolean c = arr[i].equals("true");
                String s = arr[i+1];
                prefs.mExtList.add(new checkedString(c,s));
            }
        }else{
            exts = sp.getString(KEY_TARGET_EXTENSIONS_OLD,"txt" );
            if ( exts.length()>0 ){
                String[] arr = exts.split("\\|");
                int size = arr.length;
                for( int i=0;i<size;i++ ){
                    prefs.mExtList.add(new checkedString(arr[i]));
                }
            }
        }

        prefs.mRegularExrpression = sp.getBoolean(KEY_REGULAR_EXPRESSION, false );
        prefs.mIgnoreCase = sp.getBoolean(KEY_IGNORE_CASE, true );

        prefs.mFontSize = Integer.parseInt( sp.getString( KEY_FONTSIZE , "-1" ) );
        prefs.mHighlightFg = sp.getInt( KEY_HIGHLIGHTFG , 0xFF000000 );
        prefs.mHighlightBg = sp.getInt( KEY_HIGHLIGHTBG , 0xFF00FFFF );

        prefs.addLineNumber = sp.getBoolean(KEY_ADD_LINENUMBER, false);
        return prefs;
    }

    void setListItem( LinearLayout view ,
            ArrayList<checkedString> list ,
            View.OnLongClickListener logclicklistener ,
            CompoundButton.OnCheckedChangeListener checkedChangeListener )
    {
        view.removeAllViews();
        Collections.sort(list, new Comparator<checkedString>() {
            @Override
            public int compare(checkedString object1, checkedString object2) {
                return object1.string.compareToIgnoreCase(object2.string);
            }
        });
        for( checkedString s : list ){
            CheckBox v = (CheckBox)View.inflate(this, R.layout.list_dir, null);
            if ( s.equals("*") ){
                v.setText(R.string.label_no_extension);
            }else{
                v.setText(s.string);
            }
            v.setChecked( s.checked );
            v.setTag(s);
            v.setOnLongClickListener(logclicklistener);
            v.setOnCheckedChangeListener(checkedChangeListener);
            view.addView(v);
        }
    }

    private void refreshDirList(){
        setListItem( mDirListView , mPrefs.mDirList , mDirListener , mCheckListener);
    }
    private void refreshExtList(){
        setListItem( mExtListView , mPrefs.mExtList , mExtListener , mCheckListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == R.id.menu_option ){
            Intent intent = new Intent( this ,  OptionActivity.class );
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
