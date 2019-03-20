
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;


public class V extends GridPane {
    private static V v;

    private final Slider arrowWidth  = new Slider(3,50, 10);
    private final Slider arrowHeigth = new Slider(3,50, 5);
    private final Slider callWidth   = new Slider(11,80, 20);
    private final Slider yGap        = new Slider(10,500, 20);

    private final Slider boxXdist = new Slider(10,80, 10);
    private final Slider boxDistBefore= new Slider(5,80, 10);
    private final Slider boxBottomMargin= new Slider(5,80, 20);
    private final Slider boxRightMargin= new Slider(0,80, 20);
    private final Slider boxDistAfter = new Slider(5,80, 30);
    private final Slider boxDistHead  = new Slider(-10,40, 10);

    private final Slider elseBefore      = new Slider(0,80, 20);
    private final Slider elseAfter       = new Slider(-40,80, 0);
    private final Slider elseAfterEmpty  = new Slider(-40,80, 10);

    private final Slider deltaReturn = new Slider(0,80, 10);
    private final Slider deltaEmptyReturn = new Slider(0,80, 10);
    private final Slider deltaCall  = new Slider(0,80, 10);
    private final Slider beforeSelf = new Slider(-20,80, 0);
    private final Slider beforeSelfReturn = new Slider(-20,80, 0);
    private final Slider selfArrowHeight = new Slider(0,80, 20);

    private final Slider[] sliders ={
            arrowWidth,
            arrowHeigth,
            callWidth,
            yGap,
            boxXdist,
            boxDistBefore,
            boxBottomMargin,
            boxRightMargin,
            boxDistAfter,
            boxDistHead,
            elseBefore,
            elseAfter,
            elseAfterEmpty,
            deltaReturn,
            deltaEmptyReturn,
            deltaCall,
            beforeSelf,
            beforeSelfReturn,
            selfArrowHeight,
    };


    private V(){
        int i=0;
        add(new Label("Arrow"), 0, i++);
        addSlider(i++, "Arrow width: ", arrowWidth);
        addSlider(i++, "Arrow height: ", arrowHeigth);
        addSlider(i++, "Self arrow height: ", selfArrowHeight);

        add( new Separator(), 0, i++, 3, 1);
        add(new Label("Y-Gap"), 0, i++);
        addSlider(i++, "Y-Gap: ", yGap);

        add( new Separator(), 0, i++, 3, 1);
        add(new Label("Box"), 0, i++);
        addSlider(i++, "X-distance: ", boxXdist);
        addSlider(i++, "Before: ", boxDistBefore);
        addSlider(i++, "Bottom margin: ", boxBottomMargin);
        addSlider(i++, "Rigth margin: ", boxRightMargin);
        addSlider(i++, "After: ", boxDistAfter);
        addSlider(i++, "After head: ", boxDistHead);

        add( new Separator(), 0, i++, 3, 1);
        add(new Label("Box-else"), 0, i++);
        addSlider(i++, "Before: ", elseBefore);
        addSlider(i++, "After: ", elseAfter);
        addSlider(i++, "After empty: ", elseAfterEmpty);

        add( new Separator(), 0, i++, 3, 1);
        add(new Label("Return:"), 0, i++);
        addSlider(i++, "Space before: ", deltaReturn);
        addSlider(i++, "Space before empty: ", deltaEmptyReturn);
        addSlider(i++, "Space before self return: ", beforeSelfReturn);

        add( new Separator(), 0, i++, 3, 1);
        add(new Label("Call:"), 0, i++);
        addSlider(i++, "Space before: ", deltaCall);
        addSlider(i++, "width: ", callWidth);
        addSlider(i++, "Space before self call: ", beforeSelf);

        GridPane.setHgrow(yGap, Priority.ALWAYS);
    }

    private void addSlider(int row, String text, Slider s ){
        add(new Label(text), 0, row);
        add(s, 2, row);
        Label l = initSlider(s);
        add(l, 1, row);
        GridPane.setHalignment(l, HPos.RIGHT);
    }

    public static void setChangeListener(ChangeListener<? super Number> changeListener) {
        V v = get();
        for (Slider s: v.sliders)
            s.valueProperty().addListener(changeListener);
    }

    public double getArrowWidth(){return arrowWidth.getValue();}
    public double getArrowHeight(){return arrowHeigth.getValue();}
    public double getSelfArrowHeight(){return selfArrowHeight.getValue();}

    public double getCallWidth(){return callWidth.getValue();}
    public double getYgap() { return yGap.getValue(); }

    public double getBoxDist() { return boxXdist.getValue(); }
    public double getBoxBeforeDist() { return boxDistBefore.getValue(); }
    public double getBoxBottomMargin(){return boxBottomMargin.getValue();}
    public double getBoxRigthMargin(){return boxRightMargin.getValue();}
    public double getBoxAfterDist() { return boxDistAfter.getValue(); }
    public double getBoxHead() { return boxDistHead.getValue(); }

    public double getElseBefore(){return  elseBefore.getValue(); }
    public double getElseAfter(){return  elseAfter.getValue(); }
    public double getElseAfterEmpty(){return  elseAfterEmpty.getValue(); }

    public double getReturnBefore(){return deltaReturn.getValue();}
    public double getReturnBeforeEmpty(){return deltaEmptyReturn.getValue();}

    public double getCallBefore(){return deltaCall.getValue();}
    public double getBeforeSelfCall() { return beforeSelf.getValue(); }
    public double getBeforeSelfReturn() { return beforeSelfReturn.getValue(); }






    public static V get(){
        if (v==null) v=new V();
        return v;
    }

   private Label initSlider(Slider s){
        s.setSnapToTicks(true);
        s.setMajorTickUnit(1);
        s.setMinorTickCount(0);
        s.setMaxWidth(s.getMax()*5);

        Label label = new Label();
        label.setTextAlignment(TextAlignment.RIGHT);
        label.setPadding(new Insets(0,10, 0,0));
        label.textProperty().bind(Bindings.createStringBinding(()-> ""+Math.round(s.getValue()), s.valueProperty()));
        return label;
   }

    public String getSettings() {
        StringBuffer sb = new StringBuffer();
        for (Slider s: sliders)
            sb.append(s.getValue()+",");
        return sb.toString();
    }

    public boolean loadSettings(String s){
        if (!s.startsWith("settings:")) return false;
        s = s.substring(s.indexOf(':')+1);
        String[] valS = s.split(",");
        double[] valD = new double[valS.length];
        for (int i=0; i< valS.length; i++)
            try{
                valD[i] = Double.parseDouble(valS[i]);
            }catch (NumberFormatException | NullPointerException e) {
                return false;
            }
        for (int i=0; i<valD.length; i++)
            sliders[i].setValue(valD[i]);
        return true;
    }


}
