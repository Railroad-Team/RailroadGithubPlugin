package io.github.railroad.github.ui;

import io.github.railroad.core.localization.LocalizationServiceLocator;
import io.github.railroad.core.ui.RRTextField;
import io.github.railroad.core.ui.RRVBox;
import io.github.railroad.core.ui.localized.LocalizedLabel;
import io.github.railroad.github.data.GithubAccount;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;

/**
 * Style classes used in this file:
 * - edit-alias-dialog-root: root container for the dialog
 * - edit-alias-label: label for the alias field
 * - edit-alias-field: input field for the alias
 */
public class EditAliasDialog extends Dialog<String> {
    private final RRTextField aliasField = new RRTextField();

    public EditAliasDialog(GithubAccount account) {
        setTitle(LocalizationServiceLocator.getInstance().get("github.button.edit_alias"));

        var root = new RRVBox(12);
        root.getStyleClass().add("edit-alias-dialog-root");
        root.setPadding(new Insets(20));

        var aliasLabel = new LocalizedLabel("github.button.edit_alias");
        aliasLabel.getStyleClass().add("edit-alias-label");
        aliasLabel.setLabelFor(aliasField);

        aliasField.getStyleClass().add("edit-alias-field");
        aliasField.setTooltip(new Tooltip(LocalizationServiceLocator.getInstance().get("github.account.edit_alias.tooltip")));
        aliasField.setText(account.getAlias());

        root.getChildren().addAll(aliasLabel, aliasField);
        getDialogPane().setContent(root);

        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText(LocalizationServiceLocator.getInstance().get("railroad.generic.save"));
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText(LocalizationServiceLocator.getInstance().get("railroad.generic.cancel"));
        cancelButton.setCancelButton(true);

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return aliasField.getText();
            }
            
            return null;
        });

        setOnShown($ -> aliasField.requestFocus());
    }
}
