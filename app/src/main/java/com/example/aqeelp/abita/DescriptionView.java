package com.example.aqeelp.abita;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by aqeelp on 11/28/15.
 */
public class DescriptionView extends FrameLayout {
    Description description;

    public DescriptionView(Context context, AttributeSet attrs, int defStyle, Description d) {
        super(context, attrs, defStyle);
        description = d;
    }

    public DescriptionView(Context context, AttributeSet attrs, Description d) {
        super(context, attrs);
        description = d;
    }

    public DescriptionView(Context context, Description d) {
        super(context);
        description = d;
    }

    public View getView() {
        View view = inflate(getContext(), R.layout.description_layout, null);
        ((TextView) view.findViewById(R.id.description_name)).setText(description.getUserId());
        ((TextView) view.findViewById(R.id.description_date)).setText(description.getCreatedAt().toString());
        ((TextView) view.findViewById(R.id.description_content)).setText(description.getText());
        return view;
    }
}
