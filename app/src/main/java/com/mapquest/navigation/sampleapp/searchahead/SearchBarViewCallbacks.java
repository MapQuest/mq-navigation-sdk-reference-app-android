package com.mapquest.navigation.sampleapp.searchahead;

import android.view.KeyEvent;
import android.widget.TextView;

/**
 * Callbacks used for communication from the SearchBarView to the parent view class.
 */
public interface SearchBarViewCallbacks {

    /**
     * Update the search ahead contents from new text the user has entered
     **/
    void onUpdateContentForSearchText(String text);

    /**
     * Perform a search based on an action the user selects from the keyboard
     */
    boolean onEditorAction(TextView v, int actionId, KeyEvent event);
}
