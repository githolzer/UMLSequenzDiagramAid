
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;


public class Arrow {
    private final double y;
    private final String text;
    private final double textWidth;
    private final boolean  self;
    private final LifeLine a;
    private final LifeLine b;
    private final int type;
    private final int deptA;
    private final int deptB;
    private int actor =0;

    public static final int CALL = 0;
    public static final int RET  = 1;
    public static final int Con  = 2;


    public Arrow(LifeLine a, LifeLine b, double y, String text, int type) {
        this.text = text;
        this.textWidth = Helper.getTextWidth(text);
        this.y = y;
        this.a = a;
        this.b = b;
        this.type = type;
        deptA = a==null?0:a.getDepth();
        deptB = b.getDepth();

        if (a==null) actor = Helper.NONE_ACTOR_CLOCK;

        self = a==b;
    }


    public double getAutoWidth(){
        // Textbreite + Hälfte der Call-Width (==> parallele Aufrufe des Senders)
        double erg = textWidth+ (deptB+1)*V.get().getCallWidth()/2;
        if (deptA>0) erg+=(deptA-1)*V.get().getCallWidth()/2;

        // + x-Breite der Pfeilspitze
        erg += V.get().getArrowWidth();

        if (type==Con){
            // wenn constructor, dann Hälfte der Lifeline dazuaddieren
            erg+= b.getWidth()/2;
            if (b.isMulti()) erg-=10;
        }

        // wenn clock oder actor  für clock oder actor breite dazu addieren
        if (a==null) {
            erg-=V.get().getCallWidth();
            if (actor == 1) erg += 45;
            if (actor == 2) erg += 70;
        }


        //if (ret!=null) erg = Math.max(erg, ret.getTextWidth()+call.getReceiverDept()*V.get().getCallWidth()/2);

        erg+=20; // Abstand für text (10 rechts und 10 links)


        if (self){
            double sxg = getStartX();
            double callw = sxg+V.get().getCallWidth()+V.get().getArrowWidth()+20+10+Helper.getTextWidth(text);

            if (type==CALL) //noinspection ConstantConditions
                return callw-a.getX();
                        //getStartX()+V.get().getCallWidth()+V.get().getArrowWidth()-20+40+10+textWidth -a.getX();
            if (type==RET) //noinspection ConstantConditions
                return sxg+V.get().getCallWidth()+V.get().getArrowWidth() +20+10+Helper.getTextWidth(text)
                                    -a.getX();
        }
        return erg;
    }

    private double getStartX(){
        double sxg;
        sxg = a==null?0:a.getX()+(deptA+1)*V.get().getCallWidth()/2; // selbe x+ paralleleCalls
        double dx = -V.get().getCallWidth()/2; // deltaX je nach Aufrufrichtung
        if (!self && (a==null || b.getX()>a.getX())) dx = -dx;
        sxg += dx;
        return  sxg;
    }
    private double getEndX(){
        double exg;
        exg = b.getX()+(deptB+1)*V.get().getCallWidth()/2; // x+ paralleleCalls

        double dx =  -V.get().getCallWidth()/2; // deltaX je nach Aufrufrichtung
        if (!self&&(a==null || b.getX()>a.getX())) dx = -dx;

        double dxC = b.getWidth() / 2+(b.isMulti()?-10:0);
        if (a!=null && b.getX()<a.getX()) dxC = -dxC;
        exg -= dx;
        if (type ==Con) exg -= dxC-dx;
        return exg;
    }

    public void drawArrow(Canvas can){
        GraphicsContext g = can.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.setStroke(Color.BLACK);
        double sxg = getStartX();
        double exg = getEndX();


        if (type==RET) g.setLineDashes(10);
        else g.setLineDashes();

        double shift = V.get().getArrowWidth();
        if (!self && exg > sxg) shift = -V.get().getArrowWidth();

        double yVolatile = this.y;
        if (self) {
            // region Self
            double h = Math.max(V.get().getSelfArrowHeight(),V.get().getArrowHeight()+5);//20;
            if (type != RET) {
                g.strokeLine(sxg+V.get().getCallWidth()/2, yVolatile-h , sxg+V.get().getCallWidth()+V.get().getArrowWidth(), yVolatile -h);
                g.strokeArc(
                        sxg+V.get().getCallWidth()+V.get().getArrowWidth()-20,
                        yVolatile - h ,
                        40, h, 90, -180, ArcType.OPEN);
                g.strokeLine( sxg+V.get().getCallWidth()+V.get().getArrowWidth(), yVolatile,  exg, yVolatile );

               // paintText(can, sxg+V.get().getCallWidth(), -h+yVolatile - getTextBounds().getMaxY(), false);
                Helper.strokeText(g, text, sxg+V.get().getCallWidth()+V.get().getArrowWidth()+20+10, yVolatile );

               /* g.save();
                g.setStroke(Color.BLUE);
                g.strokeRect(sxg+V.get().getCallWidth()+V.get().getArrowWidth()-20+40+10, yVolatile-10-h/2+h-40, textWidth, Helper.getTextHeight(text));
                g.restore();*/

            }
            if (type == RET){
                g.setLineDashes(5);

                g.strokeLine(sxg+V.get().getCallWidth(), yVolatile , sxg+V.get().getCallWidth()+V.get().getArrowWidth(), yVolatile );
                g.strokeArc(
                        sxg+V.get().getCallWidth()+V.get().getArrowWidth()-20,
                        yVolatile ,
                        40, h, 90, -180, ArcType.OPEN);
                g.strokeLine(
                        sxg+V.get().getCallWidth()+V.get().getArrowWidth(), yVolatile + h,
                        sxg+V.get().getCallWidth()/2, yVolatile + h
                );

                //paintText(can, sxg+V.get().getCallWidth()*1.5+20, +h+yVolatile, false );
                Helper.strokeText(g, text, sxg+V.get().getCallWidth()+V.get().getArrowWidth() +20+10, +h+yVolatile, false );
                exg-=V.get().getCallWidth()/2;

                yVolatile+=h;

               /* g.save();
                g.setStroke(Color.BLUE);
                g.strokeRect(sxg+V.get().getCallWidth()+V.get().getArrowWidth() +20+10, h+yVolatile-40, Helper.getTextWidth(text), Helper.getTextHeight(text));
                g.restore();*/
            }
            //endregion 
        }else{
            //region not self
            if (type == Con) g.setLineDashes(5,5);
            else g.setLineDashes();
            g.strokeLine(sxg, yVolatile, exg /*+ shift / 2*/, yVolatile);

            //paintText(can, sxg+(actor!=0?70:0), exg, true);
            double x0 = sxg;
            if (actor==1) x0+=45;
            if (actor==2) x0+=70;
            double x1 = exg;
            if (sxg>exg) x1+=V.get().getArrowWidth();
            else x1-=V.get().getArrowWidth();

            double textX = Math.min(x0,x1) + (Math.abs(x1 - x0) - textWidth) / 2;



            /*g.save();
            g.setStroke(Color.RED);
            g.strokeLine(sxg, y-10, sxg, y+10);
            g.strokeLine(exg, y-10, exg, y+10);
            g.setStroke(Color.GREEN);
            g.strokeRect(Math.min(x0,x1), y-40, Math.abs(x1-x0), 40);
            g.strokeLine(textX, y-40, textX, y);
            g.restore();*/

            Helper.strokeText(g, text, textX, y -5);
            //endregion
        }

        // Zeichne Pfeilspitzen
        if (type != Con && RET!=type && a!=null)
            g.fillPolygon(new double[]{
                    exg + shift, 
                    exg + shift, exg}, 
                    new double[]{
                            yVolatile - V.get().getArrowHeight(), yVolatile + V.get().getArrowHeight(), yVolatile}, 3);
        else{
            g.setLineDashes();
            g.strokeLine(exg+(exg>sxg&&!self?-2:+2), yVolatile, exg+shift, yVolatile-V.get().getArrowHeight());
            g.strokeLine(exg+(exg>sxg&&!self?-2:+2), yVolatile, exg+shift, yVolatile+V.get().getArrowHeight());
        }

        if (a==null){
            if (actor==1) {
                double r = 40;
                double ddx = 20;
                double ddy = -70;
                g.setFill(Color.WHITE);
                g.fillRect(sxg - 3, y + ddy, 45, 120);

                g.strokeOval(sxg + ddx / 2, y + ddx + ddy, r - ddx, r - ddx);     // Kopf
                g.strokeLine(sxg, y + r + 10 + ddy, sxg + 40, y + r + 10 + ddy); // Arme
                g.strokeLine(sxg + r / 2, y + r + ddy, sxg + r / 2, y + r + 40 + ddy); // Körper
                g.strokeLine(sxg + r / 2, y + r + 40 + ddy, sxg + r, y + r + 80 + ddy); // Bein 1
                g.strokeLine(sxg + r / 2, y + r + 40 + ddy, sxg, y + r + 80 + ddy); // Bein 2
            }
            if (actor==2) {
                double r2 = 30;
                g.setFill(Color.WHITE);
                g.fillRect(sxg-2, y-r2-2, 2*r2+10, 2*r2+10);
                g.strokeOval(sxg, y-r2, 2*r2, 2*r2);
                g.strokeLine(sxg+r2   , y-25, sxg+r2, y);
                g.strokeLine(sxg+r2+20, y   , sxg+r2, y);
            }
        }
    }

    public double getHeightAboveY(){
        if (self){
            return Math.max(Helper.getTextHeight(text)+5, V.get().getArrowHeight());
        }else{
            if (type==RET)
                return
                        Math.max(20,V.get().getArrowHeight()+5)+Helper.getTextHeight(text);

            else
                return Math.max(
                        Math.max(20,V.get().getArrowHeight()+5),
                        Helper.getTextHeight(text)
                );
        }
    }

    public double getMaxX(){
        double sxg, exg;
        exg = b.getX()+(deptB+1)*V.get().getCallWidth()/2;
        sxg = a==null?0:a.getX()+(deptA+1)*V.get().getCallWidth()/2;
        double dx = !self && exg > sxg ? V.get().getCallWidth()/2 : -V.get().getCallWidth()/2;
        double dxC = (exg > sxg) ? b.getWidth() / 2 : -b.getWidth() / 2;
        exg -= dx;
        sxg += dx;
        if (type ==Con) exg -= dxC-dx;

        if (!self) return Math.max(sxg, exg);
        else{
            if (type == RET) return  sxg+V.get().getCallWidth()+V.get().getArrowWidth() +20+10+ Helper.getTextWidth(text);
            else return (sxg+V.get().getCallWidth()+V.get().getArrowWidth()+20+10+Helper.getTextWidth(text));

        }

    }


    public String getText() {
        return text;
    }


}
