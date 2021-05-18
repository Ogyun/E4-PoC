package com.example.javamail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import com.sun.mail.imap.IMAPStore;

import org.apache.commons.io.IOUtils;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;


public class MainActivity extends AppCompatActivity {
    HashMap<String, byte[]> map = new HashMap<>();//Creating HashMap

    Button btEncrypt;
    Button btDecrypt;
    Button btDecryptAll;
    EmailUtilities utilities;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btEncrypt = findViewById(R.id.bt_encrypt);
        btDecrypt = findViewById(R.id.bt_decrypt);
        btDecryptAll = findViewById(R.id.bt_decryptAll);


        Intent intent = getIntent();
        utilities = (EmailUtilities) intent.getParcelableExtra("emailClass");


/*        new Thread(new Runnable() {

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

        }).start();*/
/*
        Store store = utilities.getStore();
        try {
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Message messages[] = inbox.getMessages();
            for (int a = 0;a<messages.length;a++) {
                Log.d("content", getTextFromMessage(messages[a]));
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
*/

/*        sEmail= intent.getStringExtra("email");
        sPassword = intent.getStringExtra("password");
        Toast.makeText(MainActivity.this, sEmail+sPassword, Toast.LENGTH_LONG).show();*/

    /*    StringBuilder builder = new StringBuilder();
        for (Provider provider : Security.getProviders()) {
            builder.append("provider: ")
                    .append(provider.getName())
                    .append(" ")
                    .append(provider.getVersion())
                    .append("(")
                    .append(provider.getInfo())
                    .append(")\n");
        }
        String providers = builder.toString();
        Log.d("Providers",providers);
*/
/*
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while(true){
                        readMails();
                        Thread.sleep(10000);
                    }
                } catch (Exception e) {
                    Log.e("ReadMails", e.getMessage(), e);
                }
            }

        }).start();

*/


        btEncrypt.setOnClickListener(new View.OnClickListener() {
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

        btDecrypt.setOnClickListener(new View.OnClickListener() {
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
        });

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
