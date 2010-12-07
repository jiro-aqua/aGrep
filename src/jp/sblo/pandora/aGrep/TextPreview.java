package jp.sblo.pandora.aGrep;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TextPreview extends ListView {


    public TextPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public TextPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextPreview(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context)
    {
        setSmoothScrollbarEnabled(true);
        setScrollingCacheEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setFastScrollEnabled(true);
        setBackgroundColor(Color.WHITE);
        setCacheColorHint(Color.WHITE);
        setDividerHeight(0);
    }

    static class Adapter extends ArrayAdapter<CharSequence>
    {

        private int mFontSize;
        private Pattern mPattern;
        private int mFgColor;
        private int mBgColor;

        public Adapter(Context context, int resource, int textViewResourceId, ArrayList<CharSequence> objects)
        {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView view = (TextView)convertView;
            if ( view == null ) {
                view = (TextView)inflate(getContext() , R.layout.textpreview_row , null );
                view.setTextColor(Color.BLACK);
                view.setTextSize(mFontSize);
            }
            CharSequence d = getItem(position);

            view.setText( Search.highlightKeyword( d, mPattern, mFgColor , mBgColor ) );

            return view;
        }

        public void setFormat(Pattern pattern, int fg, int bg, int size) {
            mPattern = pattern;
            mFgColor = fg;
            mBgColor = bg;
            mFontSize = size;
        }
    }


}
