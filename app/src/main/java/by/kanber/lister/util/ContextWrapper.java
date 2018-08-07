package by.kanber.lister.util;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class ContextWrapper extends android.content.ContextWrapper{
    public ContextWrapper(Context newBase) {
        super(newBase);
    }

    public static ContextWrapper wrap(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        context = context.createConfigurationContext(configuration);

        return new ContextWrapper(context);
    }
}