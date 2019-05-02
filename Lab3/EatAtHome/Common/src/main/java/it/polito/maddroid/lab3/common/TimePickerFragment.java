package it.polito.maddroid.lab3.common;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.text.DecimalFormat;
import java.util.Calendar;


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    
    private TextView tvChiamante;
    private EditText etCaller;

    public TimePickerFragment(TextView tvChiamante) {
        this.tvChiamante = tvChiamante;
    }
    
    public TimePickerFragment(EditText etCaller) {
        this.etCaller = etCaller;
    }
    
    public TimePickerFragment() {}
    
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
        DecimalFormat formatter = new DecimalFormat("00");
        String shour = formatter.format(hourOfDay);
        String sminutes = formatter.format(minute);
        
        if (tvChiamante != null)
            tvChiamante.setText(""+shour+":"+sminutes);

        if (etCaller != null)
            etCaller.setText(""+shour+":"+sminutes);
    }
}
