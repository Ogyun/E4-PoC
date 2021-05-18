package com.example.E4_PoC;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.E4_PoC.R;

import javax.mail.Message;


public class MainActivity extends AppCompatActivity {

   // Button btDecrypt;
    Button btEncryptAll;
    Button btDecryptAll;
    Button btEncryptOnReceipt;
    EmailUtilities utilities;
    Button btComposeNewMail;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Decrypt single Mail button
        // btDecrypt = findViewById(R.id.bt_decrypt);
        btEncryptAll = findViewById(R.id.bt_encryptAll);
        btDecryptAll = findViewById(R.id.bt_decryptAll);
        btEncryptOnReceipt = findViewById(R.id.bt_enableEncryptOnReceipt);
        btComposeNewMail = findViewById(R.id.bt_composeNewMail);



        Intent intent = getIntent();
        utilities = (EmailUtilities) intent.getParcelableExtra("emailClass");

        btComposeNewMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SendMessageActivity.class);
                i.putExtra("emailClass", (Parcelable) utilities);
                startActivity(i);
                                        }
        });

        btEncryptOnReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                           for (Message  lastMail:utilities.getMessages()) {


                               // Message lastMail = utilities.getLastMail();
                               if (!utilities.isEncrypted(lastMail)) {
                                   byte[] plainMail = utilities.readPlainMailAsByteArray(lastMail);

                                   utilities.deleteMail(lastMail);

                                   Message encryptedMessage = utilities.createEncryptedMail(plainMail);
                                   utilities.appendSingleMail(encryptedMessage);
                               } else {
                                Log.d("Message Encrypted","Message is already encrypted");
                               }
                           }
                        Thread.sleep(5000);
                    } else{
                               Thread.sleep(5000);
                           }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }).start();

            }
        });


        btEncryptAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Message is already encrypted", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                    });
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();

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

        btDecryptAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            utilities.Authenticate();
                            utilities.createConfig();
                            utilities.readMails();
                                    for (Message  encryptedMail:utilities.getMessages()) {

                                        if (utilities.isEncrypted(encryptedMail)) {
                                            String decryptedMailContent = utilities.readEncryptedMail(encryptedMail);

                                            utilities.deleteMail(encryptedMail);

                                            Message decryptedMail = utilities.createDecryptedMail(decryptedMailContent);
                                            utilities.appendSingleMail(decryptedMail);
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this, "Message is already encrypted", Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                            });
                                        }
                            }

                        } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        }
    });

    }
}