
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;

public class Help extends HBox {

    public Help() {
        WebView wv = new WebView();
        HBox.setHgrow(wv, Priority.ALWAYS);
        wv.getEngine().loadContent(
                "<html>" +
                        "<body>" +
                        "<h1>Help</h1>" +
                        "<div>This sequence diagram generator was coded by Roland Holzer and is only capable of those UML parts needed in the gymnasium (high school).</div>" +
                        "<h2>general structure</h2>" +
                        "<div>" +
                        "<div>The first line must start with <b>objects:</b> followed by a comma-seperated list of objects used in this diagram.<div>\r\n" +
                        "<div>An object has the syntax <b>name[:class]</b> i.e. the object name optionally followed a colon and a class name or" +
                        "<b>_name:class</b> as an anonymous object.</div>" +
                        "<div> Object-names with a _ at the beginning are not displayed they are only for " +
                        "referencing the object in calls.</div>" +
                        "<div>Object names with <b>[ ]</b> at the end are displayed as multi-objects. The brackets are part of the name.</div>" +
                        "<div>A <b>ygap</b> adds extra vertical space.</div>" +
                        "<div>A newline can be entered by &#x007C; (vertical bar) in any String.</div>" +
                        "</div>"+
                        "" +
                        "<h2>switches</h2>" +
                        "<ul>" +
                        "<li>none  - no actor or timer just a synchron call at the start</li>" +
                        "<li>actor - an actor at the start (asynchron call)</li>" +
                        "<li>clock - a timer at the start (asynchron call)</li>" +
                        "</ul>" +
                        ""+
                        "<h2>calls</h2>" +
                        "<div>" +
                        "<div>Calls are lines with <b>object-name.method(...)</b>. No checks are made the call starts at the" +
                        "current active object and is given to the object in the call.</div>" +
                        "<div>A call can start with <b>new</b> which creates a constructor-call. It will only adjust the " +
                        "y-coordinate of the lifeline. </div>" +

                        "<div> A call ends with <b>return [value]</b>. Instead of <b>return</b> a simple <b>r</b> can be used." +
                        "</div>" +
                        "<div>" +
                        "A <b>destroy()</b> is a destructor-call. This will end the life-line." +
                        "</div>" +
                        "" +
                        "<h2>boxes</h2>" +
                        "<div>" +
                        "<div>A box surrounds every call made between <b>box name [condition]</b> and <b>end</b>.</div>" +
                        "<div>An empty box surrounds the current object and has a width so that the name and condition fits inside.</div>"+
                        "<div>With <b>else [condition]</b> alternatives can be placed in an <i>alt</i> box (or any other box if it makes sense to you).</div>"+
                        "</div>" +
                        "" +
                        "<h2>example</h2>" +
                        "<pre>" +
                        "objects: dieGui, dieSteuerung, derPassagier[idx], dieFlugBuchung[i]\n" +
                        "actor\n" +
                        "dieGui.clickAnzeigeReiseziel( )\n" +
                        "\tdieSteuerung.anzeigeReiseziel( \"Maier\", \"Hans, \"20.01.2011\" )\n" +
                        "\t\tbox loop [idx=0 bis aAnzahlPassagiere-1]\n" +
                        "\t\t\tderPassagier[idx].gibName()\n" +
                        "\t\t\treturn derName\n" +
                        "\t\t\tderPassagier[idx].gibVorname()\n" +
                        "\t\t\treturn derVorname\n" +
                        "\t\t\tbox break [\"Hans Maier\" gefunden]\n" +
                        "\t\t\tend\n" +
                        "\t\t\t\n" +
                        "\t\tend\n" +
                        "\t\t\n" +
                        "\t\tbox opt [\"Hans Maier\" gefunden]\n" +
                        "\t\t\tbox loop [i=0 bis aAnzahlBuchungen-1]\n" +
                        "\t\t\t\tdieFlugBuchung[i].gibPassagierNr( )\n" +
                        "\t\t\t\treturn diePassagierNr\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\tbox opt [diePassagierNr = idx]\n" +
                        "\t\t\t\t\tdieFlugBuchung[i].gibDatum()\n" +
                        "\t\t\t\t\treturn dasDatum\n" +
                        "\t\t\t\tend\n" +
                        "\t\t\t\t\n" +
                        "\t\t\t\tbox break [Buchung von \"Hans Maier\" gefunden]\n" +
                        "\t\t\t\t\tdieFlugBuchung[i].gibReiseziel()\n" +
                        "\t\t\t\t\treturn dasReiseziel\n" +
                        "\t\t\t\tend\n" +
                        "\t\t\t\n" +
                        "\t\t\tend\n" +
                        "\t\tend\n" +
                        "\tdieGui.anzeigeText(\"Reiseziel: \"+dasReiseziel)\n" +
                        "\treturn\n" +
                        "\n" +
                        "return\n" +
                        "return" +
                        "</pre>" +
                        "" +
                        "</body>" +
                        "</html>"
        );
        this.getChildren().add(wv);

    }
}
