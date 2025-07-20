package io.github.railroad.github.ui;

import io.github.railroad.core.ui.RRButton;
import io.github.railroad.core.ui.RRHBox;
import io.github.railroad.core.ui.RRVBox;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class MFACodeDisplay extends RRVBox {
    private final RRButton openBrowserBtn;

    public MFACodeDisplay(String code) {
        setSpacing(10);
        setAlignment(Pos.CENTER);

        var digitsContainer = new RRHBox(5);
        digitsContainer.setAlignment(Pos.CENTER);

        for (char ch : code.toCharArray()) {
            var textField = new TextField(Character.toString(ch));
            textField.setPrefWidth(40);
            textField.setEditable(false);
            textField.setAlignment(Pos.CENTER);
            digitsContainer.getChildren().add(textField);
        }

        var copyBtn = new RRButton("github.button.copy_code");
        copyBtn.setGraphic(new FontIcon(FontAwesomeSolid.COPY));
        copyBtn.setOnAction($ -> {
            Clipboard cb = Clipboard.getSystemClipboard();
            var content = new ClipboardContent();
            content.putString(code);
            cb.setContent(content);
        });

        this.openBrowserBtn = new RRButton("github.button.open_browser");
        openBrowserBtn.setGraphic(new FontIcon(FontAwesomeSolid.EXTERNAL_LINK_ALT));

        var buttonRow = new RRHBox(10);
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.getChildren().addAll(copyBtn, openBrowserBtn);

        getChildren().setAll(digitsContainer, buttonRow);
    }

    public RRButton getOpenBrowserButton() {
        return openBrowserBtn;
    }
}
