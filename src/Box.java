


import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Box {
    private final int startIndex;
    private int endIndex;
    private int dept;
    private final String type;
    private final String text;

    private double x;
    private double w;
    private final double y0;
    private double y1;
    private double correctX; // Max x correction for inner boxes
    private final ArrayList<Box> innerBoxes = new ArrayList<>();
    private LifeLine lifeLine; // only needed for e.g. breaks with no call inside to determine the start of the box e.g. for comment margins in Parser

    private final ArrayList<String> alt = new ArrayList<>();
    private final ArrayList<Double> altY= new ArrayList<>();

    public Box(int startIndex, double y0, String type, String text) {
        this.startIndex = startIndex;
        this.type = type;
        this.text = text;
        this.y0 = y0;
    }

    public void endBox(int endIndex, double y1){
        this.endIndex = endIndex;
        this.y1 = y1;
    }

    public LifeLine getLifeLine() { return lifeLine; }

    public void setLifeLine(LifeLine lifeLine) { this.lifeLine = lifeLine; }

    public double getY0(){return y0;}
    public double getY1(){return y1;}

    public void move(double dx){ x+=dx;}
    public void incWidth(double dx){ w+=dx;}

    public void setXandWidth(double x, double w, int dept){
        this.dept = dept;
        double shift = Helper.getTextWidth(type)+20+20+10;
        this.x = x-shift;
        this.w = w+shift;
        if (w==0){
            this.w = shift+ Helper.getTextWidth(text)+V.get().getBoxRigthMargin()+V.get().getCallWidth();

        }
        if (text!=null) {
           double w2=   Helper.getTextWidth(text) + shift + V.get().getBoxRigthMargin() + V.get().getCallWidth()*2 + correctX;
           if (this.w<w2) this.w=w2;
        }
        this.w= Math.max(this.w, getMaxAltWidth());
    }

    public int getStartIndex() { return startIndex; }
    public int getEndIndex() { return endIndex; }

    public double getX(){return x;}
    public double getWidth(){return w;}

    public double getHeadHeight(){
        return Math.max(Helper.getTextHeight(type),Helper.getTextHeight(text))+V.get().getBoxHead();
    }

    public void addAlt( double y, String altText){
        alt.add(altText);
        altY.add(y);
    }

    public void addInnerBox(Box b ){innerBoxes.add(b);}

    public void paint(Canvas can){
        can.getGraphicsContext2D().setStroke(Color.BLACK);
        can.getGraphicsContext2D().setFill(Color.BLACK);
        can.getGraphicsContext2D().setLineDashes();
        can.getGraphicsContext2D().setFont(Helper.font);

        double y1OfInnerBox = Helper.getTextHeight(type)+10+y0;
        double widthOfInnerBox = Helper.getTextWidth(type)+20+20; // 20 wg. Ecke + 20 für Rand links und Rechts
        double xShift = x;

        can.getGraphicsContext2D().save();
        can.getGraphicsContext2D().setLineWidth(3);
        can.getGraphicsContext2D().strokeRect(xShift,y0, w, y1-y0+2);
        can.getGraphicsContext2D().restore();

        // Stroke the inner Box for the Text (mit der abgeflachten Ecke)
        can.getGraphicsContext2D().strokeLine(xShift, y1OfInnerBox, xShift+Helper.getTextWidth(type)+20, y1OfInnerBox);
        can.getGraphicsContext2D().strokeLine(
                xShift+Helper.getTextWidth(type)+20, y1OfInnerBox,
                xShift+Helper.getTextWidth(type)+20+20, y1OfInnerBox-20
                );
        can.getGraphicsContext2D().strokeLine(
                xShift+Helper.getTextWidth(type)+20+20, y1OfInnerBox-20,
                xShift+Helper.getTextWidth(type)+20+20, y0
                );

        //can.getGraphicsContext2D().fillText(type, xShift+10, y0+10+Helper.getTextHeight(type));
        Helper.strokeText(can.getGraphicsContext2D(), type, xShift+10, y0+Helper.getTextHeight(type));

        if (text!=null)
            Helper.strokeText(can.getGraphicsContext2D(), text,
                    xShift+widthOfInnerBox+20+
                            V.get().getCallWidth()/2+dept*V.get().getCallWidth()/2
                            +correctX,
                    y0+Helper.getTextHeight(text));


        for (int i=0; i<alt.size(); i++){
            String altText = alt.get(i);
            Double altY = this.altY.get(i);
            can.getGraphicsContext2D().setLineDashes(10);
            can.getGraphicsContext2D().strokeLine(xShift, altY, xShift+w/*+widthOfInnerBox*/, altY);

            Helper.strokeText(can.getGraphicsContext2D(), altText,
                    xShift+widthOfInnerBox+20+
                            V.get().getCallWidth()/2+dept*V.get().getCallWidth()/2
                            +correctX,
                    altY+Helper.getTextHeight(altText));
        }
    }


    private double getMaxAltWidth(){
        double erg =0;
        double widthOfInnerBox = Helper.getTextWidth(type)+20+20;
        for (String anAlt : alt) {
            erg = Math.max(erg, x + widthOfInnerBox + 20 +
                    V.get().getCallWidth() / 2 + dept * V.get().getCallWidth() / 2
                    + correctX + Helper.getTextWidth(anAlt));
        }

        return erg-x+V.get().getBoxRigthMargin();
    }

    public void correctForInnerBoxes() {
        /*System.out.println(type+" "+text+": ");
        for (Box b:innerBoxes)
            System.out.print("("+b.type+" "+b.text+"), ");
        System.out.println();*/
        if (innerBoxes.size()==0) return;

        double minx = -1;
        double maxx = -1;

        for (Box b:innerBoxes){
            if (minx==-1){ minx = b.x; maxx = b.x+b.w;}
            if (minx>b.x){ minx = b.x; }
            if (maxx< b.x+b.w) maxx= b.x+b.w;
        }

        double minDist=V.get().getBoxDist();
        if (minx<=x+minDist) correctX = x-minx+minDist;
        if (minx<=x+minDist){ w+=x+minDist; x=minx-minDist; w-=minx; }
        if (maxx>=x+w-minDist){ w = maxx-x+minDist;}

    }

    public double getXNegative(){return Helper.getTextWidth(type)+20+20+10+correctX;}

    @Override
    public String toString() {
        return "Box ["+type+"] "+text;
    }
}
