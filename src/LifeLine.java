
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class LifeLine {
    private final String name;
    private final String klasse;

    private double x,y;
    private int depth =0;
    private final int upperBorder=10;
    private final int lowerBorder = 10;
    private boolean isMulti;
    private boolean destroy;
    private double yEnd;

    public LifeLine(String name, String klasse, double x, double y) {
        this.name = name;
        this.klasse = klasse;
        this.y = y;
        isMulti= klasse!=null && klasse.toLowerCase().replace(" ","").endsWith("[]");
        isMulti = isMulti()|| (name!=null && name.toLowerCase().replace(" ","").endsWith("[]"));

        this.x = x-(isMulti?20:0);
    }

    public void setX(double x){this.x=x;}

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return name.toUpperCase().equals(obj+"");
    }

    public int getDepth() {
        return depth;
    }

    public void incDepth(){
        depth++;}
    public void decDepth(){
        depth--;}

    @Override
    public String toString() {
        return name+"("+x+"/"+y+") dep:"+ depth;
    }

    public double getX() { return x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public String getName() {
        return name;
    }

    public void draw(Canvas can, double maxY) {
        can.getGraphicsContext2D().setStroke(Color.BLACK);
        can.getGraphicsContext2D().setFill(Color.WHITE);
        can.getGraphicsContext2D().setLineDashes();


        double w = Helper.getTextWidth(getDrawString());
        double h = Helper.getTextHeight(getDrawString());

        if (isMulti) {
            can.getGraphicsContext2D().strokeRect(x-w/2+V.get().getCallWidth() - 10+20, y, w + 20, h + lowerBorder + upperBorder);

            can.getGraphicsContext2D().fillRect(  x-w/2+V.get().getCallWidth() - 10+10, y+10, w + 20, h + lowerBorder + upperBorder);
            can.getGraphicsContext2D().strokeRect(x-w/2+V.get().getCallWidth() - 10+10, y+10, w + 20, h + lowerBorder + upperBorder);

            can.getGraphicsContext2D().fillRect(  x-w/2+V.get().getCallWidth() - 10,    y+20, w+20, h+lowerBorder+upperBorder);
            can.getGraphicsContext2D().strokeRect(x-w/2+V.get().getCallWidth() - 10,    y+20, w+20, h+lowerBorder+upperBorder);
        }else{
            can.getGraphicsContext2D().fillRect(  x-w/2+V.get().getCallWidth() - 10,    y, w+20, h+lowerBorder+upperBorder);
            can.getGraphicsContext2D().strokeRect(x-w/2+V.get().getCallWidth() - 10,    y, w+20, h+lowerBorder+upperBorder);
        }

        can.getGraphicsContext2D().setFill(Color.BLACK);
        //Text t = new Text(getDrawString());
        //t.setFont(Helper.font);

        //can.getGraphicsContext2D().fillText(getDrawString(), x-w/2+Helper.callWidth, y+upperBorder-t.getLayoutBounds().getMinY()+(isMulti?20:0));
        Helper.strokeText(can.getGraphicsContext2D(), getDrawString(), x-w/2+V.get().getCallWidth(), y+upperBorder+Helper.getTextHeight(getDrawString())/*-t.getLayoutBounds().getMinY()*/+(isMulti?20:0), true);

       /* can.getGraphicsContext2D().strokeLine(x-w/2+Helper.callWidth,
                Helper.font.getSize()/6+y+upperBorder+t.getBaselineOffset()+(isMulti?20:0),
                x+w/2+Helper.callWidth,
                Helper.font.getSize()/6+y+upperBorder+t.getBaselineOffset()+(isMulti?20:0)
        );*/

        // Lifeline
        can.getGraphicsContext2D().setLineDashes(5);
        if (destroy)
            can.getGraphicsContext2D().strokeLine(x+V.get().getCallWidth() ,y+h+upperBorder+lowerBorder+(isMulti?20:0), x+V.get().getCallWidth(), yEnd);
        else
            can.getGraphicsContext2D().strokeLine(x+V.get().getCallWidth() ,y+h+upperBorder+lowerBorder+(isMulti?20:0), x+V.get().getCallWidth(), maxY+lowerBorder+upperBorder);

    }

    public double getWidth(){
        return Helper.getTextWidth(getDrawString())+20+(isMulti?40:0);

    }

    public double getHeight() {
        return Helper.getTextHeight(getDrawString())+lowerBorder+upperBorder+(isMulti?40:0);
    }

    public boolean isMulti(){return isMulti;}

    private String getDrawString(){
        String s = name.startsWith("_")?"":name;
        if (klasse!= null && klasse.length()>0) s+= ":"+klasse;
        return s;
    }

    public void destroy(double y1) {
        destroy=true;
        yEnd=y1;
    }
}
