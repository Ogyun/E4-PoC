package com.example.javamail;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMessageActivity extends AppCompatActivity {

    EditText etTo,etSubject, etMessage;
    Button btSend;
    EmailUtilities utilities;
    String to,subject,content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        etTo = findViewById(R.id.et_to);
        etSubject = findViewById(R.id.et_subject);
        etMessage = findViewById(R.id.et_message);
        btSend = findViewById(R.id.bt_send);

        Intent intent = getIntent();
        utilities = (EmailUtilities) intent.getParcelableExtra("emailClass");

        btSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
              utilities.AuthenticateSMTP();
              to = etTo.getText().toString();
              subject = etSubject.getText().toString();
              content = etMessage.getText().toString();

                try {
                   Message newMail = utilities.createMailForSending(to,subject,content);

                    //Works
                    //Alternative to thread
                    new SendMail().execute(newMail);
                    Log.d("Success:", "Email Message Sent Successfully");

                    //Clear text fields
                    etTo.setText("");
                    etSubject.setText("");
                    etMessage.setText("");

                    to ="";
                    subject="";
                    content ="";


                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private class SendMail extends AsyncTask<Message,String,String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SendMessageActivity.this, "Please wait", "Sending email", true, false);
        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
                return "Success";
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return "Error";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (s.equals("Success")) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SendMessageActivity.this);
                builder.setCancelable(false);
                builder.setTitle(Html.fromHtml("<font color='#509324'>Success</font>"));
                builder.setMessage("Mail send successfully");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialaog, int which) {
                        dialaog.dismiss();
                        etTo.setText("");
                        etSubject.setText("");
                        etMessage.setText("");
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getApplicationContext(), "Something went wrong?", Toast.LENGTH_SHORT).show();
            }
        }
    }
}