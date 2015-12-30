package com.example.aqeelp.abita;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by aqeelp on 11/28/15.
 */
public class DescriptionView extends FrameLayout {
    Description description;
    Context context;

    public DescriptionView(Context c, AttributeSet attrs, int defStyle, Description d) {
        super(c, attrs, defStyle);
        context = c;
        description = d;
    }

    public DescriptionView(Context c, AttributeSet attrs, Description d) {
        super(c, attrs);
        context = c;
        description = d;
    }

    public DescriptionView(Context c, Description d) {
        super(c);
        context = c;
        description = d;
    }

    public View getView() {
        View view = inflate(this.context, R.layout.description_layout, null);
        if (description.getUser() != null)
            ((TextView) view.findViewById(R.id.description_name)).setText(description.getUser().getDisplayName() + " says...");
        else
            ((TextView) view.findViewById(R.id.description_name)).setText("An anonymous user says...");
        // TODO ((TextView) view.findViewById(R.id.description_date)).setText(description.getCreatedAt().toString());
        ((TextView) view.findViewById(R.id.description_date)).setText("Date created");
        ((TextView) view.findViewById(R.id.description_content)).setText(description.getText());
        return view;
    }
}
