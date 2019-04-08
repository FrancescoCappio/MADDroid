package it.polito.maddroid.lab2;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.widget.TimePicker;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        
        // we need to use a context with a modified theme so that the picker is not entirely white
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppThemeInverted);
        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(contextThemeWrapper, this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
    
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        
        if (activity instanceof OrderDetailActivity) {
            ((OrderDetailActivity) activity).setTime(hourOfDay, minute);
        }
        
    }
}
