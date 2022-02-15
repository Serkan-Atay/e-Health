package com.gui.ehealt_v2;
/**
 * Class allows for first time registration of a user (as well as checking user is not registering double using email),
 * user inputs information and it is inserted into DB
 * Geocoding data (longitude and latitude) are also extracted using user input and saved in DB
 * DB connection achieved with JDBC driver
 * DB: MYSQL, localhost on each pc
 * After registration immediate return to login page for login
 * @author Amalie Wilke; StudentID: 1304925
 */

import Encryption.HashClass;
import GoogleMaps.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import org.w3c.dom.Text;

import java.sql.*;

public class RegistrationController {
    String db_password = "hells";  //

    final private HashClass hash = new HashClass();
    final private SceneController controller = new SceneController();
    final private HTTPconnection httpcon=new HTTPconnection();
    private GeocodeCoordinates latlongcoord=new GeocodeCoordinates();

    // ImageView
    @FXML
    private ImageView durtleImageView;

    //Registration Form FXML Elements
    @FXML
    private Button registration_button;
    @FXML
    private TextField firstname_textfield;

    @FXML
    private TextField lastname_textfield;

    @FXML
    private TextField street_textfield;

    @FXML
    private TextField number_textfield;

    @FXML
    private TextField zip_textfield;
    @FXML
    private TextField town_textfield;
    @FXML
    private DatePicker birthday;
    @FXML
    private TextField insurance_textfield;
    @FXML
    private ComboBox <String> insurance_type;
    @FXML
    private TextField new_email_textfield;

    @FXML
    private TextField new_password_textfield;

    String [] insurancetype={"Public", "Private"};

    public void setAll(){
        insurance_type.setItems(FXCollections.observableArrayList(insurancetype));
    }

    public void registration(ActionEvent event) throws Exception{


        Window owner=registration_button.getScene().getWindow();
        if(firstname_textfield.getText().isEmpty()){
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "First Name needs to be entered");
            return;
        }

        if(lastname_textfield.getText().isEmpty()){
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Last Name needs to be entered");
            return;
        }

        if(street_textfield.getText().isEmpty() || number_textfield.getText().isEmpty() || zip_textfield.getText().isEmpty() || new_email_textfield.getText().isEmpty() ||new_password_textfield.getText().isEmpty() || insurance_textfield.getText().isEmpty()){
            showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Registration fields not complete yet");
            return;
        }

        String fn=firstname_textfield.getText();
        String ln=lastname_textfield.getText();
        String str=street_textfield.getText();
        String hn=number_textfield.getText();
        String zi=zip_textfield.getText();
        String to=town_textfield.getText();
        Date bd=Date.valueOf(birthday.getValue());
        String in=insurance_textfield.getText();
        String em=new_email_textfield.getText();
        String npw=new_password_textfield.getText();
        System.out.println(fn);

        //additional formatting for address entries in case there are spaces
        String str_nospaces=str.replaceAll("\\s","");  //String street address without spaces
        String to_nospaces=to.replaceAll("\\s","");    //String town address without spaces

        //connection for the DB
        Connection connection = null;
        PreparedStatement Insert = null;
        PreparedStatement DoesUserExist=null;
        ResultSet resultSet=null;

        try {
            connection= DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/ehealth_db", "ehealth", db_password); //localhost:3306/
            System.out.println("Successful DB connection");
            DoesUserExist=connection.prepareStatement("SELECT * FROM users WHERE Email=?");
            DoesUserExist.setString(1, em);
            resultSet=DoesUserExist.executeQuery();  //never forget executeQuery because otherwise everything does not work
            System.out.println("Check if account exists");

            if(resultSet.isBeforeFirst()){
                System.out.println("User already exists");  //wouldn't this be a security thing
                showAlert(Alert.AlertType.ERROR, owner, "Form Error", "Unable to register, user already exists");
                return;
            }

            else {
                transformAddress(str_nospaces, hn, zi, to_nospaces);  //Transforms string formats to ints when necessary and passes them to parsing method, final coordinates latitude and longtitude are saved in object GeoCoordinates
                Insert = connection.prepareStatement("INSERT INTO users (FirstName, LastName, Street, HouseNumber, ZIP, Town, BirthDate, Email, Kennwort, InsuranceName, Latitude, Longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                Insert.setString(1, fn);
                Insert.setString(2, ln);
                Insert.setString(3, str);
                Insert.setString(4, hn);
                Insert.setString(5, zi);
                Insert.setString(6, to);
                Insert.setDate(7, bd);
                Insert.setString(8, em);
                Insert.setString(9, hash.getHash(npw));
                Insert.setString(10, in);
                Insert.setFloat(11, latlongcoord.getLatitude());
                Insert.setFloat(12, latlongcoord.getLongitude());
                Insert.executeUpdate();


                controller.switchToLogin(event);  //have Viktor check if this controller switch is elegant enough
                System.out.println("Final Object test, latitude is:" + latlongcoord.getLatitude());
            }

        }catch(SQLException ex){
            System.out.println("Unsuccessful connection");
        }
        
        connection.close();
    }


    public static void showAlert(Alert.AlertType alertType, Window owner, String s, String alertmessage) {

        Alert alert= new Alert(alertType);
        alert.setTitle(s);
        alert.setHeaderText(null);
        alert.setContentText(alertmessage);
        alert.initOwner(owner);
        alert.show();
    }

    //method to get the latitude and longitude of the user input to save in DB parallel to normal address format for future calculations for radius search
    public void transformAddress(String str, String hn, String zi, String to){

        try{
            int hnum=Integer.parseInt(hn);  //transforming string values saved in DB to int for Google API
            int z=Integer.parseInt(zi);     //transforming string values saved in DB to int for Google API
            System.out.println("Check intger parsing was correct:"+hnum+" "+z);
            latlongcoord=httpcon.createRequest(str, hnum, z, to);  //uses the normal
        }catch(NumberFormatException ex){
            ex.printStackTrace();
        }
    }

    public void setImage(Image image){
        durtleImageView.setImage(image);
    }

}