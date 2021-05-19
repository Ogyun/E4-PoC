package com.example.E4_PoC;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class EmailUtilities {

   private String email;
   private String password;
   private Store store;
   private Session session;
   private Session smtpSession;
   private Activity activity;
   private Properties properties;
   private Message messages[];
   private byte[] salt;
   private int iterations  = 5000;
   private SecretKeySpec keySpec;
   private Folder inbox;
   private String encryptionPassword;

    public EmailUtilities(Activity activity,EmailAccount account){
       this.email = account.getEmailAddress();
       this.password = account.getPassword();
       this.activity = activity;
       this.encryptionPassword = account.getEncryptionPassword();
   }

   /* protected EmailUtilities(Parcel in) {
        email = in.readString();
        password = in.readString();

    }

    public static final Creator<EmailUtilities> CREATOR = new Creator<EmailUtilities>() {
        @Override
        public EmailUtilities createFromParcel(Parcel in) {
            return new EmailUtilities(in);
        }

        @Override
        public EmailUtilities[] newArray(int size) {
            return new EmailUtilities[size];
        }
    };

    @Override
    public int describeContents() {return 0;}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(password);
    }
*/
    public Message [] getMessages(){
        return messages;
    }

    public boolean Authenticate(){
        boolean authenticated = false;
       properties = new Properties();
       properties.setProperty("mail.host", "imap.gmail.com");
       properties.setProperty("mail.port", "993");
       properties.setProperty("mail.transport.protocol", "imaps");
       properties.put("mail.imaps.starttls.enable", "true");

       session = Session.getInstance(properties,
               new javax.mail.Authenticator() {
                   protected PasswordAuthentication getPasswordAuthentication() {
                       return new PasswordAuthentication(email, password);
                   }
               });

       //Start a debug session
       session.setDebug(true);

       try {
               store = session.getStore("imaps");
               store.connect();
               authenticated =true;

          } catch (AuthenticationFailedException e){
           e.printStackTrace();
           authenticated = false;
           return authenticated;
       }
       catch (Exception e) {
           e.printStackTrace();

       }
        return authenticated;
   }

   public Message getLastMail() throws MessagingException {
       Message message = inbox.getMessage(messages.length);
       return message;
   }
   public Message getMail(int mailNumber) throws MessagingException {
        Message message = inbox.getMessage(mailNumber);
        return message;
   }
   public boolean isEncrypted(Message message) throws MessagingException {

        boolean isEncrypted = false;
        if(message.isExpunged()){
            Log.d("Message expunged","Message expunged");
            isEncrypted=true;
            return isEncrypted;
        }
        String[] decriptionHeader = message.getHeader("Content-Description");
        if (decriptionHeader==null){
            Log.d("Description: ","Description is null or empty");
            return isEncrypted;
        } else{
            String description = decriptionHeader[0];

            if (description == null||description.isEmpty()){
                Log.d("Description: ","Description is null or empty");
                return isEncrypted;
            }
            else if(description.contains("Encrypted")){
                isEncrypted = true;
                Log.d("Description: ",description);
                return isEncrypted;
            }
            else{
                isEncrypted = false;
                return  isEncrypted;

            }
        }

   }

   public void deleteMail(Message message) throws MessagingException {
       //It deletes only if the message is unseen
       message.setFlag(Flags.Flag.DELETED, true);
   }

   //Might need to change the type from Message to MimeMessage
   public byte[] readPlainMailAsByteArray(Message plainMessage) throws IOException, MessagingException {
       //Read message content as byteArray
       InputStream stream = plainMessage.getInputStream();
       byte[] bytes = IOUtils.toByteArray(stream);
       return bytes;

   }

   public String readEncryptedMail(Message encryptedMessage) throws MessagingException, IOException {


           //Check the content type of the message
           Log.d("Content type",encryptedMessage.getContentType());

           //Create multipart
           Multipart multipart = (Multipart) encryptedMessage.getContent();

           //Print out the name of the first part - IV file
           Log.d("First part" ,multipart.getBodyPart(0).getFileName());

           //Read the content of the IV file
           Log.d("First content" , multipart.getBodyPart(0).getContentType());

           //Pass the content of the IV file to a byteArray
            InputStream stream  =  multipart.getBodyPart(0).getInputStream();
            byte[] ivByteArray = IOUtils.toByteArray(stream);

           //Convert ivBytes to IVSpec and return it
           IvParameterSpec ivSpec = convertBytesToIVParameterSpec(ivByteArray);


           //Print out the name of the encrypted file
           Log.d("Second part" ,multipart.getBodyPart(1).getFileName());

           //Print out the content of the encrypted file
           Log.d("Second content" , (String) multipart.getBodyPart(1).getContent());

           //Pass the content of the encrypted file to String, could be converted to byte array instead
           String st = (String) multipart.getBodyPart(1).getContent();

           /*BodyPart bd = (BodyPart) multipart.getBodyPart(1).getContent();
           bd.getContentType();
           */

           byte[] encryptedTextBytes = decoderfun(st);


           //Return here
           String decryptedText = decrypt(encryptedTextBytes,ivSpec);
           Log.d("Decrypt",decryptedText);
           return decryptedText;
   }

   public Message createEncryptedMail(byte[] plainText) throws MessagingException {


        //Create a new encrypted message
       Message message = new MimeMessage(session);

       // Create a multi-part
       Multipart multipart = new MimeMultipart();

       //Create IV bodyPart
       MimeBodyPart ivBodyPart = new MimeBodyPart();

       //Generate IV byteArray
       byte[] ivByteArray = generateIV();

       //Pass ivByteArray to ByteArrayDataSource
       ByteArrayDataSource bds = new ByteArrayDataSource(ivByteArray, "application/octet-stream");
       ivBodyPart.setDataHandler(new DataHandler(bds));
       ivBodyPart.setFileName("IV");

       //Add the IV bodyPart to multipart
       multipart.addBodyPart(ivBodyPart);

            //Also works
/*          attachmentBodyPart.setContent(ivContentByteArray,"application/octet-stream");
            attachmentBodyPart.setFileName("IV");
            multipart.addBodyPart(attachmentBodyPart);*/

       //Create an encrypted text bodyPart
       MimeBodyPart textBodyPart = new MimeBodyPart();

       //Convert ivByteArray to IVSpec
       IvParameterSpec ivSpec = convertBytesToIVParameterSpec(ivByteArray);

       //Encrypt byte array
       String encryptedText = encrypt(plainText,ivSpec);

       textBodyPart.setContent(encryptedText,"text/plain");//application/x-any creates no name file
       textBodyPart.setFileName("Encrypted");
       //Add the encrypted text to multipart
       multipart.addBodyPart(textBodyPart);

        //Set Content-Description header of  the  message to Encrypted
       message.setDescription("Encrypted");

       //Finally add the multipart to message
       message.setContent(multipart);

       return message;

   }
   public Message createDecryptedMail(String content) throws MessagingException {
       //Create a new message
       Message message = new MimeMessage(session);
       Multipart mp = new MimeMultipart();
       MimeBodyPart bd = new MimeBodyPart();
       bd.setContent(content,"text/plain");
       mp.addBodyPart(bd);
       message.setContent(mp);
       return message;
   }

   public void appendSingleMail(Message message) throws MessagingException {
       //Create new message array and pass it to the inbox
       Message newMessages[] = new Message[1];
       newMessages[0] = message;
       inbox.appendMessages(newMessages);
   }

    public void appendMultipleMails(Message[]messages) throws MessagingException {
        inbox.appendMessages(messages);
    }

    public void readMails() {

             //Read mails from trash folder
            /*Folder inbox = store.getFolder("[Gmail]/Trash");
              inbox.open(Folder.READ_WRITE);
              Message messages[] = inbox.getMessages();*/

        try {
            //Get all messages from inbox folder
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            messages = inbox.getMessages();

            //inbox.close(true);
           // store.close();
        }
        catch (MessagingException e){
            e.printStackTrace();
        }

   }

   public boolean hasNewMail() throws MessagingException {
        boolean newMessages = false;
        messages = inbox.getMessages();
        if (messages!=null || messages.length>0){
            newMessages=true;
            return newMessages;
        }
        return newMessages;

   }
   public void createConfig(){

        try
        {
            //Create a config folder with a config message
            Folder configFolder = store.getFolder("CONFIG");
            if (!configFolder.exists()) {
                if (configFolder.create(Folder.HOLDS_MESSAGES)) {
                    configFolder.setSubscribed(true);
                    Log.d("Folder created","Folder was created successfully");

                    //Generate salt
                    byte[] saltBytes = generateSalt();

                    //Create a config  message
                    Message configMessage = new MimeMessage(session);

                    //Create multipart
                    Multipart multipart = new MimeMultipart();

                    //Create bodyPart for salt
                    MimeBodyPart saltBodyPart = new MimeBodyPart();

                    //Create byteArray dataSource
                    ByteArrayDataSource bds = new ByteArrayDataSource(saltBytes, "application/octet-stream");
                    saltBodyPart.setDataHandler(new DataHandler(bds));
                    saltBodyPart.setFileName("Salt");

                    //Add the Salt bodyPart to multipart
                    multipart.addBodyPart(saltBodyPart);

                    //Set message From
                    configMessage.setFrom(new InternetAddress(email));

                    configMessage.setContent(multipart);

                    Message newMessages[] = new Message[1];
                    newMessages[0] = configMessage;
                    configFolder.appendMessages(newMessages);

                    salt = saltBytes;
                    keySpec = generateSecretKey(encryptionPassword,salt,iterations);

                }
            } else {
                Log.d("Folder - ","CONFIG Folder already exist");
                configFolder.open(Folder.READ_WRITE);
                Message messages [] = configFolder.getMessages();

                //Get the last message
                Message m = configFolder.getMessage(messages.length);

                //Read salt from the Config folder
                 byte saltFromServer[] = readConfig(m);

                 //Generate secret key
                 keySpec = generateSecretKey(encryptionPassword,saltFromServer,iterations);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

   }

   private byte [] readConfig(Message  configMessage) throws IOException, MessagingException {

       //Create multipart
       Multipart multipart = (Multipart) configMessage.getContent();

       //Pass the content of the IV file to a byteArray
       InputStream stream  =  multipart.getBodyPart(0).getInputStream();
       byte[] saltByteArray = IOUtils.toByteArray(stream);

       return saltByteArray;
   }


    private byte[] generateSalt(){
        //Random salt for next step
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    private byte[] generateIV(){

        //Create initialization vector for AES
        SecureRandom ivRandom = new SecureRandom();
        byte[] iv = new byte[16];
        ivRandom.nextBytes(iv);
        return iv;
    }

    private IvParameterSpec convertBytesToIVParameterSpec(byte[] ivBytes){
       IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
       return ivSpec;
    }

    private SecretKeySpec generateSecretKey(String password, byte[] salt, int iterationCount){
        SecretKeySpec keySpec = null;
        try {
           //Convert password to char array
           char[] pwdCharArr = password.toCharArray();

           //PBKDF2 - derive the key from the password
           PBEKeySpec pbeKeySpec = new PBEKeySpec(pwdCharArr, salt, iterationCount, 256);
           SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA256");
           byte[] keyBytes = secretKeyFactory.generateSecret(pbeKeySpec).getEncoded();
           keySpec = new SecretKeySpec(keyBytes, "AES");

       } catch (Exception e){
           e.printStackTrace();
       }

        return keySpec;
    }

    private String encrypt(byte[] plainText, IvParameterSpec ivSpec) {

        String encryptedText = "";
        try {
            //Encrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

            // Log.d("Provider", cipher.getProvider().getName());

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedTextBytes = cipher.doFinal(plainText);

            encryptedText = encoderfun(encryptedTextBytes);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return encryptedText;

    }


    private String decrypt(byte[] encryptedTextBytes, IvParameterSpec ivSpec) {
        String plainText = "";
        try {

            //Decrypt
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedByteArray = cipher.doFinal(encryptedTextBytes);

            plainText = new String(decryptedByteArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return plainText;
    }

    private static String encoderfun(byte[] decval) {
        String conVal = Base64.encodeToString(decval, Base64.DEFAULT);
        return conVal;
    }
    private static byte[] decoderfun(String val){
        byte[] decodedVal = Base64.decode(val,Base64.DEFAULT);
        return  decodedVal;
    }


    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }

    public void AuthenticateSMTP(){
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        smtpSession = Session.getInstance(properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email, password);
                    }
                });

    }
    public Message createMailForSending(String to, String subject, String content) throws MessagingException {

            //create a MimeMessage object
        Message message = new MimeMessage(smtpSession);

        //set From email field
        message.setFrom(new InternetAddress(email));

        //set To email field
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(to));

        //set email subject field
        message.setSubject(subject);

        //set the content of the email message
        message.setText(content);

        return message;

    }

}

