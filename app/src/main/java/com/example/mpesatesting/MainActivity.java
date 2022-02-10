package com.example.mpesatesting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;

import butterknife.BindView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mpesatesting.AccessToken;
import com.example.mpesatesting.STKPush;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.example.mpesatesting.AppConstants.BUSINESS_SHORT_CODE;
import static com.example.mpesatesting.AppConstants.CALLBACKURL;
import static com.example.mpesatesting.AppConstants.PARTYB;
import static com.example.mpesatesting.AppConstants.PASSKEY;
import static com.example.mpesatesting.AppConstants.TRANSACTION_TYPE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    @BindView(R.id.etAmount)
    EditText mAmount;
    @BindView(R.id.etPhone)EditText mPhone;
    @BindView(R.id.btnPay)
    Button mPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mProgressDialog = new ProgressDialog(this);
        mApiClient = new ApiClient();
        mApiClient.setIsDebug(true); //Set True to enable logging, false to disable.

        mPay.setOnClickListener(this);

        getAccessToken();
    }

        public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
        @Override
        public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

        if (response.isSuccessful()) {
        mApiClient.setAuthToken(response.body().accessToken);

        }
    }

        @Override
        public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

        }
        });


    }
        @Override
        public void onClick(View view) {
        if (view== mPay){
        String phone_number = mPhone.getText().toString();
        String amount = mAmount.getText().toString();
        performSTKPush(phone_number,amount);
        }
        }


        public void performSTKPush(String phone_number,String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
        BUSINESS_SHORT_CODE,
        Utils.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
        timestamp,
        TRANSACTION_TYPE,
        String.valueOf(amount),
        Utils.sanitizePhoneNumber(phone_number),
        PARTYB,
        Utils.sanitizePhoneNumber(phone_number),
        CALLBACKURL,
        "MPESA Android Test", //Account reference
        "Testing"  //Transaction description
        );

        mApiClient.setGetAccessToken(false);

        //Sending the data to the Mpesa API, remember to remove the logging when in production.
        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
        @Override
        public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
        mProgressDialog.dismiss();
        try {
        if (response.isSuccessful()) {
        Timber.d("post submitted to API. %s", response.body());
        } else {
        Timber.e("Response %s", response.errorBody().string());
        }
        } catch (Exception e) {
        e.printStackTrace();
        }
        }

        @Override
        public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
        mProgressDialog.dismiss();
        Timber.e(t);
        }
        });
        }

        @Override
        public void onPointerCaptureChanged(boolean hasCapture) {

        }


}