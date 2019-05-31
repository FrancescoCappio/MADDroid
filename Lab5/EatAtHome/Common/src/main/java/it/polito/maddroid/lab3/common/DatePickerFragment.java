package it.polito.maddroid.lab3.common;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    
    private EditText etCaller;
    
    public DatePickerFragment(EditText etCaller) {
        this.etCaller = etCaller;
    }
    
    public DatePickerFragment() {}
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // we need to use a context with a modified theme so that the picker is not entirely white
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppThemeInverted);
        // Create a new instance of TimePickerDialog and return it
        return new DatePickerDialog(contextThemeWrapper, this, year, month, day);
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        
        String sMonth = String.format("%02d", month+1);
        String sDay = String.format("%02d", dayOfMonth);
        if (etCaller != null)
            etCaller.setText(sDay + "-" + sMonth  + "-" + year);
    
    }
}
