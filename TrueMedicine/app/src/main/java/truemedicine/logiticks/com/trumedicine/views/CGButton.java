package truemedicine.logiticks.com.trumedicine.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;


public class CGButton extends Button {
    Context context;
    public CGButton(Context context) {
        super(context);
        this.context = context;
        initFonts();
    }

    private void initFonts() {
        Typeface fromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        setTypeface(fromAsset);
    }

    public CGButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initFonts();
    }

    public CGButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initFonts();
    }


}
