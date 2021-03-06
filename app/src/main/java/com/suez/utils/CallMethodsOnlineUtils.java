package com.suez.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.odoo.BaseAbstractListener;
import com.odoo.R;
import com.odoo.core.orm.OModel;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.utils.OResource;
import com.odoo.datas.OConstants;
import com.suez.SuezConstants;

import java.util.HashMap;

/**
 * Created by joseph on 18-5-11.
 */

public class CallMethodsOnlineUtils {
    private static final String TAG = CallMethodsOnlineUtils.class.getSimpleName();
    private Context mContext;
    private OModel mModel;
    private String method;
    private OArguments args;
    private HashMap<String, Object> context;
    private HashMap<String, Object> kwargs;
    private BaseAbstractListener listener;

    public CallMethodsOnlineUtils(OModel model, String method, OArguments args, HashMap<String, Object> context, HashMap<String, Object> kwargs) {
        this.mContext = model.getContext();
        this.mModel = model;
        this.method = method;
        this.args = args;
        this.context = context;
        this.kwargs = kwargs;
    }

    public CallMethodsOnlineUtils(OModel model, String method, OArguments args, HashMap<String, Object> context) {
        this(model, method, args, context, null);
    }

    public CallMethodsOnlineUtils(OModel model, String method, OArguments args) {
        this(model, method, args, null);
    }

    public CallMethodsOnlineUtils setListener(BaseAbstractListener listener) {
        this.listener = listener;
        return this;
    }

    public void callMethodOnServer(boolean showDialog) {
        CallMethodTask task = new CallMethodTask();
        task.setDialogVisible(showDialog);
        task.execute();
    }

    public void callMethodOnServer() {
        callMethodOnServer(true);
    }

    public class CallMethodTask extends AsyncTask<Void, Void, Object> {
        private ProgressDialog dialog;
        private boolean dialogVisible = true;

        @Override
        protected Object doInBackground(Void... voids) {
            try {
                int retry = 0;
                Object obj = null;
                while (obj == null || String.valueOf(obj).equals("false") && retry <= SuezConstants.RPC_MAX_RETRY) {
                    obj = mModel.getServerDataHelper().callMethod(method, args, context, kwargs);
                    retry ++ ;
                }
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!dialogVisible) {
                return;
            }
            dialog = new ProgressDialog(mContext);
            dialog.setTitle(R.string.title_please_wait);
            dialog.setMessage(OResource.string(mContext, R.string.title_searching));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (listener != null) {
                listener.OnSuccessful(o);
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        }

        private void setDialogVisible(boolean visible) {
            dialogVisible = visible;
        }
    }
}
