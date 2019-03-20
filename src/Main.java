

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private Stage stage;
    private final VBox vbSliders = new VBox();
    private final ArrayList<Slider> sliders = new ArrayList<>();
    private final Button bAutoSize = new Button("auto");

    private final Canvas can = new Canvas(2*1024,1<<12);
    private final CheckMenuItem cmi = new CheckMenuItem("auto_size after parse");

    private File file;
    private boolean loading = false;

    private double scale = 1;

    private final Parser parser = new Parser();

    private final FileChooser fc = new FileChooser();
    private final FileChooser fcProject = new FileChooser();

    private static final Font fa = Font.loadFont(Main.class.getResourceAsStream("Font Awesome 5 Free-Solid-900.otf"), 16);

    private final String startString = "objects: ";
    private final String test = "objects: a,b,c \n" +
            "a.tu()\n" +
            "\ta.bl()\n" +
            "\t\tcomment 0 hallo 0\n" +
            "\tr\n" +
            "box loop asfd\n" +
            "\tcomment 1 bla|bla|asfasf 1\n" +
            "\tcomment 0 bla|bla|asfasf 2\n" +
            "\tb.tun()\n" +
            "end\n" +
            "\t\tcomment 0 bla|bla|asfasf 3\n" +
            "\t\tc.hall\n" +
            "\t\t\tcomment 1 duesel\n" +
            "\t\tr\n" +
            "\tr\n" +
            "\t\n" +
            "r";

    private final TextArea taCode = new TextArea(
            startString
    );




    public void start(Stage primaryStage)  {

        V.setChangeListener((o, old, newValue)-> settingsChanged());
             
        fc.setInitialDirectory(new File("."));
        stage = primaryStage;
        stage.setTitle("Squenzdiagramm Aid by R.Holzer");


        try {
            stage.getIcons().setAll(SwingFXUtils.toFXImage(ImageIO.read(Main.class.getResource("icon.png")),null));
            stage.getIcons().setAll(SwingFXUtils.toFXImage(ImageIO.read(Main.class.getResource("icon16.png")),null));
        } catch (Exception e) {
            e.printStackTrace();
        }


        can.getGraphicsContext2D().setFont(Helper.font);
        can.getGraphicsContext2D().setLineWidth(2);

        initAction();

        primaryStage.setScene(buildGui());

        primaryStage.setWidth(1024);
        primaryStage.setHeight(786);
        try {
             primaryStage.setMaximized(!true);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (getParameters().getRaw().size()>0){
            load(getParameters().getRaw().get(0));
        }
       // load("test Comment Box 2.hzSeq");
        primaryStage.show();

    }

    private void settingsChanged() {
        if (!loading)
            parse();
    }

    private void initAction(){
        fc.setInitialDirectory(new File("."));
        fcProject.setInitialDirectory(new File("."));
        can.setOnScroll(
                m-> {
                    if (m.isControlDown()) {
                        if (m.getDeltaY() > 0) scale += 0.125;
                        else scale -= 0.125;

                        if (scale <= 0) scale = 0.125;
                        draw();
                    }
                }
        );

        can.setOnDragOver((e)->{
            //e.getDragboard().getFiles().size()==1;
            if (e.getDragboard().hasFiles() && e.getDragboard().getFiles().get(0).getName().toLowerCase().endsWith(".hzseq")){
                e.acceptTransferModes(TransferMode.COPY);
                e.consume();
            }
        });

        can.setOnDragDropped((e)->{
            List<File> list = e.getDragboard().getFiles();
            if (list.size()==1) load(list.get(0));
        });

        taCode.addEventHandler(KeyEvent.KEY_RELEASED,new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                //handle shortcuts if defined
                try {
                    Runnable r = taCode.getScene().getAccelerators().get(new KeyCodeCombination(event.getCode()));
                    if (r != null) {
                        r.run();
                    }
                }catch (IllegalArgumentException ignored){
                    // fail silently
                }
            }
        });


        fcProject.getExtensionFilters().clear();
        fcProject.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project", "*.hzSeq"));
        fcProject.getExtensionFilters().add(new FileChooser.ExtensionFilter("any", "*"));
        fcProject.setInitialFileName(".hzSeq");
        fcProject.setSelectedExtensionFilter(fcProject.getExtensionFilters().get(0));
    }

    private Scene buildGui(){

        TitledPane tpSettings = new TitledPane("Settings", V.get());
        tpSettings.setExpanded(false);
        tpSettings.setAnimated(false);
        tpSettings.setGraphic(getIconLabel("\uf013", Color.BLACK, false));

        TitledPane tpObjectPosition = new TitledPane("Lifeline position", vbSliders);
        tpObjectPosition.setGraphic(getIconLabel("\uf337", Color.BLACK, false));

        VBox vBox = new VBox(  tpObjectPosition, tpSettings, taCode);
        VBox.setVgrow(taCode, Priority.ALWAYS);

        SplitPane mainSplitPane = new SplitPane(new ScrollPane(can), vBox);

        Tab tSyntax = new Tab("Sequence-diagram", mainSplitPane);
        tSyntax.setClosable(false);

        Tab help = new Tab("Help", new Help());

        help.setClosable(false);

        TabPane tabs = new TabPane(tSyntax, help);
        tabs.addEventFilter(KeyEvent.KEY_PRESSED, e->{
            if (e.getCode()==KeyCode.ALT_GRAPH) e.consume();
        });
        mainSplitPane.getDividers().get(0).setPosition(.7);

        BorderPane borderPane = new BorderPane(tabs);
        borderPane.setTop(initMenu());
        return new Scene(borderPane);
    }


    private Label getIconLabel(String t, Color c, boolean outline){
        Label l = new Label(t);

        l.setTextFill(c);
        if (outline) {
            l.getStyleClass().add("outline");
            l.getStylesheets().addAll(Main.class.getResource("st.css").toString());
        }
        l.setFont(Main.fa);

        return l;
    }

    private MenuBar initMenu(){
        Color c = Color.DARKGRAY;

        MenuItem miFileNew = new MenuItem("_new");
        miFileNew.setOnAction(e->{file=null; taCode.setText("");});
        miFileNew.setAccelerator(KeyCodeCombination.keyCombination("CTRL+N"));

        MenuItem miFileOpen = new MenuItem("_open", getIconLabel("\uf07c", c, true));
        miFileOpen.setOnAction(e->load());
        miFileOpen.setAccelerator(KeyCodeCombination.keyCombination("CTRL+O"));

        MenuItem miFileSave = new MenuItem("_save", getIconLabel("\uf0c7", c, true));
        miFileSave.setOnAction(e->save(false));
        miFileSave.setAccelerator(KeyCodeCombination.keyCombination("CTRL+S"));

        MenuItem miFileSaveAs = new MenuItem("save _as");
        miFileSaveAs.setOnAction(e->save(true));
        miFileSaveAs.setAccelerator(KeyCodeCombination.keyCombination("CTRL+Shift+S"));


        MenuItem miSavePng = new MenuItem("export _png", getIconLabel("\uf56e", c, true));
        miSavePng.setOnAction(e->saveToPng());
        miSavePng.setAccelerator(KeyCodeCombination.keyCombination("Ctrl+P"));
//-------------------------

        MenuItem miParse = new MenuItem("_parse",  getIconLabel("\uf6ad", c, false));
        miParse.setOnAction(e->parse());
        miParse.setAccelerator(KeyCodeCombination.keyCombination("F5"));


        cmi.setSelected(true);
        cmi.setGraphic( getIconLabel("\uf5e4", c, false));
        cmi.setAccelerator(KeyCodeCombination.keyCombination("F4"));

        MenuItem miAutoSize = new MenuItem("_autosize", getIconLabel("\uf5de", c, false));
        miAutoSize.setOnAction(e->doAutosize());
        miAutoSize.setAccelerator(KeyCodeCombination.keyCombination("F6"));
//-------------------------
        MenuItem miZoomIn = new MenuItem("zoom _in", getIconLabel("\uf00e", c, false));
        miZoomIn.setOnAction(e->{scale+=0.125; draw();});
        miZoomIn.setAccelerator(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));

        MenuItem miZoomOut = new MenuItem("zoom _out", getIconLabel("\uf010", c, false));
        miZoomOut.setOnAction(e->{scale-=0.125; if (scale<0.125) scale=0.125; draw();});
        miZoomOut.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
//-------------------------
        MenuItem miInsertAnd = new MenuItem("insert and");
        miInsertAnd.setOnAction(e->{taCode.insertText(taCode.getCaretPosition(), "\u2227");});
        miInsertAnd.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));

        MenuItem miInsertOr = new MenuItem("insert or");
        miInsertOr.setOnAction(e->{taCode.insertText(taCode.getCaretPosition(), "\u2228");});
        miInsertOr.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));


        Menu mFile = new Menu("_File", null,miFileNew, miFileOpen, miFileSave, miFileSaveAs, miSavePng);
        Menu mParse = new Menu("_Parse", null, miParse, cmi, miAutoSize);
        Menu mEdit = new Menu("_Edit", null, miInsertOr, miInsertAnd);
        Menu mView = new Menu("_View", null, miZoomIn, miZoomOut);
        MenuBar mbar = new MenuBar(mFile, mEdit, mParse, mView);
        return mbar;
    }

    private void doAutosize() {
        parser.autosize(sliders);
    }

     boolean parse(boolean fromSlider){
        boolean success = parser.parse(taCode.getText(), fromSlider, sliders, vbSliders, this);
        draw();
        return success;
    }

    private void parse(){
        boolean success =parse(false);
        if (success && cmi.isSelected()) parser.autosize(sliders);
        draw();
    }

    private void draw(){
        //long zeit = System.currentTimeMillis();

        can.getGraphicsContext2D().clearRect(0,0, can.getWidth(), can.getHeight());
        can.getGraphicsContext2D().setTransform(new Affine());

        can.setHeight(scale*(parser.getMaxY()+40));
        can.setWidth(scale*(parser.getMaxX()+10-parser.getShiftX()+20));

        can.getGraphicsContext2D().save();
        can.getGraphicsContext2D().scale(scale,scale);
        can.getGraphicsContext2D().translate(-parser.getShiftX()+20,0);
        for (LifeLine l:parser.getObjects()) l.draw(can, parser.getMaxY()+10);
        for (Call c:parser.getCallList()) c.draw(can);
        for (Box b:parser.getBoxList()) b.paint(can);
        for (ArrayList<Comment> com : parser.getComments())
            for( Comment c:com) c.draw(can);

        can.getGraphicsContext2D().restore();
        //todo System.out.println("Rendering: "+(System.currentTimeMillis()-zeit)+"ms");
    }


    private void save(boolean saveAs){
        if (this.file==null || saveAs) {
            File file = fcProject.showSaveDialog(this.stage);
            if (file == null) return;
            this.file = file;
            fcProject.setInitialDirectory(file.getParentFile());
        }
        try {
            String s = file.getCanonicalPath();
            if (!s.toLowerCase().endsWith(".hzseq")){
                s+=".hzSeq";
            }
            file = new File(s);
            OutputStreamWriter writer =
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write("settings:"+V.get().getSettings()+"\r\n"+taCode.getText());
            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load(){
        File file = fcProject.showOpenDialog(this.stage);
        if (file==null) return;

        load(file);

    }

    private void load(String file){
        File f = new File(file);
        if (!f.exists()) return;
        else{
            load(f);
        }
    }

    private void load(File file){
        try {
            this.file = file;
            fcProject.setInitialDirectory(file.getParentFile());
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if (lines.size()>0){
                loading=true;
                if (V.get().loadSettings(lines.get(0))) lines.remove(0);
                loading = false;
                StringBuffer sb = new StringBuffer();
                for (String s: lines) {
                    sb.append(s);
                    sb.append(System.lineSeparator());
                }
                taCode.setText(sb.toString());
                parse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToPng(){
        try {
            double oldScale = scale;
            scale =1;
            parse();
            WritableImage wi = can.snapshot(
                    null,
                    new WritableImage((int)can.getWidth()+20, (int)can.getHeight()+20)
            );
            scale = oldScale;
            parse();

            fc.getExtensionFilters().clear();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("any", "*"));
            fc.setInitialFileName(".png");
            fc.setSelectedExtensionFilter(fc.getExtensionFilters().get(0));
            if(file!=null) fc.setInitialDirectory(file.getParentFile());

            File file = fc.showSaveDialog(this.stage);
            if (file==null) return;
            String s = file.getCanonicalPath();
            if (!s.toLowerCase().endsWith(".png")){
                s+=".png";
            }
            file = new File(s);

            fc.setInitialDirectory(file.getParentFile());
            ImageIO.write(SwingFXUtils.fromFXImage(wi, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Application.launch(Main.class);
    }
}
