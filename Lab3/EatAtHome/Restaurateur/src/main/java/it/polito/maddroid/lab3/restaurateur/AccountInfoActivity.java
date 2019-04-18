package it.polito.maddroid.lab3.restaurateur;


import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class AccountInfoActivity extends AppCompatActivity {
    
    private boolean editMode = true;
    
    private MenuItem menuEdit;
    private MenuItem menuConfirm;
    
    private EditText etName;
    private EditText etPhone;
    private EditText etMail;
    private EditText etDescription;
    private EditText etAddress;
    
    private TextView tvDescriptionCount;
    
    private ImageView ivPhoto;
    private FloatingActionButton fabPhoto;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
    
        MenuInflater menuInflater = getMenuInflater();
        
        menuInflater.inflate(R.menu.account_info_menu, menu);
        
        menuEdit = menu.findItem(R.id.menu_edit);
        menuConfirm = menu.findItem(R.id.menu_confirm);
        
        setMenuItemsVisibility();
        
        return true;
    }
    
    private void getReferencesToViews() {
        
        etName = findViewById(R.id.et_name);
        etAddress = findViewById(R.id.et_address);
        etDescription = findViewById(R.id.et_description);
        etMail = findViewById(R.id.et_mail);
        etPhone = findViewById(R.id.et_phone);
        
        fabPhoto = findViewById(R.id.fab_add_photo);
        ivPhoto = findViewById(R.id.iv_avatar);
        
        tvDescriptionCount = findViewById(R.id.tv_description_count);
        
    }
    
    private void setMenuItemsVisibility() {
        menuEdit.setVisible(!editMode);
        menuConfirm.setVisible(editMode);
    }
    
    private void setEditEnabled(boolean enabled) {
    
    
    
    }
}
