package com.tinnvec.dctvandroid;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by kev on 11/14/16.
 */

public class PropertyReader {

    private Context context;

    public PropertyReader(Context context){
        this.context=context;
    }

    public Properties getMyProperties(String file){
        Properties properties = new Properties();
        try{
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(file);
            properties.load(inputStream);

        }catch (Exception e){
            System.out.print(e.getMessage());
        }

        return properties;
    }
}
