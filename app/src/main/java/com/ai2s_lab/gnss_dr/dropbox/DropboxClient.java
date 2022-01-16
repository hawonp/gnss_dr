package com.ai2s_lab.gnss_dr.dropbox;

import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DropboxClient {

    private static final String ACCESS_TOKEN = "sl.BAMLpdEf27iUI3EVWhs62eMdEYF49B3vR9-cL7Vhdnu__cthgRkfgVuXAHTMfncNhTp6U7ZORcUYhdwSKW44P9xoNxBSI55C9kuCc7W71egdX7CHF0ORBWqr--dPtHgYNfkDc0jsfICt";

    private DbxRequestConfig dbxRequestConfig;
    private DbxClientV2 dbxClientV2;

    public DropboxClient(){
        dbxRequestConfig = DbxRequestConfig.newBuilder("gnss").build();
        dbxClientV2 = new DbxClientV2(dbxRequestConfig, ACCESS_TOKEN);

        try {
            FullAccount account = dbxClientV2.users().getCurrentAccount();
            Log.d("DROPBOX", account.getName().getDisplayName());
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public boolean uploadFile(String file_path, String file_name){
        try (InputStream inputStream = new FileInputStream(file_path)){
            FileMetadata metadata = dbxClientV2.files().uploadBuilder("/" + file_name).uploadAndFinish(inputStream);
            return true;
        } catch (UploadErrorException e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
