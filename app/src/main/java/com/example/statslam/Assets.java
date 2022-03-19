package com.example.statslam;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import dev.firstseed.sports_reference.StatModel;

public class Assets
{

    final private static HashMap<String, StatModel> modelMap = new HashMap<>();



    public static ArrayList<String> getModelList(Context context)
    {
        System.out.println("Listing Assets");
        ArrayList<String> models = new ArrayList<>();
        models.add("All");
        try {
            for (String f : context.getAssets().list("")) {
                if(!f.endsWith(".json"))
                {
                    continue;
                }
                models.add(f.replace(".json", ""));

            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return models;
    }

    public static void copyTournamentToCache(Context context)throws Exception
    {
        File f = new File(DirectoryManager.CACHE_DIR(context).getAbsolutePath()+"/cbb/postseason/2022-ncaa.html");
        DirectoryManager.copyAssetUsingStream(context.getAssets().open("2022-ncaa.html"), f);
    }

    public static StatModel getModel(Context context, String name) throws Exception
    {
        if(modelMap.containsKey(name))
        {
            return modelMap.get(name);
        }


        StatModel statModel;
        name +=".json";
        InputStream is = context.getAssets().open(name);
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8);
        statModel = new StatModel(writer.toString());

        modelMap.put(name, statModel);

        return statModel;
    }
}
