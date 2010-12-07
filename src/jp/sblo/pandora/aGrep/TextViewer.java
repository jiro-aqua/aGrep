package jp.sblo.pandora.aGrep;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TextViewer extends Activity {
    public  static final String EXTRA_LINE = "line";
    public  static final String EXTRA_QUERY = "query";
    public  static final String EXTRA_PATH = "path";

    private TextLoadTask mTask;
    private String mPatternText;
    private int mLine;
    private Settings.Prefs mPrefs;
    private String mPath;
    private TextPreview mTextPreview;
    ArrayList<CharSequence> mData = new ArrayList<CharSequence>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = Settings.loadPrefes(getApplicationContext());

        setContentView(R.layout.textviewer);

        mTextPreview = (TextPreview)findViewById(R.id.TextPreview);



        Intent it = getIntent();
        if (it!=null){

            Bundle extra = it.getExtras();
            if ( extra!=null ){
                mPath = extra.getString(EXTRA_PATH);
                mPatternText = extra.getString(EXTRA_QUERY);
                mLine = extra.getInt(EXTRA_LINE);

                if ( !mPrefs.mRegularExrpression ){
                    mPatternText = Search.escapeMetaChar(mPatternText);
                }

                setTitle( mPath + " - aGrep" );
                mTask = new TextLoadTask();
                mTask.execute(mPath);
            }

        }
    }

    class TextLoadTask extends AsyncTask<String, Integer, Boolean >{
        int mOffsetForLine=-1;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... params)
        {
            File f = new File(params[0]);
            if ( f.exists() ){

                InputStream is;
                try {
                    is = new BufferedInputStream( new FileInputStream( f ) , 65536 );
                    is.mark(65536);

                    String encode = null;
                    //  文字コードの判定
                    try{
                        UniversalDetector detector = new UniversalDetector();
                        try{
                            int nread;
                            byte[] buff = new byte[4096];
                            if ((nread = is.read(buff)) > 0 ) {
                                detector.handleData(buff, 0, nread);
                            }
                            detector.dataEnd();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                        encode = detector.getCharset();
                        is.reset();
                        detector.reset();
                        detector.destroy();
                    }
                    catch( UniversalDetector.DetectorException e ){
                    }
                    BufferedReader br=null;
                    try {
                        if ( encode != null ){
                            br = new BufferedReader( new InputStreamReader( is , encode ) , 8192 );
                        }else{
                            br = new BufferedReader( new InputStreamReader( is ) , 8192 );
                        }

                        String text;
                        while(  ( text = br.readLine() )!=null ){
                            mData.add( text );

                        }
                        br.close();
                        is.close();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result ) {

                TextPreview.Adapter adapter = new TextPreview.Adapter(getApplicationContext(),  R.layout.textpreview_row, R.id.TextPreview, mData);
                mData = null;

                Pattern pattern;

                if ( mPrefs.mIgnoreCase ){
                    pattern = Pattern.compile(mPatternText, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE );
                }else{
                    pattern = Pattern.compile(mPatternText);
                }

                adapter.setFormat(pattern , mPrefs.mHighlightFg , mPrefs.mHighlightBg , mPrefs.mFontSize );
                mTextPreview.setAdapter( adapter );

                int height = mTextPreview.getHeight();
                mTextPreview.setSelectionFromTop( mLine-1 , height / 4 );
                adapter = null;
                mTextPreview = null;
                mTask = null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;

    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.menu_viewer) {
            // ビュワー呼び出し
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + mPath), "text/plain");
            startActivity(intent);
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
}
