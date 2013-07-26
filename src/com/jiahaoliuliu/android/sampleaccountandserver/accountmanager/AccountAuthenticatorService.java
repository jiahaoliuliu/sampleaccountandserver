package com.jiahaoliuliu.android.sampleaccountandserver.accountmanager;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Authenticator service that returns a subclass of AbstractAccountAuthenticator in onBind().
 */
public class AccountAuthenticatorService extends Service {

    /**
     * The tag used for the logs.
     */
    private static final String LOG_TAG = AccountAuthenticatorService.class.getSimpleName();

    /**
     * The implementation of the class |AccountAuthenticatorImpl|.
     * It is implemented as a singleton
     */
    private static AccountAuthenticatorImpl sAccountAuthenticator = null;

    /**
     * The main constructor.
     */
    public AccountAuthenticatorService() {
        super();
    }

    /**
     * The bind method of the service.
     * @param intent The intent used to invoke the service
     * @return The binder of the class which has implemented |AbstractAccountAuthenticator|
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(AccountAuthenticatorService.LOG_TAG, "Binding the service");
        IBinder ret = null;
        if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            ret = getAuthenticator().getIBinder();
        }
        return ret;
    }

    /**
     * The method used to obtain the authenticator. It is implemented as a singleton
     * @return The implementation of the class |AbstractAccountAuthenticator|
     */
    private AccountAuthenticatorImpl getAuthenticator() {
        if (AccountAuthenticatorService.sAccountAuthenticator == null) {
            AccountAuthenticatorService.sAccountAuthenticator = new AccountAuthenticatorImpl(this);
        }

        return AccountAuthenticatorService.sAccountAuthenticator;
    }

    /**
     * The class which implements the class |AbstractAccountAuthenticator|.
     * It is the one which the Android system calls to perform any action related with the account
     */
    private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {

        /**
         * The Context used.
         */
        private final Context mContext;

        /**
         * The main constructor of the class.
         * @param context The context used
         */
        public AccountAuthenticatorImpl(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response,
                String accountType,
                String authTokenType,
                String[] requiredFeatures,
                Bundle options) throws NetworkErrorException {
            Log.d(AccountAuthenticatorService.LOG_TAG, "Adding new account");
            Bundle reply = new Bundle();

            Log.d(AccountAuthenticatorService.LOG_TAG, "The auth token type is " + authTokenType);
            Intent i = new Intent(mContext, AddNewAccountActivity.class);
            i.setAction("com.gowex.pista.addnewaccount");
            i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            i.putExtra("AuthTokenType", authTokenType);
            reply.putParcelable(AccountManager.KEY_INTENT, i);

            return reply;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse arg0, Account arg1,
                Bundle arg2) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse arg0, String arg1) {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse arg0, Account arg1,
                String arg2, Bundle arg3) throws NetworkErrorException {
            return null;
        }

        @Override
        public String getAuthTokenLabel(String arg0) {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse arg0, Account arg1,
                String[] arg2) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse arg0, Account arg1,
                String arg2, Bundle arg3) throws NetworkErrorException {
            return null;
        }
    }
}

