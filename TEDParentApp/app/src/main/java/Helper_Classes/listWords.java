package Helper_Classes;

import android.app.Application;

import java.util.List;

/**
 * Created by pwdarby on 4/29/16.
 */
public class listWords extends Application {
    public static String displayWords(List<String> words){
        String newString = "";
        for (String word: words){
            newString += " " + word + "\n";
        }
        return newString;
    }
}
