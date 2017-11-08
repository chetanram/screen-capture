package com.screen.capture.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Collection of utilities used to save various objects persistently in share
 * preferences
 *
 * @author Altaf Shaikh
 */
public class PrefUtils {

    private static final String CLASS_TAG = "PrefUtils";
    private static final String DELIMITER = "##";

    /**
     * Retrieves an array of boolean values from the shared preferences of the
     * context
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to retrieve.
     * @return
     */
    public static boolean[] getBooleanArray(Context context, String prefKey) {
        String boolString = getString(context, prefKey, null);

        return parseStringOfBooleans(boolString);
    }

    /**
     * Saves an array of boolean values in delimited string form in the shared
     * preferences of the context
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to retrieve.
     * @param array   Collection of boolean values to store in shared preferences
     * @return Returns true if the new values were successfully written to
     * persistent storage.
     */
    public static boolean saveBooleanArray(Context context, String prefKey,
                                           boolean[] array) {
        final String delimString = convertBooleanArrayToString(array);

        return saveString(context, prefKey, delimString);
    }

    /**
     * Retrieves an array of String values from the shared preferences of the
     * context
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to retrieve.
     * @return
     */
    public static String[] getStringArray(Context context, String prefKey) {
        String prefString = getString(context, prefKey, null);

        return parseStringArray(prefString);
    }

    /**
     * Saves an array of String values in delimited form in the shared
     * preferences of the context
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to retrieve.
     * @param array
     * @return Returns true if the new values were successfully written to
     * persistent storage.
     */
    public static boolean saveStringArray(Context context, String prefKey,
                                          String[] array) {
        final String delimString = convertStringArrayToString(array);

        return saveString(context, prefKey, delimString);
    }

    /**
     * Retrieves an object of type T from the shared preferences of the context.
     * The StringBasedObjectBuilder is used to reconstruct the object.
     *
     * @param <T>     Type of object to be retrieved.
     * @param context Application context used to retrieve shared preferences.
     * @param prefKey The name of the preference to retrieve.
     * @param builder PrefUtils.StringBasedObjectBuilder used to rebuild the object
     *                from String form.
     * @return Object of type T that was rebuilt using the builder if the
     * preference was found, null if not.
     */
    public static <T> T getObject(Context context, String prefKey,
                                  StringBasedObjectBuilder<T> builder) {

        String prefString = getString(context, prefKey, null);

        return prefString == null ? null : builder.build(prefString);
    }

    /**
     * Saves an object of type T in the shared preferences of the context. The
     * StringBasedObjectBuilder is used to deconstruct the object to be rebuilt
     * later using the same builder.
     *
     * @param <T>
     * @param context Application context used to retrieve shared preferences.
     * @param prefKey The name of the preference to retrieve.
     * @param builder PrefUtils.StringBasedObjectBuilder used to deconstruct the
     *                object into String form.
     * @param object  The object to be converted into a string with the builder and
     *                saved in the preferences.
     * @return Returns true if the new values were successfully written to
     * persistent storage.
     */
    public static <T> boolean saveObject(Context context, String prefKey,
                                         StringBasedObjectBuilder<T> builder, T object) {

        final String delimString = builder.deconstruct(object);

        return saveString(context, prefKey, delimString);
    }

    /**
     * Retrieves a Date object in <code>long</code> form in the shared
     * preferences of the context and creates a new Date object. Returns null if
     * does not exist.
     *
     * @param context Application context used to retrieve shared preferences.
     * @param prefKey The name of the preference to retrieve.
     * @return Returns a new Date based on the preference value if it exists, or
     * <code>null</code>.
     */
    public static Date getDate(Context context, String prefKey) {
        final SharedPreferences prefs = getPreferences(context);

        long timeMillis = prefs.getLong(prefKey, 0);

        return timeMillis == 0 ? null : new Date(timeMillis);
    }

    /**
     * Saves a Date object in <code>long</code> form in the shared preferences
     * of the context
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to retrieve.
     * @param date
     */
    public static void saveDate(Context context, String prefKey, Date date) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.putLong(prefKey, date.getTime());
        editor.commit();

        /*LogUtils.debug(CLASS_TAG, "saveTimeToPreference : " + prefKey + " : "
                + date);*/
    }

    /**
     * Retrieve a String value from the preferences.
     *
     * @param context  Application context used to retrieve shared preferences.
     * @param prefKey  The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this name that
     * is not a String.
     */
    public static String getString(Context context, String prefKey,
                                   String defValue) throws ClassCastException {

        final SharedPreferences prefs = getPreferences(context);

        return prefs.getString(prefKey, defValue);
    }

    /**
     * Set a String value in the preferences of the context. Commit is called
     * automatically.
     *
     * @param context    Application context used to retrieve shared preferences
     * @param prefKey    The name of the preference to modify.
     * @param prefString The value of the preference to save.
     * @return
     */
    public static boolean saveString(Context context, String prefKey,
                                     String prefString) {
        if (prefString == null || prefString.length() == 0) {
            return false;
        }

        final SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.putString(prefKey, prefString);
        return editor.commit();
    }

    /**
     * @param context  Application context used to retrieve shared preferences
     * @param prefKey  The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this name that
     * is not a String.
     */
    public static boolean getBoolean(Context context, String prefKey,
                                     boolean defValue) {

        final SharedPreferences prefs = getPreferences(context);

        return prefs.getBoolean(prefKey, defValue);
    }

    /**
     * Set a integer value in the preferences of the context. Commit is called
     * automatically.
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to modify.
     * @param value   The new value for the preference.
     * @return Returns true if the new value was successfully written to
     * persistent storage.
     */
    public static boolean saveInt(Context context, String prefKey, int value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.putInt(prefKey, value);
        return editor.commit();
    }

    /**
     * @param context  Application context used to retrieve shared preferences
     * @param prefKey  The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this name that
     * is not a String.
     */
    public static int getInt(Context context, String prefKey, int defValue) {

        final SharedPreferences prefs = getPreferences(context);

        return prefs.getInt(prefKey, defValue);
    }

    /**
     * Set a long value in the preferences of the context. Commit is called
     * automatically.
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to modify.
     * @param value   The new value for the preference.
     * @return Returns true if the new value was successfully written to
     * persistent storage.
     */
    public static boolean saveLong(Context context, String prefKey, long value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.putLong(prefKey, value);
        return editor.commit();
    }

    /**
     * @param context  Application context used to retrieve shared preferences
     * @param prefKey  The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue. Throws
     * ClassCastException if there is a preference with this name that
     * is not a String.
     */
    public static long getLong(Context context, String prefKey, long defValue) {

        final SharedPreferences prefs = getPreferences(context);

        return prefs.getLong(prefKey, defValue);
    }

    /**
     * Set a boolean value in the preferences of the context. Commit is called
     * automatically.
     *
     * @param context Application context used to retrieve shared preferences
     * @param prefKey The name of the preference to modify.
     * @param value   The new value for the preference.
     * @return Returns true if the new value was successfully written to
     * persistent storage.
     */
    public static boolean saveBoolean(Context context, String prefKey,
                                      boolean value) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.putBoolean(prefKey, value);
        return editor.commit();
    }

    /**
     * Retrieve and hold the contents of the preferences file for the context,
     * returning a SharedPreferences through which you can retrieve and modify
     * its values. Only one instance of the SharedPreferences object is returned
     * to any callers for the same name, meaning they will see each other's
     * edits as soon as they are made.
     *
     * @param context
     * @return Returns the single SharedPreferences instance that can be used to
     * retrieve and modify the preference values.
     */
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(getPreferencesName(context),
                Context.MODE_PRIVATE);
    }

    public static String getPreferencesName(Context context) {
        return context.getPackageName();
    }

    private static String convertBooleanArrayToString(boolean[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder output = new StringBuilder();

        for (boolean b : array) {
            output.append(b);
            output.append(DELIMITER);
        }

        return output.toString();
    }

    private static boolean[] parseStringOfBooleans(String delimitedString) {
        if (delimitedString == null) {
            return null;
        }

        String[] array = delimitedString.split(DELIMITER);
        boolean[] boolArray = new boolean[array.length];

        for (int i = 0; i < array.length; i++) {
            try {
                boolArray[i] = Boolean.parseBoolean(array[i]);
            } catch (Exception e) {
                boolArray[i] = false;
            }
        }

        return boolArray;
    }

    private static String convertStringArrayToString(String[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder output = new StringBuilder();

        for (String s : array) {
            output.append(s);
            output.append(DELIMITER);
        }

        return output.toString();
    }

    private static String[] parseStringArray(String delimitedString) {
        if (delimitedString == null) {
            return null;
        }

        return delimitedString.split(DELIMITER);
    }


    /**
     * This abstract factory is used to decompose objects into a string
     * representation that can later be used to reconstruct the object
     *
     * @param <T> - The type of object this builder constructs
     * @author Altaf Shaikh
     */
    public abstract class StringBasedObjectBuilder<T> {

        /**
         * This method must be able to deconstruct the object into a meaningful
         * string representation of itself. This string should contain all
         * necessary information needed to reconstruct to object later.
         *
         * @param object
         * @return String representation of <Code>object</Code>
         */
        public abstract String deconstruct(T object);

        /**
         * This method must be able to construct a new object of type T using
         * only the base String provided.
         *
         * @param base
         * @return new object of type T
         */
        public abstract T build(String base);
    }

    public static void clearAll(Context context) {
        final SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.clear();
        editor.commit();

    }




}
