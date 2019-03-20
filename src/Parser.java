
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {


    private ArrayList<LifeLine> objects = new ArrayList<>();
    private final ArrayList<Call> callList = new ArrayList<>();
    private final ArrayList<Box> boxList = new ArrayList<>();
    private ArrayList<Comment>[] comments;

    public boolean parse(String text, boolean fromSlider, ArrayList<Slider> sliders, VBox vbSliders, Main main) {
        long zeit = System.currentTimeMillis();
        callList.clear();
        boxList.clear();


        String[] lines = text.split("\n");
        if (lines.length == 0) {
            new Alert(Alert.AlertType.WARNING, "No input text!", ButtonType.OK).showAndWait();
            return false;
        }
        if (!lines[0].toLowerCase().trim().startsWith("objects:")) {
            new Alert(Alert.AlertType.WARNING, "First line must start with objects!", ButtonType.OK).showAndWait();
            return false;
        }

        boolean error = false;

        if (!fromSlider) initObjects(text, sliders, vbSliders, main);
        comments = new ArrayList[objects.size() + 1];
        for (int i = 0; i < comments.length; i++) comments[i] = new ArrayList<>();


        //150
        double y = 0;
        for (LifeLine l : objects)
            if (l.getHeight() + 30 > y) y = l.getHeight() + 30;


        // ------------------- BEGIN PARSING -----------------------------
        Stack<Integer> stackObj = new Stack<>();

        Stack<Call> callStack = new Stack<>();
        int curObj = -1;

        ArrayList<Box> boxesInWork = new ArrayList<>();

        for (int lineNr = 0; lineNr < lines.length; lineNr++) {

            String line = lines[lineNr].trim();
            if (line.startsWith("//")) continue;
            if (line.length() == 0) continue;
            if (line.toLowerCase().startsWith("objects")) continue;
            String oldLine = line;
            try {
                if (line.equalsIgnoreCase("clock")) {
                    Helper.NONE_ACTOR_CLOCK = 2;
                } else if (line.equalsIgnoreCase("actor")) {
                    Helper.NONE_ACTOR_CLOCK = 1;
                } else if (line.equalsIgnoreCase("none")) {
                    Helper.NONE_ACTOR_CLOCK = 0;
                } else if (line.equalsIgnoreCase("ygap")) {
                    y += V.get().getYgap();
                } else if (line.toLowerCase().startsWith("comment ")) {
                    // region comment
                    line = line.substring("comment ".length());
                    int index = 0;
                    if (line.indexOf(' ') > 0) {
                        String num = line.substring(0, line.indexOf(' '));
                        try {
                            index = Integer.parseInt(num);
                            line = line.substring(line.indexOf(' ') + 1);

                        } catch (Exception ignore) {
                        }
                    }
                    if (index>comments.length){
                        new Alert(Alert.AlertType.WARNING, "Comment column "+index+"is not valid. Will use 0. ", ButtonType.OK).showAndWait();
                        error=true;
                        index=-1;
                    }
                    double commentY = y;
                    if (comments[index].size() > 0) {
                        Comment c = comments[index].get(comments[index].size() - 1);
                        double yCmax = c.getMaxY();
                        if (yCmax > commentY) commentY = yCmax + 10;
                    }
                    Comment comment = new Comment(commentY, line, index==0?null:objects.get(index-1));
                    comments[index].add(comment);
                    // endregion
                } else if (line.toLowerCase().startsWith("return") || line.toLowerCase().startsWith("r ") || line.equalsIgnoreCase("r")) {
                    //region return
                    if (callStack.isEmpty()) {
                        new Alert(Alert.AlertType.WARNING, "Too many returns (line " + lineNr + "). \nreturn" + line, ButtonType.OK).show();
                        error = true;
                    } else {
                        String returnValue;
                        if (line.length() >= "return".length() && line.charAt(1) == 'e')
                            returnValue = line.substring("return".length()).trim();
                        else
                            returnValue = line.substring("r".length()).trim();

                        int from = curObj;
                        int to = stackObj.peek();
                        if (to >= 0)
                            for (int i = Math.min(from, to); i < Math.max(from, to); i++) {
                                if (comments[i+1].size() == 0) continue;
                                Comment com = comments[i+1].get(comments[i+1].size()-1);
                                if (y < com.getMaxY()) {
                                    y = 10 + com.getMaxY();
                                }

                            }
                        if (stackObj.peek() == curObj) {
                            double h = Math.max(20, V.get().getArrowHeight() + 5);
                            y += Math.max(h, Helper.getTextHeight(returnValue) - h) + V.get().getBeforeSelfReturn(); //self-delegation correction

                        } else {
                            // Abstand vor dem Return einfügen - passend zur Textgröße oder fest bei keinem Return-Wert
                            if (returnValue.length() == 0)
                                y += V.get().getReturnBeforeEmpty() + V.get().getReturnBefore() + V.get().getArrowHeight();
                            else
                                y += Helper.getTextHeight(returnValue) + V.get().getReturnBefore() + V.get().getArrowHeight();
                        }
                        // Call beenden
                        Call endingCall = callStack.pop();
                        endingCall.endCall(y, returnValue);

                        if (stackObj.peek() == curObj) y += V.get().getSelfArrowHeight();

                        if (endingCall.isDestructor())
                            y += 0.75 * V.get().getCallWidth();
                        else {
                            if (stackObj.peek() == curObj) {
                                double h = Math.max(20, V.get().getArrowHeight() + 5);
                                y += h; //self-delegation correction
                            }
                            y += V.get().getArrowHeight() + 3;
                        }


                        if (stackObj.isEmpty()) curObj = -1;
                        else curObj = stackObj.pop();
                    }
                    // endregion
                } else if (line.toLowerCase().startsWith("box")) {
                    // region boxes

                    line = line.substring("box".length()).trim();
                    String type = line;
                    String cond = "";
                    if (line.indexOf(' ') > 0) {
                        type = line.substring(0, line.indexOf(' '));
                        cond = line.substring(line.indexOf(' ') + 1);
                    }

                    if (callList.size() == 0) {
                        new Alert(Alert.AlertType.WARNING, "Box starts before first call!\n" + oldLine, ButtonType.OK).showAndWait();
                        return false;
                    }
                    Box b = new Box(callList.size(), y + V.get().getBoxBeforeDist(), type, cond);
                    boxesInWork.add(b);
                    y += b.getHeadHeight() + V.get().getBoxBeforeDist();
                    // endregion
                } else if (line.equalsIgnoreCase("end")) {
                    // region box-end
                    if (boxesInWork.size() > 0) {

                        Box b = boxesInWork.remove(boxesInWork.size() - 1);
                        boxList.add(b);
                        b.endBox(callList.size() - 1, y + V.get().getBoxBottomMargin());


                        y += V.get().getBoxAfterDist() + V.get().getBoxBottomMargin();
                        double x0 = -1, x1 = -1;
                        int startDept = 0;

                        for (int i = b.getStartIndex(); i <= b.getEndIndex(); i++) {
                            Call c = callList.get(i);

                            double xCaller = c.getXMin();
                            double xReceiver = c.getXMax();
                            if (x0 == -1) {
                                x0 = xCaller;
                                x1 = xReceiver;

                                startDept = c.getCaller().getDepth();
                            }
                            if (x0 > xCaller) {
                                x0 = xCaller;
                                startDept = c.getCaller().getDepth();
                            }
                            if (x1 < xReceiver) x1 = xReceiver;
                        }

                        if (b.getStartIndex() > b.getEndIndex() && curObj >= 0) {
                            b.setXandWidth(getObjects().get(curObj).getX(), 0, getObjects().get(curObj).getDepth());
                            b.setLifeLine(getObjects().get(curObj));
                        } else
                            b.setXandWidth(x0, x1 - x0 + V.get().getBoxRigthMargin(), startDept);
                        for (Box bin : boxesInWork) {
                            bin.addInnerBox(b);
                        }
                    } else {
                        new Alert(Alert.AlertType.WARNING, "In line " + lineNr + " is an end outside a box.\n" + oldLine, ButtonType.OK).showAndWait();
                        error = true;
                    }
                    // endregion
                } else if (line.toLowerCase().startsWith("else ") || line.equalsIgnoreCase("else")) {
                    //region else
                    if (boxesInWork.size() == 0) {
                        new Alert(Alert.AlertType.WARNING, "In line " + lineNr + " is an else outside a box.\n" + oldLine, ButtonType.OK).showAndWait();
                        error = true;
                    } else {
                        if (line.indexOf(' ') >= 0) line = line.substring("else ".length());
                        else line = "";
                        y += V.get().getElseBefore();//20;
                        boxesInWork.get(boxesInWork.size() - 1).addAlt(y, line);


                        if (line.trim().length() == 0) y += V.get().getElseAfterEmpty();//10;
                        else {
                            y += Helper.getTextHeight(line);
                            y += V.get().getElseAfter();
                        }
                    }
                    //endregion
                } else {
                    // region Calls & Constructor
                    // >constructor stuff>
                    boolean constructor = line.startsWith("new ");
                    if (constructor) line = line.substring(3).trim();
                    // </constructor stuff>


                    if (line.contains(".")) {
                        int objIndex = getObjectIndex(line.substring(0, line.indexOf('.')));
                        if (objIndex < 0) {
                            new Alert(Alert.AlertType.WARNING, "Called object not found in line:\n" + oldLine, ButtonType.OK).show();
                            return false;
                        }
                        LifeLine receiver = objects.get(objIndex);
                        LifeLine caller = curObj < 0 ? null : objects.get(curObj);
                        String call = line.substring(line.indexOf('.') + 1);


                        // Comment Y-Shift if necessary
                        int from = curObj;
                        {
                            int to = objIndex;
                            if (from >= 0)
                                for (int i = Math.min(from, to); i < Math.max(from, to); i++) {
                                    if (comments[i+1].size() == 0) continue;
                                    Comment com = comments[i+1].get(comments[i+1].size()-1);
                                    if (y < com.getMaxY()) {
                                        y = 10 + com.getMaxY();
                                    }

                                }
                        }


                        //if (objIndex == curObj) y += Math.max(20,V.get().getArrowHeight()+5); //self-delegation correction

                        if (objIndex == curObj) {
                            double h = Math.max(20, V.get().getArrowHeight() + 5);
                            y += Math.max(h, Helper.getTextHeight(call)) + V.get().getBeforeSelfCall();
                            y += V.get().getSelfArrowHeight();
                            //Math.max(20,V.get().getArrowHeight()+5); //self-delegation correction
                        } else {
                            // y Platz für den Aufruf einfügen
                            if (call.length() == 0) y += V.get().getCallBefore() + V.get().getArrowHeight();
                            else
                                y += Math.max(Helper.getTextHeight(call), V.get().getArrowHeight()) + V.get().getCallBefore();
                        }


                        if (constructor) { // Wenn Constructor call: die Lifeline verschieben und y auf dessen Mitte setzen
                            y += receiver.getHeight() / 2;
                            receiver.setY(y - receiver.getHeight());
                        }

                        stackObj.push(curObj);


                        callStack.push(new Call(caller, receiver, y, call, constructor));


                        y += V.get().getArrowHeight() + 3;
                        //if (objIndex == curObj) y += Math.max(20,V.get().getArrowHeight()+5);


                        callList.add(callStack.peek());

                        curObj = objIndex;
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Please respect the syntax! Disrespectful is\n" + oldLine).show();
                        error = true;
                    }
                    //endregion
                }

            } catch (Exception t) {
                Helper.getExceptionAlert("Unhandled source code error", "Error in code!", "In line " + lineNr + " an error occured.\n" + oldLine, t).showAndWait();
                t.printStackTrace();
                return false;
            }
        }

        // ------------------- END PARSING -----------------------------
        if (!callStack.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, callStack.size() + " return(s) missing.", ButtonType.OK).show();
            error = true;
        }
        while (!callStack.isEmpty()) {
            Call c = callStack.pop();
            c.endCall(y += 10, "");
            if (c.getCaller() == c.getReceiver()) y += 30;
        }

        if (boxesInWork.size() > 0) {
            new Alert(Alert.AlertType.WARNING, boxesInWork.size() + " end(s) missing.", ButtonType.OK).show();
            error = true;
        }
        while (boxesInWork.size() > 0) {
            // Attention copied code
            Box b = boxesInWork.remove(boxesInWork.size() - 1);
            boxList.add(b);
            b.endBox(callList.size() - 1, y + 20);
            y += 30;
            double x0 = -1, x1 = -1;
            for (int i = b.getStartIndex(); i <= b.getEndIndex(); i++) {
                Call c = callList.get(i);
                double xc = Math.min(c.getXMin(), c.getXMin());
                double xr = Math.max(c.getXMax(), c.getXMax());
                if (x0 == -1) {
                    x0 = xc;
                    x1 = xr;
                }
                if (x0 > xc) x0 = xc;
                if (x1 < xr) x1 = xr;
            }
            b.setXandWidth(x0 - 10, x1 - x0 + 10, 0);
            for (Box bin : boxesInWork) {
                bin.addInnerBox(b);
            }
        }

        //Collections.reverse(boxList);
        for (Box b : boxList)
            b.correctForInnerBoxes();

        adjustMarginsForComments();

        //todo  System.out.println("Took: "+(System.currentTimeMillis()-zeit));

        return !error;
    }


    public ArrayList<LifeLine> getObjects() {
        return objects;
    }

    public ArrayList<Call> getCallList() {
        return callList;
    }

    public ArrayList<Box> getBoxList() {
        return boxList;
    }

    public ArrayList<Comment>[] getComments() {
        return comments;
    }


    private void initObjects(String text, ArrayList<Slider> sliders, VBox vbSliders, Main main) {
        objects = new ArrayList<>();

        String[] lines = text.split("\n");
        if (lines.length == 0) return;

        if (lines[0].trim().toLowerCase().startsWith("objects:")) {
            String s = lines[0].substring(lines[0].indexOf(':') + 1);
            lines = s.split(",");
            double xPos = 150;

            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].trim();
                String[] nc = lines[i].split(":");
                String name = nc[0];
                String klasse = nc.length > 1 ? nc[1] : "";


                LifeLine lifeLine = new LifeLine(name.trim(), klasse.trim(), xPos, 24);
                objects.add(lifeLine);
                lifeLine.setX(lifeLine.getX() + lifeLine.getWidth() / 2);

                xPos = lifeLine.getX() + lifeLine.getWidth() / 2 + 10;
                if (sliders != null && sliders.size() > i) lifeLine.setX(sliders.get(i).getValue());
            }

            if (sliders.size()!=objects.size()) sliders.clear();
            for (int i = 0; i < objects.size(); i++) {
                double oldSliderValue = sliders.size() > i ? sliders.get(i).getValue() : objects.get(i).getX();
                Slider slider = new Slider(0, 5000, oldSliderValue);

                if (i < sliders.size()) sliders.set(i, slider);
                else sliders.add(slider);

                final int fi = i;
                slider.valueProperty().addListener(
                        (observable, oldValue, newValue) -> {
                            objects.get(fi).setX(newValue.doubleValue());
                            main.parse(true);
                        }
                );
            }


            GridPane gp = new GridPane();
            for (int i = 0; i < objects.size(); i++) {
                Label value = new Label();
                gp.addRow(i, new Label(objects.get(i).getName() + ": "), value, sliders.get(i));
                final int ii = i;
                value.textProperty().bind(Bindings.createStringBinding(() -> "" + Math.round(sliders.get(ii).getValue()), sliders.get(ii).valueProperty()));
                GridPane.setHalignment(value, HPos.RIGHT);
                GridPane.setHgrow(sliders.get(i), Priority.ALWAYS);
            }
            vbSliders.getChildren().clear();
            vbSliders.getChildren().add(gp);
        }
    }


    private int getObjectIndex(String name) {
        name = name.replace("|", "").replace(" ", "");
        for (int i = 0; i < objects.size(); i++)
            if (objects.get(i).getName().replace("|", "").replace(" ", "").equalsIgnoreCase(name)) return i;
        return -1;
    }

    public double getMaxY() {
        double erg = 0;
        for (Call c : callList) if (c.getY1() > erg) erg = c.getY1();

        for (LifeLine l : objects) if (l.getY() + l.getHeight() > erg) erg = l.getY() + l.getHeight();

        for (int i = 0; i < comments.length; i++)
            for (Comment c : comments[i]) if (erg < c.getMaxY()) erg = c.getMaxY();
        return erg;
    }

    public double getMaxX() {
        double erg = 0;
        for (LifeLine l : objects)
            if (l.getX() + l.getWidth() > erg) erg = l.getX() + l.getWidth();
        for (Call c : callList) {
            if (c.getXMax() > erg) erg = c.getXMax();
        }
        for (Box b : boxList)
            if (b.getX() + b.getWidth() > erg) erg = b.getX() + b.getWidth();

        for (int i = 0; i < comments.length; i++)
            for (Comment c : comments[i]) if (erg < c.getMaxX()) erg = c.getMaxX();
        return erg;
    }

    public double getShiftX() {
        double erg = 0;
        for (Box b : boxList)
            if (b.getX() < erg) erg = b.getX();
        return erg;
    }

    private double getMaxComment(int col) {
        double erg = 0;
        for (int i = 0; i < comments[col].size(); i++) {
            double maxWidth = comments[col].get(i).getWidth();
            if (col>0) maxWidth += V.get().getCallWidth() / 2 + 10;

            if (maxWidth > erg)
                erg = maxWidth;
        }
        return erg;
    }

    /**
     * Returns a list of all boxes, that start in this column, if none an emty list is returned.
     *
     * @param col the column in which the box starts
     * @return a list of boxes
     */
    private List<Box> getBoxForColumn(int col) {
        ArrayList<Box> erg = new ArrayList<>();
        for (Box b : boxList) {
            int minCol = objects.size() + 2;
            if (b.getEndIndex() < b.getStartIndex()) {
                minCol = getObjectIndex(b.getLifeLine().getName());
            } else
                for (int i = b.getStartIndex(); i <= b.getEndIndex(); i++) {
                    LifeLine caller = callList.get(i).getCaller();
                    LifeLine rec = callList.get(i).getReceiver();
                    if (caller != null) minCol = Math.min(minCol, getObjectIndex(caller.getName()));
                    if (rec != null) minCol = Math.min(minCol, getObjectIndex(rec.getName()));
                }

            if (minCol == col ) erg.add(b);
        }
        /*System.err.println("------------------------------------------------");
        System.err.println("Col: "+col);
        for (Box b: erg) System.err.println(b);
        System.err.println("------------------------------------------------");*/
        return erg;
    }

    private void adjustMarginsForComments() {
        for (ArrayList<Comment> comList : comments)
            for (int i = 0; i < comList.size(); i++) {
                Comment com = comList.get(i);
                // check with every box,
                double leftMargin = 0;
                double rightMargin = 0;
                com.setMarginRight(0);
                com.setMarginLeft(0);

                for (Box box : boxList) {
                    double left = box.getX() + box.getWidth();

                    double y0 = box.getY0();
                    double y1 = box.getY1();

                    // comment is at same heigth
                    if (!(com.getY() > y1 || com.getMaxY() < y0)) {
                        // comment intersects also in X-direction
                        if (com.getX() < left && com.getMaxX() > left) {
                            leftMargin = Math.max(leftMargin, left - com.getX() + 10);
                        }
                    }
                }
                com.setMarginLeft(leftMargin);


                LifeLine life = com.getLifeline();
                int index = 0;
                if (life != null) index = getObjectIndex(life.getName())+1;
                for (Box box : getBoxForColumn(index)) {
                    double right = box.getX();

                    double y0 = box.getY0();
                    double y1 = box.getY1();

                    // comment is at same heigth
                    if (!(com.getY() > y1 || com.getMaxY() < y0)) {
                        rightMargin = Math.max(rightMargin, box.getXNegative() + 10);
                    }
                }

                com.setMarginRight(rightMargin);
              //  System.out.println(leftMargin + "/" + rightMargin + " " + com.toString());
            }
    }

    public void autosize(ArrayList<Slider> sliders) {
        if (callList.size() == 0) return;

            double x0 = 0;
            double[][] matrix = new double[objects.size()][objects.size()];


            x0 = getMaxComment(0);



            for (int i = 0; i < comments.length - 1; i++)
                matrix[i][i] = getMaxComment(i+1);

            for (Call c : callList) {
                LifeLine lifeCaller = c.getCaller();
                LifeLine lifeRecive = c.getReceiver();
                int ci, cr;
                if (lifeCaller != null) {
                    ci = getObjectIndex(lifeCaller.getName());
                    cr = getObjectIndex(lifeRecive.getName());
                    double val = matrix[ci][cr];
                    matrix[ci][cr] = Math.max(val, c.getAutoWidth());
                } else {
                    x0 = Math.max(x0, c.getAutoWidth());
                }
            }
         //   printMatrix(matrix);
            sliders.get(0).valueProperty().set(x0);


            for (int i = 1; i < sliders.size(); i++) {
                double max = 0;
                for (int j = 0; j < i; j++) {
                    if (matrix[i][j] + objects.get(j).getX() > max) max = matrix[i][j] + objects.get(j).getX();
                    if (matrix[j][i] + objects.get(j).getX() > max) max = matrix[j][i] + objects.get(j).getX();
                    if (matrix[j][j] + objects.get(j).getX() > max) max = matrix[j][j] + objects.get(j).getX();
                }
                if (max <= objects.get(i - 1).getX() + objects.get(i - 1).getWidth() / 2 + objects.get(i).getWidth() / 2 + 10)
                    max = objects.get(i - 1).getX() + objects.get(i - 1).getWidth() / 2 + objects.get(i).getWidth() / 2 + 10;
                sliders.get(i).valueProperty().set(max);

            }
    }


    private void printMatrix(double[][] matrix) {
        DecimalFormat df = new DecimalFormat("00000");
        System.out.println();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++)
                System.out.print(df.format(matrix[i][j]) + " ");
            System.out.println();
        }


    }
}
