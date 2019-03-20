
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Comment {
    private final double y;
    private final String text;
    private final LifeLine obj;
    private double dept;

    private double marginLeft, marginRight;

    public Comment(double y, String text, LifeLine obj) {
        this.y = y;
        this.text = text ;
        this.obj = obj;
        if (obj!=null) dept = obj.getDepth();
        else dept=0;
        if (dept==0) dept=1;

    }

    public void setMarginLeft(double marginLeft) { this.marginLeft = marginLeft; }

    public void setMarginRight(double marginRight) { this.marginRight = marginRight; }



    public double getHeight(){ return Helper.getTextHeight(text)+20; }

    public double getY(){return y;}
    public double getMaxY(){ return y+getHeight()+20; }

    public double getWidth(){
        return Helper.getTextWidth(text)+30
                +marginLeft+marginRight;
    }

    public double getWidthWithoutMargins(){return Helper.getTextWidth(text)+40;}

    public double getMaxX(){
        return getWidth()+getX();}

    public double getX(){
        return obj!=null?
                obj.getX() +V.get().getCallWidth()*(dept+2)/2 +10
                :0;
    }

    public LifeLine getLifeline() {
        return obj;
    }

    public void draw(Canvas c){
        GraphicsContext g = c.getGraphicsContext2D();
        g.setStroke(Color.BLACK);

        double w = Helper.getTextWidth(text);
        double h = Helper.getTextHeight(text);

        double x = getX();

       // g.strokeRect(x, y+10, w+20, h+20);

        g.strokeLine(marginLeft+x     ,y+10, marginLeft+x     , y+h+20+10); // |...
        g.strokeLine(marginLeft+x+w+20,y+20, marginLeft+x+w+20, y+h+20+10); // ...|

        g.strokeLine(marginLeft+x+w+10, y+10, marginLeft+x+w+20, y+20); // ...\
        g.strokeLine(marginLeft+x+w+10, y+10, marginLeft+x+w+10, y+20); //
        g.strokeLine(marginLeft+x+w+10, y+20, marginLeft+x+w+20, y+20); //

        g.strokeLine(marginLeft+x, y+10     , marginLeft+x+w+10, y+10);// ---
        g.strokeLine(marginLeft+x, y+10+h+20, marginLeft+x+w+20, y+10+h+20);// ___


        if (Helper.DEBUG) {
            g.save();
            g.setStroke(Color.RED);
            g.setLineWidth(4);
            g.strokeRect(x, y, getWidth(), h + 10 + 20);
            g.strokeLine(getX() + 10, y + 10, getX(), y + 10);
            g.strokeLine(getMaxX() - 20, y + 10, getMaxX(), y + 10);
            g.setStroke(Color.GREEN);
            g.strokeLine(getX(), y + 20, getX() + marginLeft, y + 20);
            g.strokeLine(getMaxX(), y + 20, getMaxX() - marginRight, y + 20);
            g.restore();
        }

        Helper.strokeText(g, text, marginLeft+x+10, y+20+h);


    }

    @Override
    public String toString() {
        return marginLeft+"/"+marginRight+" text: "+text.replace("\n", " ");
    }
}
