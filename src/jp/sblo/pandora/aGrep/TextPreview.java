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

        private Pattern mPattern;
        private int mColor;

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
            }
            CharSequence d = getItem(position);

            view.setText( Search.highlightKeyword( d, mPattern, mColor ) );

            return view;
        }

        public void setHighlight(Pattern pattern, int color) {
            mPattern = pattern;
            mColor = color;
        }
    }


}
