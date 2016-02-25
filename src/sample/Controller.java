package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.events.Event;
import org.xml.sax.*;
import org.w3c.dom.*;

public class Controller implements Initializable{

    {int isItPrivate = 0;}

    /** <by Carl> */
    @FXML private AnchorPane anchorPane;
    @FXML private GridPane gridPane;
    private Stage stage;
    private Scene scene;
    Timeline timeline=new Timeline();
    private int RowNumber = 9//1-99 8-24suggested
            ,ColNumber = 9 //7-99 8-45suggested
            ,MineNumber = 10;//1 - 1/3 space 1/12 - 1/4 suggested
    private int XMax,YMax;

    private int[][] map= new int[RowNumber][ColNumber];//9:mine,0-8:mines nearby;row,col
    private int[][] map_status = new int[RowNumber][ColNumber];
    /**0:unclicked
     * 1:clicked(if clicked,show the picture of the number on it or the mine )
     * 2:flagged
     * 3:exploded mine
     * 4:wrong flag
     * 5:question mark
     */
    private Image[] f_image=new Image[5],b_image=new Image[10],s_image=new Image[6],t_image=new Image[11];
    //Image[] covers=new Image[8];
    private ImageView faces;
    private ImageView[] times = new ImageView[3],mine_lefts = new ImageView[3];
    private ImageView[][] images=new ImageView[RowNumber][ColNumber];//row,col

    private boolean isLose,is_face_pressed,is_face_eat,isFirstPlay=true;
    private int face;//0:smile 1:win 2:lose 3:eat 4:smile_press
    private int unclicked;//when unclicked=MineNumber and not lose, win
    private int mine_left;//show on scoreboard

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for(int i=0;i<11;i++){
            if(i<5) f_image[i]=new Image("f"+i+".png");
            if(i<6&&i!=1) s_image[i]=new Image("s"+i+".png");
            if(i<10) b_image[i]=new Image("b"+i+".png");
            if(i<11) t_image[i]=new Image("t"+i+".png");
        }//get images

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(1000), e -> {scene=gridPane.getScene();stage=(Stage)scene.getWindow();initializeKey();}));
        tl.setCycleCount(1);tl.play();

        initializePane();
        /**
        Stage secondary = new Stage();
        GridPane second = new GridPane();
        second.add(new ImageView(b_image[9]),0,0);
        secondary.setScene(new Scene(new GridPane(),100,500));
        secondary.show();
        */

    }
    public void initializePane(){
        anchorPane.getChildren().remove(0,anchorPane.getChildren().size());

        XMax=ColNumber*32+32;YMax=RowNumber*32+100;
        anchorPane.setPrefSize(XMax,YMax);
        gridPane.setMaxSize(XMax,YMax+52);
        if(!isFirstPlay)
            Main.update_root(stage);
        isFirstPlay=false;

        for(int i=0;i<3;i++){
            mine_lefts[i]=new ImageView();
            mine_lefts[i].setX(71 - i * 26);
            mine_lefts[i].setY(19);
            anchorPane.getChildren().add(mine_lefts[i]);

            times[i]=new ImageView();
            times[i].setX(XMax - 45 - i * 26);
            times[i].setY(19);
            anchorPane.getChildren().add(times[i]);
        }//time and mine_left

        faces = new ImageView();
        faces.setX(XMax/2-26);
        faces.setY(16);
        anchorPane.getChildren().add(faces);//face

        images=new ImageView[RowNumber][ColNumber];
        map= new int[RowNumber][ColNumber];
        map_status = new int[RowNumber][ColNumber];
        for(int row=0;row<RowNumber;row++)
            for(int col=0;col<ColNumber;col++){
                images[row][col]=new ImageView();
                images[row][col].setX(16+col*32);
                images[row][col].setY(84+row*32);
                anchorPane.getChildren().add(images[row][col]);
            }//buttons
        reset();
    }
    public void initializeKey(){
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown())
                switch (e.getCode()) {
                    case DIGIT1: newGameBeginner(); break;
                    case DIGIT2: newGameIntermediate(); break;
                    case DIGIT3: newGameExpert(); break;
                    case N: newGame(); break;
                    case Q: quit(); break;
                    case C:
                        try {
                            newGameCustom();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case H:
                        try {
                            help();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case A:
                        try {
                            about();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case B: board(); break;
                }
        });
    }
    public void beforeShow(){
        //stage=(Stage)
        //gridPane.getScene().getWindow();
        //scene=gridPane.getScene();
        //Timeline tl = new Timeline(new KeyFrame(Duration.millis(3000), ae -> {stage=(Stage)gridPane.getScene().getWindow();p("time");}));
        //tl.setCycleCount(1);tl.play()
    }

    public void mouse_drag(MouseEvent ME) {
        int zone=get_zone(ME),row=yTOrow(ME),col=xTOcol(ME);

        if(ME.getButton()  == MouseButton.PRIMARY && !ME.isControlDown() && !ME.isShiftDown()) {
            if(zone==2){ if(!is_face_pressed) {faces.setImage(f_image[4]); is_face_pressed=true;}}
            else if(is_face_pressed) {update_face(); is_face_pressed=false;}
            if(zone==1) { button_release(row,col); button_press(row,col);}
            else {button_release(row,col); last_press=null;}
            if(zone==1) {if(!is_face_eat) {faces.setImage(f_image[3]); is_face_eat=true;}}
            else if(is_face_eat) {update_face(); is_face_eat=false;}
        }
        if(ME.getButton()  == MouseButton.MIDDLE || (ME.getButton() == MouseButton.PRIMARY && !ME.isControlDown() && ME.isShiftDown())){
            if(zone==1) {button_middle_release(row,col);button_middle_press(row,col);}
            else {button_middle_release(row,col);  last_press=null;}
            if(zone==1) {if(!is_face_eat) {faces.setImage(f_image[3]); is_face_eat=true;}}
            else if(is_face_eat) {update_face(); is_face_eat=false;}
        }


    }
    public void mouse_press(MouseEvent ME) {
        int zone=get_zone(ME),row=yTOrow(ME),col=xTOcol(ME);

        if(ME.getButton()  == MouseButton.PRIMARY && !ME.isControlDown() && !ME.isShiftDown()) {
            if(zone==2) faces.setImage(f_image[4]);
            if(zone==1) button_press(row,col);
            if(zone==1) {faces.setImage(f_image[3]); is_face_eat=true;}
        }
        if(ME.getButton()  == MouseButton.SECONDARY || (ME.getButton() == MouseButton.PRIMARY && ME.isControlDown() && !ME.isShiftDown())) {
            if(zone==1) right_click(row,col);
        }
        if(ME.getButton()  == MouseButton.MIDDLE || (ME.getButton() == MouseButton.PRIMARY && !ME.isControlDown() && ME.isShiftDown())){
            if(zone==1) button_middle_press(row,col);
            if(zone==1) {faces.setImage(f_image[3]); is_face_eat=true;}

        }
    }
    public void mouse_release(MouseEvent ME) {
        int zone=get_zone(ME),row=yTOrow(ME),col=xTOcol(ME);

        if(ME.getButton() == MouseButton.PRIMARY && !ME.isControlDown() && !ME.isShiftDown())  {
            if(zone==1) {button_release(row,col); left_click(row,col); last_press=null;}
            if(zone==2) reset();
        }
        if(ME.getButton()  == MouseButton.MIDDLE || (ME.getButton() == MouseButton.PRIMARY && !ME.isControlDown() && ME.isShiftDown())){
            if(zone==1) {button_middle_release(-1,-1); middle_click(row,col); last_press=null;}
        }

        if(isLose) loseCheck();
        if(face!=0) timeline.stop();
        if(is_face_eat) is_face_eat=false;
        update_face();
    }

    private void left_click(int row,int col){
        if(map_status[row][col]==0){
            if(unclicked==RowNumber*ColNumber){//if unclicked, generate map
                for(int i=0;i<MineNumber;i++){//ten mines
                    int ro=(int)(RowNumber*Math.random());
                    int co=(int)(ColNumber*Math.random());
                    if(map[ro][co]==9||(ro==row&&co==col))
                        i-=1;
                    else {
                        map[ro][co]=9;
                        for(int r=-1;r<2;r++)
                            for(int c=-1;c<2;c++)
                                if(!(ro+r<0||ro+r>=RowNumber||co+c<0||co+c>=ColNumber))
                                    if(map[ro+r][co+c]!=9)
                                        map[ro+r][co+c]+=1;
                    }
                }
                timeline.play();//start counting time
            }

            if(map_status[row][col]==0){//otherwise
                map_status[row][col]=1;
                unclicked-=1;
                if(map[row][col]==0)
                    for(int r=-1;r<2;r++)
                        for(int c=-1;c<2;c++)
                            if(!(row+r<0||row+r>=RowNumber||col+c<0||col+c>= ColNumber))
                                if(map_status[row+r][col+c]==0)
                                    left_click(row+r,col+c);
                update_button(row, col);
            }

            if(map[row][col]==9){//Boom!
                map_status[row][col]=3;
                update_button(row, col);
                isLose=true;
                return;
            }

            win_check();
        }
    }
    private void right_click(int row,int col){
        if(!(map_status[row][col]==0||map_status[row][col]==2||map_status[row][col]==5)) return;

        if(map_status[row][col]==0){
            map_status[row][col]=2;
            mine_left-=1;
        }
        else if(map_status[row][col]==2){
            map_status[row][col]=5;
            mine_left+=1;
        }
        else if(map_status[row][col]==5) {
            map_status[row][col] = 0;
        }
        update_button(row, col);
        update_mine_left();
    }
    private void middle_click(int row, int col){
        if(map_status[row][col]==1){
            int flag_around=0;
            for(int r=-1;r<2;r++)
                for(int c=-1;c<2;c++)
                    if(!(row+r<0||row+r>=RowNumber||col+c<0||col+c>=ColNumber))
                        if(map_status[row+r][col+c]==2)
                            flag_around++;
            if(flag_around==map[row][col])
                for(int r=-1;r<2;r++)
                    for(int c=-1;c<2;c++)
                        if(!(row+r<0||row+r>=RowNumber||col+c<0 || col + c >= ColNumber))
                            if(map_status[row+r][col+c]==0)
                                left_click(row+r,col+c);
        }
    }

    private void win_check(){
        if(unclicked==MineNumber){//win!
            for(int row=0;row<RowNumber;row++)
                for(int col=0;col<ColNumber;col++)
                    if(map[row][col]==9){
                        map_status[row][col]=2;
                        update_button(row, col);
                    }
            face=1;
        }
    }
    private void loseCheck() {
        for(int row=0;row<RowNumber;row++)
            for(int col=0;col<ColNumber;col++){
                if(map_status[row][col]==2&&map[row][col]!=9){
                    map_status[row][col]=4;
                    update_button(row, col);
                }
                if(map[row][col]==9&&map_status[row][col]==0){
                    map_status[row][col]=1;
                    update_button(row, col);
                }
            }
        face=2;
    }
    private void reset(){
        isLose=false;is_face_pressed = false;is_face_eat=false;
        face=0;unclicked=RowNumber*ColNumber;mine_left=MineNumber;
        update_mine_left();
        reset_time();
        update_face();
        for(int row=0;row<RowNumber;row++)
            for(int col=0;col<ColNumber;col++){
                map[row][col]=0;
                map_status[row][col]=0;
                update_button(row, col);
            }
    }

    private int[] last_press;
    private void button_press(int row,int col){
        if(last_press==null || !(last_press[0]==row&&last_press[1]==col))
            if(map_status[row][col]==0){
                images[row][col].setImage(new Image("b0.png"));
                last_press = new int[]{row,col};
            }
    }
    private void button_release(int row,int col){
        if(last_press!=null && !(last_press[0]==row&&last_press[1]==col))
            update_button(last_press[0], last_press[1]);
    }

    private void button_middle_press(int row,int col){
        if(last_press==null || !(last_press[0]==row&&last_press[1]==col)){
            for(int r=-1;r<2;r++)
                for(int c=-1;c<2;c++)
                    if(!(row+r<0||row+r>=RowNumber||col+c<0 || col + c >= ColNumber))
                        if(map_status[row+r][col+c]==0)
                            images[row+r][col+c].setImage(b_image[0]);
            last_press = new int[]{row,col};
        }
    }
    private void button_middle_release(int row,int col){
        if(last_press!=null){
            int ro=last_press[0], co=last_press[1];
            if(!(ro==row&&co==col))
                for(int r=-1;r<2;r++)
                    for(int c=-1;c<2;c++)
                        if(!(ro+r<0 || ro+r>=RowNumber || co+c<0 || co+c>=ColNumber))
                            update_button(ro+r,co+c);
        }
    }

    private int get_zone(MouseEvent ME){
        int zone=0;//1:mine area 2:face
        int y=(int)ME.getY();int x=(int)ME.getX();
        if((!(16>x||x>=XMax-16||84>y||y>=YMax-16))&&face==0) zone=1;
        if(!(XMax/2-26>x||x>=XMax/2+26||16>y||y>=68)) zone=2;
        return zone;
    }
    private int yTOrow(MouseEvent ME) {
        double row=(ME.getY()-84)/32;
        if(row>=0) return (int)row;
            else return (int)row-1;
    }
    private int xTOcol(MouseEvent ME) {
        double col=(ME.getX()-16)/32;
        if(col>=0) return (int)col;
        else return (int)col-1;
    }

    private void update_button(int row,int col){
        if(map_status[row][col]!=1)
            images[row][col].setImage(s_image[map_status[row][col]]);
        else
            images[row][col].setImage(b_image[map[row][col]]);
    }
    private void update_face(){
        faces.setImage(f_image[face]);
    }
    private void update_mine_left(){
        boolean isPositive = mine_left>=0;
        int show_mine=mine_left;
        if(mine_left>999) show_mine=999;
        if(mine_left<-99) show_mine=-99;
        if(!isPositive) show_mine=0-show_mine;

        for(int i=0;i<3;i++)
            mine_lefts[i].setImage(t_image[show_mine%(int)(Math.pow(10,i+1))/(int)(Math.pow(10,i))]);
        if(!isPositive)
            mine_lefts[2].setImage(t_image[10]);
    }

    public void quit(){
        stage.close();
    }

    public void newGame(){
        reset();
    }

    private int[] custom;
    public void newGameCustom() throws IOException {
        custom=new int[]{RowNumber,ColNumber,MineNumber};

//        Dialog<ButtonType> dialog = new Dialog<>();
//        dialog.setTitle("CUSTOM");
//        dialog.setHeaderText("head");


        Pane root = FXMLLoader.load(getClass().getResource("Custom.fxml"));

        ButtonType Cancel = new ButtonType("Cancel", ButtonBar.ButtonData.APPLY);
        ButtonType OK = new ButtonType("OK", ButtonBar.ButtonData.APPLY);
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION,"CUSTOM",Cancel,OK);//simpleAlarm("CUSTOM","h","d",null);
        alert.getDialogPane().setContent(root);
        alert.setTitle("CUSTOM");
        alert.setHeaderText("head");
        alert.setGraphic(null);

        //alert.getDialogPane().getButtonTypes().addAll(OK, Cancel);
        TextField[] textField=new TextField[3];
        for(int i=0;i<3;i++){
            textField[i] = new TextField();
            int current = i;
            textField[i].setOnKeyReleased(ime -> custom[current] = customKeyReleased(textField[current],ime));
            textField[i].setText(custom[i]+"");
            textField[i].setPrefSize(40, 26);
            textField[i].setLayoutX(170); textField[i].setLayoutY(30 * i + 20);
            root.getChildren().add(textField[i]);
        }

        int focusIs = 0;
        while (true) {
            Optional<ButtonType> result=alert.showAndWait();
            textField[focusIs].requestFocus();
            p(textField[focusIs].isFocused() + "" + focusIs);
            if(result.isPresent()&&result.get().equals(OK)){
                if(custom[0]<1||custom[0]>99){
                    showSimpleAlarm("Invalid Row Number", "", "Number Of Rows Should Be 1 - 99;\nNumber Of 8 - 24 Is Suggested", "OK",null);
                    focusIs=0;}
                else if(custom[1]<7||custom[1]>99){
                        showSimpleAlarm("Invalid Column Number", "", "Number Of Column Should Be 7 - 99;\nNumber Of 8 - 45 Is Suggested", "OK",null);
                        focusIs=1;}
                    else if(custom[2]<1||custom[2]>custom[0]*custom[1]/3){
                            showSimpleAlarm("Invalid Mine Number", "", "Number Of Mine Should Be 1 - 1/3 of Total spaces;\nNumber Of  1/12 - 1/4 Of Total Spaces Is Suggested", "OK",null);
                            focusIs=2;}
                        else{
                            RowNumber=custom[0];
                            ColNumber=custom[1];
                            MineNumber=custom[2];
                            initializePane();
                            return;
                        }
            }
            if(result.isPresent()&&result.get().equals(Cancel)){
                break;
            }

        }

    }
    public int customKeyReleased(TextField input, javafx.scene.input.KeyEvent ime){
        //if(ime.getCode()==KeyCode.ENTER)
        String text=input.getText();
        int num=Integer.parseInt("0"+text.replaceAll("\\D", ""));
        if(!(num+"").equals(text)) input.setText(num + "");
        return num;

    }

    private Alert simpleAlarm(String title,String headerText, String information, String buttonName){
        ButtonType buttonType=new ButtonType(buttonName, ButtonBar.ButtonData.APPLY);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,information,buttonType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setGraphic(null);
        return alert;
    }
    private Alert simpleAlarm(String title, javafx.scene.Node information, String buttonName){
        DialogPane root=new DialogPane();
        root.setContent(information);

        Alert alert = simpleAlarm(title, "", "", buttonName);
        root.getButtonTypes().addAll(alert.getButtonTypes());
        alert.setDialogPane(root);
        return alert;
    }
    private Alert showSimpleAlarm(String title,String headerText, String information, String buttonName, simpleVoid v){
        Alert alert = simpleAlarm(title, headerText, information, buttonName);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()&&result.get().equals(alert.getButtonTypes().get(0)))  if(v!=null) v.v();
        return alert;
    }
    private Alert showSimpleAlarm(String title, javafx.scene.Node information, String buttonName, simpleVoid v){
        Alert alert = simpleAlarm(title, information, buttonName);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()&&result.get().equals(alert.getButtonTypes().get(0))) if(v!=null) v.v();
        return alert;
    }
    private Alert showSimpleAlarm(String title,String headerText, javafx.scene.Node information, String[] buttonNames, simpleVoid[] vs){
        if(buttonNames.length!=vs.length) {p("Error: # Of ButtonType And Void Are Not Same"); return null;}
        DialogPane root=new DialogPane();
        root.setContent(information);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setDialogPane(root);

        ButtonType[] buttonTypes = new ButtonType[buttonNames.length];
        for (int i=0;i<buttonNames.length;i++){
            buttonTypes[i] = new ButtonType(buttonNames[i]);
            alert.getButtonTypes().add(buttonTypes[i]);
        }

        Optional<ButtonType> result = alert.showAndWait();
        for (int i=0;i<buttonNames.length;i++){
            if(result.isPresent()&&result.get().equals(buttonTypes[i]))  if(vs[i]!=null) vs[i].v();}
        return alert;
    }

    public void newGameBeginner(){
        RowNumber = 9;
        ColNumber = 9;
        MineNumber = 10;
        initializePane();
    }
    public void newGameIntermediate(){
        RowNumber = 16;
        ColNumber = 16;
        MineNumber = 40;
        initializePane();
    }
    public void newGameExpert(){
        RowNumber = 16;
        ColNumber = 30;
        MineNumber = 99;
        initializePane();
    }

    public void board(){

    }
    /**
    List<String[]> board=new ArrayList<>();//name of player, level, time used

    private String role1 = "";
    private String role2 = "";
    private String role3 = "";
    private String role4 = "";
    private ArrayList<String> player;


    public boolean readXML(String xml) {
        player = new ArrayList<String>();
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            dom = db.parse(xml);

            Element doc = dom.getDocumentElement();

            role1 = getTextValue(role1, doc, "role1");
            if (role1 != null) {
                if (!role1.isEmpty())
                    player.add(role1);
            }
            role2 = getTextValue(role2, doc, "role2");
            if (role2 != null) {
                if (!role2.isEmpty())
                    player.add(role2);
            }
            role3 = getTextValue(role3, doc, "role3");
            if (role3 != null) {
                if (!role3.isEmpty())
                    player.add(role3);
            }
            role4 = getTextValue(role4, doc, "role4");
            if ( role4 != null) {
                if (!role4.isEmpty())
                    player.add(role4);
            }
            return true;

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return false;
    }
    public void saveToXML(String xml) {
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement("roles");

            // create data elements and place them under root
            e = dom.createElement("role1");
            e.appendChild(dom.createTextNode(role1));
            rootEle.appendChild(e);

            e = dom.createElement("role2");
            e.appendChild(dom.createTextNode(role2));
            rootEle.appendChild(e);

            e = dom.createElement("role3");
            e.appendChild(dom.createTextNode(role3));
            rootEle.appendChild(e);

            e = dom.createElement("role4");
            e.appendChild(dom.createTextNode(role4));
            rootEle.appendChild(e);

            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(xml)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }
    private String getTextValue(String def, Element doc, String tag) {
        String value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }
    */
    public void help() throws IOException {
        showSimpleAlarm("HELP", FXMLLoader.load(getClass().getResource("Help.fxml")), "CLOSE", null);
    }
    public void about() throws IOException {
        simpleAlarm("About", FXMLLoader.load(getClass().getResource("About.fxml")), "CLOSE").show();
    }


    int time;
    private void update_time(){
        for(int i=0;i<3;i++)
            times[i].setImage(t_image[time%(int)(Math.pow(10,i+1))/ (int)(Math.pow(10,i))]);
    }
    private void reset_time(){
        timeline.stop();
        time=0;
        timeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {time++; update_time();}));
        timeline.setCycleCount(999);
        update_time();
    }


    private void p(Object o) {System.out.println(o);}
    interface simpleVoid{
        public void v();
    }
    /** </by Carl> */
}

/**
 * git remote add origin https://github.com/caca2331/DS_3.git
 * git push -u origin master
 */