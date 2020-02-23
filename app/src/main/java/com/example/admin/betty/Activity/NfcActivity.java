package com.example.admin.betty.Activity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.betty.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class NfcActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    Context context;

    TextView tvNFCContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        context = this;

        tvNFCContent = (TextView) findViewById(R.id.nfc_contents);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }


    @Override
    public void onResume(){
        super.onResume();
        if(getIntent().hasExtra(NfcAdapter.EXTRA_TAG)) {
            NdefMessage ndefMessage = this.getNdefMessageFromIntent(getIntent());
            if(ndefMessage.getRecords().length > 0){
                NdefRecord ndefRecord = ndefMessage.getRecords()[0];
                String payload = new String(ndefRecord.getPayload());
                //Toast.makeText(this, payload, Toast.LENGTH_SHORT).show();
            }
        }

        enableForegroundDispatchsystem();
    }

    @Override
    public void onPause(){
        super.onPause();
        disableForegroundDispatchsystem();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        setIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            //Toast.makeText(this, "NFCIntent", Toast.LENGTH_SHORT).show();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] payload = "com.example.admin.betty".getBytes();
            NdefRecord ndefRecord = NdefRecord.createExternal("android.com", "pkg", payload);

            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
            writeNdefMessage(tag, ndefMessage);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void enableForegroundDispatchsystem(){
        Intent intent = new Intent(this, NfcActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    private void disableForegroundDispatchsystem(){
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage){
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if(ndefFormatable == null){
                Toast.makeText(this, "Tag is not Ndef formatable", Toast.LENGTH_SHORT).show();
            }

            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
        } catch (Exception e){
            Log.e("formatTag", e.getMessage());
        }
    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage){
        try {
            if (tag == null){
                Toast.makeText(this, "Tag object cannot be null", Toast.LENGTH_SHORT).show();
                return;
            }

            Ndef ndef = Ndef.get(tag);
            if (ndef == null){
                formatTag(tag, ndefMessage);
            } else {
                ndef.connect();
                if (!ndef.isWritable()){
                    Toast.makeText(this, "Tag is not writable", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "Tag writen", Toast.LENGTH_SHORT).show();
                tvNFCContent.setText("NFC 태그가 등록되었습니다.");
            }
        } catch (Exception e){
            Log.e("writeNdefMessage", e.getMessage());
        }
    }

    public NdefMessage getNdefMessageFromIntent(Intent intent) {
        NdefMessage ndefMessage = null;
        Parcelable[] extra = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (extra != null && extra.length > 0) {
            ndefMessage = (NdefMessage) extra[0];
        }
        return ndefMessage;
    }
}