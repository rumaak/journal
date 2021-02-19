package ruman.semestral_work;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomHTMLEditor extends HTMLEditor {
    Button save_button, image_button;

    public CustomHTMLEditor() {
        try {
            Tooltip save_tooltip = new Tooltip("Save");
            save_button = addButton("save_btn.png", arg0 -> {}); // Save button handler is supplied in Controller class
            save_button.setTooltip(save_tooltip);

            addSeparator();

            Tooltip image_tooltip = new Tooltip("Add image");
            image_button = addButton("image_btn.png", arg0 -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select a file to import");
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
                if (selectedFile != null) {
                    insertHtmlAfterCursor("<img src=\"" + "file:" + selectedFile + "\" alt=\"Invalid\">");
                }
            });
            image_button.setTooltip(image_tooltip);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void insertHtmlAfterCursor(String html) {
        // replace invalid chars
        html = html.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");

        // get script
        String script = String.format(
                "(function(html) {"
                + "  var sel, range;"
                + "  if (window.getSelection) {"
                + "    sel = window.getSelection();"
                + "    if (sel.getRangeAt && sel.rangeCount) {"
                + "      range = sel.getRangeAt(0);"
                + "      range.deleteContents();"
                + "      var el = document.createElement(\"div\");"
                + "      el.innerHTML = html;"
                + "      var frag = document.createDocumentFragment(),"
                + "        node, lastNode;"
                + "      while ((node = el.firstChild)) {"
                + "        lastNode = frag.appendChild(node);"
                + "      }"
                + "      range.insertNode(frag);"
                + "      if (lastNode) {"
                + "        range = range.cloneRange();"
                + "        range.setStartAfter(lastNode);"
                + "        range.collapse(true);"
                + "        sel.removeAllRanges();"
                + "        sel.addRange(range);"
                + "      }"
                + "    }"
                + "  }"
                + "  else if (document.selection && "
                + "           document.selection.type != \"Control\") {"
                + "    document.selection.createRange().pasteHTML(html);"
                + "  }"
                + "})(\"%s\");", html);

        // execute script
        WebView mWebView = (WebView) lookup(".web-view");
        mWebView.getEngine().executeScript(script);
    }

    Button addButton(String graphic_file, EventHandler<ActionEvent> handler) throws IOException {
        Path path = Paths.get(".").resolve("resources").resolve(graphic_file);
        ImageView graphic = new ImageView(new Image(Files.newInputStream(path)));
        Button button = new Button("", graphic);
        button.setOnAction(handler);

        Node node = lookup(".top-toolbar");
        if (node instanceof ToolBar) {
            ToolBar bar = (ToolBar) node;
            bar.getItems().add(button);
        }

        return button;
    }

    void addSeparator() {
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);

        Node node = lookup(".top-toolbar");
        if (node instanceof ToolBar) {
            ToolBar bar = (ToolBar) node;
            bar.getItems().add(separator);
        }
    }
}
