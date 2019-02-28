package truemedicine.logiticks.com.trumedicine.utils;

import android.content.Context;

import de.psdev.licensesdialog.licenses.License;
import truemedicine.logiticks.com.trumedicine.R;


public class Privacy extends License {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String readSummaryTextFromResources(Context context) {
        return getContent(context, R.raw.privacy);
    }

    @Override
    public String readFullTextFromResources(Context context) {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }
}
