
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;



public class Call {
    protected final LifeLine from;
    protected final LifeLine to;
    protected final double y0;
    protected boolean construct;
    protected double y1;
    protected Arrow call;
    protected Arrow ret;
    protected int fromDept, toDept;
    protected boolean destroy;

    protected Call(LifeLine from, LifeLine to, double y0){
        this.from = from;
        this.y0 = y0;
        this.to = to;
    }

    public Call(LifeLine from, LifeLine to, double y0, String text, boolean construct){
        this(from, to, y0 );
        this.construct = construct;

        if (from!=null) fromDept = from.getDepth();
        if (to!=null){
            to.incDepth();
            toDept = to.getDepth();
        }


        //noinspection ConstantConditions,ConstantConditions
        call = new Arrow(from, to, y0-(construct?to.getHeight()/2:0), text, construct?Arrow.Con:Arrow.CALL);

        if (text.equalsIgnoreCase("destroy()")){
            destroy = true;
        }
    }



    public void endCall( double y1, String text){
        this.y1 = y1;

        if (from!=null)
            ret = new Arrow( to, from,y1, text, Arrow.RET);
        to.decDepth();
        if (destroy){
            to.destroy(y1);
        }
    }

    public void draw(Canvas can){
        //new Rectangle2D.Double(sx-10+10*objects.get(curObj).getDepth(), sy, 20, y-sy)
        can.getGraphicsContext2D().setLineDashes();
        can.getGraphicsContext2D().setFill(Color.WHITE);
        can.getGraphicsContext2D().setStroke(Color.BLACK);

        can.getGraphicsContext2D().fillRect(to.getX()+toDept*V.get().getCallWidth()/2, y0, V.get().getCallWidth(), y1-y0);
        can.getGraphicsContext2D().strokeRect(to.getX()+toDept*V.get().getCallWidth()/2, y0, V.get().getCallWidth(), y1-y0);

        /*can.getGraphicsContext2D().save();
        can.getGraphicsContext2D().setStroke(Color.RED);
        double dist=2;
        can.getGraphicsContext2D().strokeRect(getXMin()-dist, y0-dist, getXMax()-getXMin()+2*dist, y1-y0+2*dist);
        dist=3;

        can.getGraphicsContext2D().setStroke(Color.ORANGE);
        can.getGraphicsContext2D().strokeRect(getXMin()-dist, y0-dist, getAutoWidth()+2*dist, y1-y0+2*dist);

        can.getGraphicsContext2D().restore();*/

        call.drawArrow(can);
        if (destroy){
            can.getGraphicsContext2D().strokeLine(
                    to.getX()+V.get().getCallWidth()/2+toDept*V.get().getCallWidth()/2-0.75*V.get().getCallWidth(), y1-0.75*V.get().getCallWidth(),
                    to.getX()+V.get().getCallWidth()/2+toDept*V.get().getCallWidth()/2+0.75*V.get().getCallWidth(), y1+0.75*V.get().getCallWidth()
            );
            can.getGraphicsContext2D().strokeLine(
                    to.getX()+V.get().getCallWidth()/2+toDept*V.get().getCallWidth()/2+0.75*V.get().getCallWidth(), y1-0.75*V.get().getCallWidth(),
                    to.getX()+V.get().getCallWidth()/2+toDept*V.get().getCallWidth()/2-0.75*V.get().getCallWidth(), y1+0.75*V.get().getCallWidth()
            );
        }else
            if (ret!=null) ret.drawArrow(can);

    }



    public double getXMin(){
        double erg = to.getX()+V.get().getCallWidth()/2;
        if (from==null) erg =0;
        else erg = Math.min(erg, from.getX()+V.get().getCallWidth()/2);

        if (construct) erg = Math.min(erg, to.getX()-to.getWidth()/2+V.get().getCallWidth());
        return erg;
    }

    public double getXMax(){
        double erg = to.getX()+(toDept+1)*V.get().getCallWidth()/2+V.get().getCallWidth()/2;
        if (from!=null) erg = Math.max(erg, from.getX()+(fromDept+1)*V.get().getCallWidth()/2+V.get().getCallWidth()/2);


        if (construct) erg = Math.max(erg, to.getX()+to.getWidth()/2+V.get().getCallWidth());
        if (to==from){
            erg = Math.max(erg, call.getMaxX());
            if (ret!=null)
                erg = Math.max(erg, ret.getMaxX());
        }
        return erg;
    }

    public double getY1(){return y1;}

    public LifeLine getCaller(){return  from;}
    public LifeLine getReceiver(){return  to;}

    public double getAutoWidth(){
        /*double erg =0;
        if (call!=null) {

            erg = Math.max(
                    erg,
                    call.getTextWidth()
                            + call.getCallerDept() * Helper.V.get().getCallWidth() / 2
                            + (construct ? call.getReceiver().getWidth() / 2 + (call.getReceiver().isMulti() ? -10 : 0) : 0));

            if (call.getCaller() == null) if (call.getActor()!=0) erg+=70;
        }
        if (ret!=null) erg = Math.max(erg, ret.getTextWidth()+call.getReceiverDept()*Helper.V.get().getCallWidth()/2);
        erg += Arrow.arrowHead*2+20;

        return erg;*/
        return Math.max(
                call==null?0:call.getAutoWidth(),
                ret==null?0:ret.getAutoWidth());
    }

    @Override
    public String toString() {
        return (call!=null? call.getText():"")+" / "+ret.getText();
    }

    public boolean isDestructor() {
        return destroy;
    }
}
