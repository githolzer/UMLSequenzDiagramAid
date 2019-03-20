
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Helper {
    public static final boolean DEBUG = false;
    public static final Font font = Font.font("Arial", FontWeight.BOLD, 30);
    //public static double callWidth=20;
    public static int NONE_ACTOR_CLOCK =0;

    public static double getTextHeight(String s){
        Text t = new Text(s.replace('|', '\n'));
        t.setFont(font);

        Bounds bound = t.getLayoutBounds();
        return bound.getHeight();
    }

    public static void strokeText(GraphicsContext g, String s, double x, double y){
        strokeText(g,s,x,y, false);
    }
    public static void strokeText(GraphicsContext g, String s, double x, double y, boolean underline){
        Text t = new Text(s.replace('|', '\n'));
        t.setFont(font);

        if (underline){
            t.setUnderline(true);
            t.setTextAlignment(TextAlignment.CENTER);
        }
        t.setSmooth(false);
        t.setFontSmoothingType(null);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        WritableImage img =t.snapshot(params, null);

        g.drawImage(img, x,y-getTextHeight(s));

        /*for (int i=0; i<lines.length; i++){
            g.fillText(lines[i], x,y-getTextHeight(s)+getTextHeight(lines[0]));
            y+=getTextHeight(lines[i]);
        }*/
    }


    public static Alert getExceptionAlert(String title, String header, String content, Exception ex){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");


        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        return alert;
    }

    public static double getTextWidth(String s){
        if (s==null || s.length()==0) return 0;
        Text t = new Text(s.replace('|', '\n'));
        t.setFont(font);
        Bounds bound = t.getLayoutBounds();
        return bound.getWidth();

    }
}
