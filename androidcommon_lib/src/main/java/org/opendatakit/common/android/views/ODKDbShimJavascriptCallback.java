/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.common.android.views;

import org.opendatakit.common.android.activities.ODKActivity;
import org.opendatakit.common.android.utilities.WebLogger;
import org.opendatakit.common.android.utilities.WebLoggerIf;
import org.opendatakit.dbshim.service.DbShimCallback;
import org.opendatakit.dbshim.service.OdkDbShimInterface;

import android.app.Activity;
import android.os.RemoteException;

/**
 * The class mapped to 'dbshim' in the Javascript
 *
 * This class implements the W3C WebSQL interface subset that is supported by
 * ODK Survey. See dbif.js
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class ODKDbShimJavascriptCallback {

  public static final String t = "ODKDbShimJavascriptCallback";

  private ODKWebView mWebView;
  private ODKActivity mActivity;
  private final WebLoggerIf log;
  private OdkDbShimInterface mDbShimService;
  private DbShimCallback.Stub mCallbackIf = new DbShimCallback.Stub() {
    @Override
    public void fireCallback(String fullCommand) throws RemoteException {
      ODKDbShimJavascriptCallback.this.fireCallback(fullCommand);
    }
  };

  public ODKDbShimJavascriptCallback(ODKWebView webView, ODKActivity activity, OdkDbShimInterface dbShimService) {
    mWebView = webView;
    mActivity = activity;
    mDbShimService = dbShimService;
    log = WebLogger.getLogger(mActivity.getAppName());
  }

  public void fireCallback(final String fullCommand) {
    log.i(t, "fireCallback");

    Activity a = (Activity) mActivity;
    a.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if ( mWebView != null ) {
          log.i(t, "fireCallback -- Fired on UI Thread");
          mWebView.loadUrl(fullCommand);
        }
      }

    });

  }

  public void immediateRollbackOutstandingTransactions() {
    log.i(t, "immediateRollbackOutstandingTransactions");
    if ( mDbShimService == null ) {
      log.i(t, "immediateRollbackOutstandingTransactions -- mDbShimService removed");
    } else {
      // "-" clears and releases all the database handles for this application
      try {
        mDbShimService.initializeDatabaseConnections(mActivity.getAppName(), "-", getCallbackIf());
      } catch (RemoteException e) {
        log.i(t, "immediateRollbackOutstandingTransactions -- mDbShimService RemoteException");
      }
    }
  }

  @android.webkit.JavascriptInterface
  public void confirmSettings(String generation) {
    if (mWebView == null) {
      log.i(t, "confirmSettings -- interface removed");
      return;
    }

    if ( mDbShimService == null ) {
      log.i(t, "confirmSettings -- mDbShimService removed");
    } else {
      // -1 resets everything
      try {
        mDbShimService.initializeDatabaseConnections(mActivity.getAppName(), generation, getCallbackIf());
      } catch (RemoteException e) {
        log.i(t, "confirmSettings -- mDbShimService RemoteException");
      }
    }
  }

  @android.webkit.JavascriptInterface
  public void executeSqlStmt(String generation, int transactionGeneration, int actionIdx,
      String sqlStmt, String strBinds) {

    if (mWebView == null) {
      log.i(t, "executeSqlStmt -- interface removed");
      // do not invoke callback -- let this die!
      return;
    }

    if ( mDbShimService == null ) {
      log.i(t, "executeSqlStmt -- mDbShimService removed");
    } else {
      // -1 resets everything
      try {
        mDbShimService.runStmt(mActivity.getAppName(), generation, transactionGeneration, actionIdx,
          sqlStmt, strBinds, getCallbackIf());
      } catch (RemoteException e) {
        log.i(t, "executeSqlStmt -- mDbShimService RemoteException");
      }
    }
  }

  @android.webkit.JavascriptInterface
  public void rollback(String generation, int transactionGeneration) {

    if (mWebView == null) {
      log.i(t, "rollback -- interface removed");
      // do not invoke callback -- let this die!
      return;
    }

    if ( mDbShimService == null ) {
      log.i(t, "rollback -- mDbShimService removed");
    } else {
      // -1 resets everything
      try {
        mDbShimService.runRollback(mActivity.getAppName(), generation, transactionGeneration, getCallbackIf());
      } catch (RemoteException e) {
        log.i(t, "rollback -- mDbShimService RemoteException");
      }
    }
  }
  @android.webkit.JavascriptInterface
  public void commit(String generation, int transactionGeneration) {

    if (mWebView == null) {
      log.i(t, "commit -- interface removed");
      // do not invoke callback -- let this die!
      return;
    }

    if ( mDbShimService == null ) {
      log.i(t, "commit -- mDbShimService removed");
    } else {
      // -1 resets everything
      try {
        mDbShimService.runCommit(mActivity.getAppName(), generation, transactionGeneration, getCallbackIf());
      } catch (RemoteException e) {
        log.i(t, "commit -- mDbShimService RemoteException");
      }
    }
  }

  private DbShimCallback.Stub getCallbackIf() {
    return mCallbackIf;
  }

}