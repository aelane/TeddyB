package AWS_Classes;

/**
 * Created by Niko on 1/12/2016.
 */
public class LoginResponse {
    private Boolean Success;
    private String ID;
    private String Token;

    public LoginResponse(){};

    public void setSuccess(Boolean bool){
        Success = bool;
    }

    public Boolean getSuccess(){
        return Success;
    }

    public void setID(String id){
        ID = id;
    }

    public String getID(){
        return ID;
    }

    public void setToken(String token){
        Token = token;
    }

    public String getToken(){
        return Token;
    }


}
