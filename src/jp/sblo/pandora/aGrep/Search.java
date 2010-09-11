package jp.sblo.pandora.aGrep;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.widget.Toast;



public class Search extends Activity implements GrepView.Callback
{

    private Activity mActivity;
    private GrepView mGrepView;
    private GrepView.GrepAdapter mAdapter;
    private ArrayList<GrepView.Data>	mData ;
    private GrepTask mTask;
    private String mQuery;

    private Settings.Prefs mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mPrefs = Settings.loadPrefes(this);

        setContentView(R.layout.result);

        mActivity = this;

        if ( mPrefs.mDirList.size() == 0 ) {
            Toast.makeText(this,R.string.label_no_target_dir, Toast.LENGTH_LONG).show();
            startActivity( new Intent(this,Settings.class) );
            finish();
        }

        mGrepView = (GrepView)findViewById(R.id.DicView01);
        mData = new ArrayList<GrepView.Data>();
        mAdapter = new GrepView.GrepAdapter(mActivity, R.layout.list_row, R.id.DicView01, mData);
        mGrepView.setAdapter( mAdapter );
        mGrepView.setCallback(this);

        Intent it = getIntent();

        if (it != null &&
            Intent.ACTION_SEARCH.equals(it.getAction()) )
        {
            Bundle extras = it.getExtras();
            mQuery = extras.getString(SearchManager.QUERY);

            if ( mQuery!=null && mQuery.length() >0 ){
                mTask = new GrepTask();
                mTask.execute(mQuery);
            }else{
                finish();
            }
        }
    }

    class GrepTask extends AsyncTask<String, Integer, Boolean>
    {
        private ProgressDialog mProgressDialog;
        private int mFileCount=0;
        private boolean mCanceled;
        private Pattern mPattern;
        private String mPatternText;

        @Override
        protected void onPreExecute() {
            mData.removeAll(mData);
            mCanceled = false;
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setTitle(R.string.grep_spinner);
            mProgressDialog.setMessage(mQuery);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog)
                {
                    mCanceled = true;
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            return grepRoot( params[0] );
        }


        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();

            synchronized( mData ){
                Collections.sort( mData , new GrepView.Data() );

                mAdapter.notifyDataSetChanged();
            }
            mGrepView.setSelection(0);
            Toast.makeText(mActivity,result?R.string.grep_finished:R.string.grep_canceled, Toast.LENGTH_LONG).show();
            mTask = null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {
            mProgressDialog.setMessage( Search.this.getString(R.string.progress ,mQuery,mFileCount));
            if ( progress[0] == 1 ){
                synchronized( mData ){
                    mAdapter.notifyDataSetChanged();
                    mGrepView.setSelection(mData.size()-1);
                }
            }
        }


        boolean grepRoot( String text )
        {
            if ( mPrefs.mIgnoreCase ){
                mPatternText = text.toLowerCase();
                mPattern = Pattern.compile(mPatternText, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE );
            }else{
                mPatternText = text;
                mPattern = Pattern.compile(mPatternText);
            }

            if ( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState() ) ) {

                for( String dir : mPrefs.mDirList ){
                    if ( !grepDirectory(new File(dir) ) ){
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        boolean grepDirectory( File dir )
        {
            if ( mCanceled ){
                return false;
            }
            if ( dir==null ){
                return false;
            }
//			android.util.Log.e( "aGrep" , "dir="+dir.getPath() );

            File[] flist = dir.listFiles( );

            for( File f : flist ){
                boolean res = false;
                if ( f.isDirectory() ){
                    res = grepDirectory( f );
                }else{
                    res = grepFile( f );
                }
                if ( !res ) {
                    return false;
                }
            }
            return true;
        }

        boolean grepFile( File file  )
        {
            if ( mCanceled ){
                return false;
            }
            if ( file==null ){
                return false;
            }

            boolean extok=false;
            for( String ext : mPrefs.mExtList ){
                if ( file.getName().toLowerCase().endsWith("."+ext)){
                    extok = true;
                    break;
                }
            }
            if ( !extok ){
                return true;
            }

//            android.util.Log.e( "aGrep" , "file="+file.getPath() );


            InputStream is;
            try {
                is = new BufferedInputStream( new FileInputStream( file ) , 65536 );
                is.mark(65536);

                //  文字コードの判定
                UniversalDetector detector = new UniversalDetector(null);
                try{
                    int totalread=0;
                    int nread;
                    byte[] buff = new byte[1024];
                    while ((nread = is.read(buff)) > 0 && !detector.isDone()) {
                        detector.handleData(buff, 0, nread);
                        totalread += nread;
                        if ( totalread > 1024*2 ){
                            break;
                        }
                    }
                    detector.dataEnd();
                }
                catch( FileNotFoundException e ){
                    e.printStackTrace();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return true;
                }
                String encode = detector.getDetectedCharset();
//				if ( encode != null ){
//					android.util.Log.e("aGrep", encode);
//				}
                detector.reset();
                is.reset();

                BufferedReader br=null;
                try {
                    if ( encode != null ){
                        br = new BufferedReader( new InputStreamReader( is , encode ) , 8192 );

                    }else{
                        br = new BufferedReader( new InputStreamReader( is ) , 8192 );
                    }

                    String text;
                    int line = 0;
                    boolean found = false;
                    while(  ( text = br.readLine() )!=null ){
                        line ++;

                        if ( mPrefs.mRegularExrpression ){
                            Matcher m = mPattern.matcher( text );
                            if ( m.find() ){
                                found = true;
                                SpannableStringBuilder ss = new SpannableStringBuilder(text);

                                int start=-1;
                                int end;
                                while( m.find(start) ){
                                    start = m.start();
                                    end = m.end();

                                    BackgroundColorSpan span = new BackgroundColorSpan( 0xFF00FFFF );
                                    ss.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    start = end;
                                }

                                synchronized( mData ){
                                    mData.add(new GrepView.Data(file, line, ss));
                                }

                            }
                        }else{
                            String temptext = text;
                            if ( mPrefs.mIgnoreCase ){
                                temptext = text.toLowerCase();
                            }
                            if ( temptext.indexOf(mPatternText) >=0 ){
                                found = true;
                                SpannableStringBuilder ss = new SpannableStringBuilder(text);

                                int start=0;
                                int end;

                                while( (start = temptext.indexOf(mPatternText, start)) > 0 ){
                                    end = start+mPatternText.length();

                                    BackgroundColorSpan span = new BackgroundColorSpan( 0xFF00FFFF );
                                    ss.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    start = end;
                                }

                                synchronized( mData ){
                                    mData.add(new GrepView.Data(file, line, ss));
                                }

                            }

                        }
                    }
                    br.close();
                    is.close();
                    mFileCount++;
                    if ( found ) {
                        publishProgress(1);
                    }else{
                        publishProgress(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return true;
        }

//
//		private String  extractZip(File f)
//		{
//			String ret=null;
//    		ZipInputStream zis;
//			try {
//				zis = new ZipInputStream(new FileInputStream(f) );
//	    		ZipEntry ze;
//	    		while( ret==null && (ze= zis.getNextEntry())!=null ){
//	    			String name = ze.getName();
//
//	    			if ( name.toLowerCase().endsWith(".dic") ){
//	    				File nf = new File( SDCARD.getPath() + "/adice/" + getName(name) );
//	    				nf.getParentFile().mkdir();
//
//	    				FileOutputStream fos = new FileOutputStream(nf);
//
//	    				byte[] buff = new byte[512];
//	    				int	len;
//	    				int offset=0;
//	    				int lastoffset=0;
//
//	    				for( ;; ){
//	    					len = zis.read(buff);
//	    					if ( len == -1  )break;
//	    					fos.write(buff, 0, len);
//	    					offset += len;
//
//	    					// update progress bar
//	    					if ( offset - lastoffset > 1024*16 ){
//	    						publishProgress( offset , 0 );
//	    						lastoffset = offset;
//	    					}
//	    				}
//	    				fos.close();
//	    				ret = nf.getPath() ;
//	    			}
//	    			zis.closeEntry();
//	    		}
//	    		zis.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			return ret;
//		}

    }

    @Override
    public void onGrepItemClicked(int position)
    {
        GrepView.Data data = (GrepView.Data) mAdapter.getItem(position);

        Intent it = new Intent(this,TextViewer.class);

        it.putExtra(TextViewer.EXTRA_PATH , data.mFile.getAbsolutePath() );
        it.putExtra(TextViewer.EXTRA_QUERY, mQuery);
        it.putExtra(TextViewer.EXTRA_LINE, data.mLinenumber );

        startActivity(it);
    }

    @Override
    public boolean onGrepItemLongClicked(int position)
    {
        return false;
    }

}
