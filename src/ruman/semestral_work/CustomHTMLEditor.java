package ruman.semestral_work;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;

public class CustomHTMLEditor extends HTMLEditor {
    Button save_button, image_button, add_group_button, add_note_button;

    public CustomHTMLEditor() {
        try {
            save_button = addButton("save_btn.png", arg0 -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Save location");
                File selectedDirectory = directoryChooser.showDialog(getScene().getWindow());
                if (selectedDirectory != null) {
                    Path dir = selectedDirectory.toPath();
                    Path file_path = dir.resolve("save.html");
                    String html = getHtmlText();
                    try {
                        Files.writeString(file_path, html, CREATE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            image_button = addButton("image_btn.png", arg0 -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select a file to import");
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
                if (selectedFile != null) {
                    System.out.println(selectedFile);
                }

                insertHtmlAfterCursor("<img src=\"" + "file:" + selectedFile + "\" alt=\"retard\" style=\"max-width: 704; max-height: 324\">");
            });

            // These buttons might get moved elsewhere
            add_group_button = addButton("add_group_btn.png", arg0 -> System.out.println("Added a group"));
            add_note_button = addButton("add_note_btn.png", arg0 -> System.out.println("Added a note"));
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

        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);

        Node node = lookup(".top-toolbar");
        if (node instanceof ToolBar) {
            ToolBar bar = (ToolBar) node;
            bar.getItems().add(button);
            bar.getItems().add(separator);
        }

        return button;
    }
}
