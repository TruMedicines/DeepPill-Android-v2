package truemedicine.logiticks.com.trumedicine.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;


public class CGTextViewLight extends AppCompatTextView {
    Context context;

    public CGTextViewLight(Context context) {
        super(context);
        this.context = context;
        initFonts();
    }


    public CGTextViewLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initFonts();

    }

    private void initFonts() {

        Typeface fromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        setTypeface(fromAsset);

    }


    public CGTextViewLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initFonts();
    }
}
