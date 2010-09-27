package jp.sblo.pandora.aGrep;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

public class TextViewer extends Activity {
    public  static final String EXTRA_LINE = "line";
    public  static final String EXTRA_QUERY = "query";
    public  static final String EXTRA_PATH = "path";

    private ScrollView mScrollView;
    private TextView mEditor;
    private TextLoadTask mTask;
    private String mPatternText;
    private int mLine;
    private Settings.Prefs mPrefs;
    private Pattern mPattern;
    private String mPath;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = Settings.loadPrefes(this);

        setContentView(R.layout.textviewer);

        mScrollView = (ScrollView)findViewById(R.id.scrollview);

        mEditor = (TextView)findViewById(R.id.textedit);

        mEditor.setFocusable(false);
        mEditor.setFocusableInTouchMode(false);

        Intent it = getIntent();
        if (it!=null){
//        	Uri data = it.getData();
//        	String path = Uri.decode( data.getSchemeSpecificPart().substring(2) );		// skip "//"

            Bundle extra = it.getExtras();
            if ( extra!=null ){
                mPath = extra.getString(EXTRA_PATH);
                mPatternText = extra.getString(EXTRA_QUERY);
                mLine = extra.getInt(EXTRA_LINE);

                if ( mPrefs.mIgnoreCase ){
                    mPatternText = mPatternText.toLowerCase();
                    mPattern = Pattern.compile(mPatternText, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE );
                }else{
                    mPattern = Pattern.compile(mPatternText);
                }

                setTitle( mPath + " - aGrep" );
                mTask = new TextLoadTask();
                mTask.execute(mPath);
            }

        }
    }

    class TextLoadTask extends AsyncTask<String, Integer, String>{
        int mOffsetForLine=-1;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params)
        {
            StringBuilder result=new StringBuilder();
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
                            return null;
                        }
                        encode = detector.getCharset();
    //					if ( encode != null ){
    //						android.util.Log.e("aGrep", encode);
    //					}
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

                        int line=0;
                        String text;
                        while(  ( text = br.readLine() )!=null ){
                            line++;
                            if ( line == mLine ){
                                mOffsetForLine = result.length();
                            }
                            result.append( text );
                            result.append( '\n' );
                        }
                        br.close();
                        is.close();
                        return result.toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // android.util.Log.e("jotan" , "end of load" );
                SpannableString ss = new SpannableString(result);

                Matcher m;

                if (mPrefs.mRegularExrpression) {
                    m = mPattern.matcher(result);

                    int start = 0;
                    int end = 0;
                    while (m.find(start)) {
                        start = m.start();
                        end = m.end();

                        // android.util.Log.e("jotan" , "found "+start+","+end
                        // );

                        // SPAN を SpannableString に組み込む。
                        BackgroundColorSpan span = new BackgroundColorSpan(0xFF00FFFF);
                        ss.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        start = end;
                    }
                } else {

                    String temptext = result;
                    if (mPrefs.mIgnoreCase) {
                        temptext = result.toLowerCase();
                    }

                    int start = 0;
                    int end = 0;
                    while ((start = temptext.indexOf(mPatternText, start)) >= 0) {
                        end = start + mPatternText.length();

//                        android.util.Log.e("jotan" , "found "+start+","+end );

                        // SPAN を SpannableString に組み込む。
                        BackgroundColorSpan span = new BackgroundColorSpan(0xFF00FFFF);
                        ss.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        start = end;
                    }

                }
                mEditor.setText(ss);

                mScrollView.post(new Runnable() {
                    public void run() {
                        if (mOffsetForLine > 0) {
                            Layout layout = mEditor.getLayout();
                            int line = layout.getLineForOffset(mOffsetForLine);
                            Rect rect = new Rect();
                            mEditor.getLineBounds(line, rect);
                            int height = mScrollView.getHeight();
                            int pos = rect.top - height / 4;
                            mScrollView.scrollTo(0, pos);
                        }
                    }
                });
                // android.util.Log.e("jotan" , "complete " );
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
