package truemedicine.logiticks.com.trumedicine.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class CGEditText extends AppCompatEditText {
    Context context;

    public CGEditText(Context context) {
        super(context);
        this.context = context;
        initFonts();
    }

    private void initFonts() {
        Typeface fromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        setTypeface(fromAsset);
    }

    public CGEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initFonts();
    }


    public CGEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initFonts();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ((TextInputLayout) getParent()).setError(null);
        ((TextInputLayout) getParent()).setErrorEnabled(false);
        return super.onTouchEvent(event);
    }

    @Override
    public void setError(CharSequence error) {
        ((TextInputLayout) getParent()).setError(error);
        ((TextInputLayout) getParent()).setEnabled(true);
    }

}
