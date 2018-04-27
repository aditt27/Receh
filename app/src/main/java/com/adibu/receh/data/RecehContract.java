package com.adibu.receh.data;

/**
 * Created by AdityaBudi on 25/09/2017.
 */

public class RecehContract {

    public static final String USERS_DATA = "users";
    public static final String RECEH_DATA = "data";
    private static String SELECTED_RECEH_DATA;

    public static String getSelectedRecehData() {
        return SELECTED_RECEH_DATA;
    }

    public static void setSelectedRecehData(String selectedRecehData) {
        SELECTED_RECEH_DATA = selectedRecehData;
    }


}
