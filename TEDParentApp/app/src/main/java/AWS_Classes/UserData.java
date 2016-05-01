package AWS_Classes;

/**
 * Created by Niko on 1/10/2016.
 */
public class UserData {

    private String Username;
    private String Password;

    public UserData(){};
    public UserData(String User, String Pass){
        Username = User;
        Password = Pass;
    }

    public void setUsername(String User){
        Username = User;
    }

    public String getUsername(){
        return Username;
    }

    public void setPassword(String Pass){
        Password = Pass;
    }

    public String getPassword(){
        return Password;
    }


}
