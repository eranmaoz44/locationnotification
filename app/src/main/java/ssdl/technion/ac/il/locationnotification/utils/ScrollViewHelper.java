package ssdl.technion.ac.il.locationnotification.utils;

/**
 * Created by Dave on 24.11.2014.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

public class ScrollViewHelper extends ScrollView {

    private OnScrollViewListener mOnScrollViewListener;

    public ScrollViewHelper(Context context) {
        super(context);
    }
    public ScrollViewHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ScrollViewHelper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public interface OnScrollViewListener {
        void onScrollChanged(ScrollViewHelper v, int l, int t, int oldl, int oldt);
    }

    public void setOnScrollViewListener(OnScrollViewListener l) {
        if(l==null){
            Log.v("fuck","shit1");
        }else {
            Log.v("fuck", "shit2");
            this.mOnScrollViewListener = l;
        }
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(mOnScrollViewListener==null){
            Log.v("fuck","shit1");
        }else{
        Log.v("fuck","shit2");
        mOnScrollViewListener.onScrollChanged( this, l, t, oldl, oldt );

        }
        super.onScrollChanged( l, t, oldl, oldt );
    }
}
