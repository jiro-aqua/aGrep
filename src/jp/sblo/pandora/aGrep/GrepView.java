package jp.sblo.pandora.aGrep;

import java.util.ArrayList;
import java.util.Comparator;
import java.io.File;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GrepView extends ListView {

    static	class Data implements Comparator<Data> {

        public File mFile ;
        public int 	mLinenumber ;
        public CharSequence 	mText;

        public Typeface mFont1;
        public Typeface mFont2;
        public int	mSize1;
        public int	mSize2;

        public Data(){
            this( null , 0 , null );
        }

        public  Data( File file , int linenumber , CharSequence text ){
            mFile = file;
            mLinenumber = linenumber;
            mText = text;
        }

        @Override
        public int compare(Data object1, Data object2) {
            int ret = object1.mFile.getName().compareToIgnoreCase(object2.mFile.getName());
            if ( ret == 0 ){
                ret = object1.mLinenumber - object2.mLinenumber;
            }
            return ret;
        }

    }

    interface Callback {
        void onGrepItemClicked(int position);
        boolean onGrepItemLongClicked(int position);
    }

    private Callback mCallback;

    private void init(Context context)
    {
        setSmoothScrollbarEnabled(true);
        setScrollingCacheEnabled  (true);
        setFocusable(true);
        setFocusableInTouchMode(true);
          setFastScrollEnabled(true);
          setBackgroundColor(Color.WHITE);
          setCacheColorHint(Color.WHITE);
	  	setDividerHeight(2);
          setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent , View view, int position, long id)
            {
                if ( mCallback != null ){
                    mCallback.onGrepItemClicked(position);
                }
            }
        });
          setOnItemLongClickListener( new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent , View view, int position, long id)
            {
                if ( mCallback != null ){
                    return mCallback.onGrepItemLongClicked(position);
                }
                return false;
            }
        });

    }

    public GrepView(Context context) {
        super(context);
        init(context);
    }

    public GrepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public GrepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setCallback( Callback cb )
    {
        mCallback = cb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        requestFocus();
        return super.onTouchEvent(ev);
    }


    static class GrepAdapter extends ArrayAdapter<Data>
    {

        static	class ViewHolder {
            TextView Index;
            TextView kwic;
        }


        public GrepAdapter(Context context, int resource, int textViewResourceId, ArrayList<Data> objects)
        {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final View view;
            ViewHolder holder;
            if ( convertView != null ) {
                view = convertView;
                holder = (ViewHolder) view.getTag();

            } else {
                view = inflate(getContext() , R.layout.list_row , null );

                holder = new ViewHolder();
                holder.Index = (TextView)view.findViewById(R.id.ListIndex);
                holder.kwic = (TextView)view.findViewById(R.id.ListPhone);

                holder.Index.setTextColor(Color.BLACK);
                holder.kwic.setTextColor(Color.BLACK);

                view.setTag(holder);
            }
            Data d = getItem(position);

            String fname = String.format( "%s(%d)", d.mFile.getName(), d.mLinenumber );
            setItem( holder.Index ,fname , d.mFont1 ,d.mSize1 );
            setItem( holder.kwic ,d.mText , d.mFont2 ,d.mSize2);

            return view;
        }

        private void setItem( TextView tv , CharSequence str, Typeface tf , int size )
        {
            if (str ==null || str.length()==0 ){
                tv.setVisibility(View.GONE);
            }else{
                tv.setVisibility(View.VISIBLE);
                tv.setText(str);
                //tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size );
                //tv.setTypeface( tf );
            }
        }
    }
}
