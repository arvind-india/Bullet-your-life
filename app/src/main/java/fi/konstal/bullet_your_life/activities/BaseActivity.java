package fi.konstal.bullet_your_life.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.List;

import fi.konstal.bullet_your_life.AsyncDriveDownload;
import fi.konstal.bullet_your_life.AsyncDriveUpload;
import fi.konstal.bullet_your_life.DriveDownloadListener;
import fi.konstal.bullet_your_life.DriveUploadListener;

/**
 * An abstract activity that handles authorization and connection to the Drive
 * services.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    protected static final int REQUEST_CODE_SIGN_IN = 400;
    protected static final int REQUEST_CODE_OPEN_ITEM = 401;

    private DriveClient mDriveClient;
    private static DriveResourceClient mDriveResourceClient;
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedpreferences = getSharedPreferences("bullet_your_life", Context.MODE_PRIVATE);

        // If user is authenticated, start sign in process
        if(sharedpreferences.getBoolean("is_auth", false)) {
            signIn();
        }
    }



    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    @SuppressLint("RestrictedApi")
    protected void signIn() {
        List<Scope> requiredScopes = new ArrayList<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user
                    Log.e(TAG, "Sign-in failed.");
                    return;
                }

                @SuppressLint("RestrictedApi") Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    showMessage("Google sign in failed");
                }
                break;
            case REQUEST_CODE_OPEN_ITEM:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    mOpenItemTaskSource.setResult(driveId);
                } else {
                    mOpenItemTaskSource.setException(new RuntimeException("Unable to open file"));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }


    public void downloadDriveImage(DriveId driveId, DriveDownloadListener<Bitmap> driveDownloadListener) {
        new AsyncDriveDownload(driveId, mDriveResourceClient, driveDownloadListener).execute();
    }

    public void uploadDriveImage(Context context, DriveUploadListener driveUploadListener, Uri imgUri) {
       new AsyncDriveUpload(context, imgUri, mDriveResourceClient, driveUploadListener).execute();
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    protected abstract void onDriveClientReady();

    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }
}