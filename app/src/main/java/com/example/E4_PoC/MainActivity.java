package com.example.E4_PoC;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.E4_PoC.R;

import java.io.IOException;
import java.util.ArrayList;

import javax.mail.Message;
import javax.mail.MessagingException;


public class MainActivity extends AppCompatActivity {

   // Button btDecrypt;
    EmailUtilities utilities;
    EmailAccount account;
    AlertDialog.Builder builder;

    ArrayAdapter<String> adapter;
    ArrayList<String> arrayList;
    ListView listView;

    public void encryptAll(MenuItem mi){
        Toast.makeText(getApplicationContext(), "Encrypting all emails on server ...", Toast. LENGTH_LONG).show();
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    utilities.Authenticate();
                    utilities.createConfig();
                    utilities.readMails();
                    for (Message  decryptedMail:utilities.getMessages()) {

                        if (!utilities.isEncrypted(decryptedMail)) {
                            byte[] decryptedMailContent = utilities.readPlainMailAsByteArray(decryptedMail);

                            utilities.deleteMail(decryptedMail);

                            Message encryptedtedMail = utilities.createEncryptedMail(decryptedMailContent);
                            utilities.appendSingleMail(encryptedtedMail);
                        } else {
                            Log.d("Message Encrypted","Message is already encrypted");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void decryptAll(MenuItem mi){
        Toast.makeText(getApplicationContext(), "Decrypting mails ....", Toast. LENGTH_LONG).show();
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    utilities.Authenticate();
                    utilities.createConfig();
                    utilities.readMails();
                    arrayList.clear();
                    for (Message  encryptedMail:utilities.getMessages()) {

                        if (utilities.isEncrypted(encryptedMail)) {
                            String decryptedMailContent = utilities.readEncryptedMail(encryptedMail);

                           // utilities.deleteMail(encryptedMail);

                            arrayList.add(decryptedMailContent);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    return;
                                }
                            });

                        //    Message decryptedMail = utilities.createDecryptedMail(decryptedMailContent);
                          //  utilities.appendSingleMail(decryptedMail);
                        } else {
                            Toast.makeText(MainActivity.this, "Message is already decrypted", Toast.LENGTH_LONG).show();
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void enableAutoEncrypt(MenuItem mi){
        Toast.makeText(getApplicationContext(), "Encrypt on receipt is enabled", Toast. LENGTH_LONG).show();
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    utilities.Authenticate();
                    utilities.createConfig();
                    utilities.readMails();
                    while(true){
                        boolean newMail = utilities.hasNewMail();
                        if(newMail){
                            for (Message  mail:utilities.getMessages()) {


                                // Message lastMail = utilities.getLastMail();
                                if (!utilities.isEncrypted(mail)) {
                                    byte[] plainMail = utilities.readPlainMailAsByteArray(mail);

                                    utilities.deleteMail(mail);

                                    Message encryptedMessage = utilities.createEncryptedMail(plainMail);
                                    utilities.appendSingleMail(encryptedMessage);
                                } else {
                                    Log.d("Message Encrypted","Message is already encrypted");
                                }
                            }
                            Thread.sleep(5000);
                        } else{
                            Log.d("No new messages", "No new messages");
                            Thread.sleep(5000);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }).start();
    }
    public void composeNewMail(MenuItem mi){
        Intent i = new Intent(MainActivity.this, SendMessageActivity.class);
        i.putExtra("emailClass", (Parcelable) account);
        startActivity(i);
    }

    public void readMails(MenuItem mi){

        read();

    }
    public void read(){
        Toast.makeText(getApplicationContext(), "Loading new mails", Toast. LENGTH_LONG).show();        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    utilities.Authenticate();
                    utilities.createConfig();
                    utilities.readMails();

                    //For automatic inbox refresh
                    //  while (true) {

                    boolean newMail = utilities.hasNewMail();
                    arrayList.clear();
                    if (newMail) {
                        for (Message mail : utilities.getMessages()) {

                            String content = utilities.getTextFromMessage(mail);
                            arrayList.add(content);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();

                                }
                            });
                        }
                        //  Thread.sleep(5000);
                    } else {
                        Log.d("No new messages", "No new messages");
                        //  Thread.sleep(5000);
                    }
                    // }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }).start();
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        //Create toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = getIntent();
        account = (EmailAccount) intent.getParcelableExtra("emailClass");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        builder = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        builder.setTitle("Please set an Encryption/Decryption password");

        builder.setCancelable(false);

        EditText encryptionPwd = new EditText(MainActivity.this);
        encryptionPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        encryptionPwd.setHint("Enter password");
        layout.addView(encryptionPwd);

        EditText confirmEncryptionPwd = new EditText(MainActivity.this);
        confirmEncryptionPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmEncryptionPwd.setHint("Confirm password");
        layout.addView(confirmEncryptionPwd);

        builder.setView(layout);
        builder.setPositiveButton("OK",null);
        AlertDialog dialog = builder.create();
        dialog.show();


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (encryptionPwd.getText().toString().isEmpty() || confirmEncryptionPwd.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
                }
                else if (encryptionPwd.getText().toString().equals(confirmEncryptionPwd.getText().toString())) {
                    account.setEncryptionPassword(encryptionPwd.getText().toString());
                    utilities = new EmailUtilities(MainActivity.this,account);
                    read();
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_LONG).show();
                }
            }
        });


        //Decrypt Single mail
/*        btDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            utilities.Authenticate();
                            utilities.createConfig();
                            utilities.readMails();
                            Message lastMessage = utilities.getLastMail();
                            utilities.readEncryptedMail(lastMessage);
                        } catch (MessagingException | IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();

            }
        });*/
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }


}
