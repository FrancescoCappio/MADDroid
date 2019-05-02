package it.polito.maddroid.lab2;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.text.DecimalFormat;
import java.util.Calendar;

import it.polito.maddroid.lab3.common.R;


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public TimePickerFragment(TextView tv_chiamante) {
        this.tv_chiamante = tv_chiamante;
    }

    TextView tv_chiamante;


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
        DecimalFormat formatter = new DecimalFormat("00");
        String shour = formatter.format(hourOfDay);
        String sminutes = formatter.format(minute);
        tv_chiamante.setText(""+shour+":"+sminutes);

    }
}
