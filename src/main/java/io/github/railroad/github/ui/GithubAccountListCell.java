package io.github.railroad.github.ui;

import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeSolid;
import io.github.railroad.core.localization.LocalizationServiceLocator;
import io.github.railroad.core.ui.RRButton;
import io.github.railroad.core.ui.RRHBox;
import io.github.railroad.core.ui.RRStackPane;
import io.github.railroad.core.ui.RRVBox;
import io.github.railroad.core.ui.localized.LocalizedTooltip;
import io.github.railroad.github.data.GithubAccount;
import io.github.railroad.github.util.GithubAvatar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

import java.util.function.Consumer;

public class GithubAccountListCell extends ListCell<GithubAccount> {
    private final GithubAccountView accountView = new GithubAccountView();
    private final RRButton ellipsisButton;
    private final RRStackPane node;

    public GithubAccountListCell(Consumer<GithubAccount> onRemove) {
        getStyleClass().add("github-account-list-cell");
        node = new RRStackPane();
        node.getChildren().add(accountView);

        var ellipsisIcon = new MFXFontIcon(FontAwesomeSolid.ELLIPSIS_VERTICAL);
        ellipsisIcon.setSize(16);
        ellipsisIcon.setTextAlignment(TextAlignment.CENTER);

        ellipsisButton = new RRButton();
        ellipsisButton.setGraphic(ellipsisIcon);
        ellipsisButton.setVariant(RRButton.ButtonVariant.GHOST);
        ellipsisButton.setButtonSize(RRButton.ButtonSize.SMALL);
        ellipsisButton.setTooltip(new LocalizedTooltip("github.account.ellipsis.tooltip"));
        ellipsisButton.setFocusTraversable(true);
        ellipsisButton.getStyleClass().add("github-account-ellipsis-button");
        StackPane.setAlignment(ellipsisButton, Pos.CENTER_RIGHT);

        var dropdown = new ContextMenu();

        var removeItem = new MenuItem(LocalizationServiceLocator.getInstance().get("github.button.remove_account"));
        removeItem.setOnAction($ -> {
            GithubAccount account = getItem();
            if(account != null && onRemove != null) {
                onRemove.accept(account);
            }
        });

        var editAliasItem = new MenuItem(LocalizationServiceLocator.getInstance().get("github.button.edit_alias"));
        editAliasItem.setOnAction($ -> {
            GithubAccount account = getItem();
            if(account != null) {
                var dialog = new EditAliasDialog(account);
                dialog.showAndWait().ifPresent(alias -> {
                    if(!alias.trim().isBlank()) {
                        account.aliasProperty().set(alias);
                    }
                });
            }
        });

        dropdown.getItems().addAll(editAliasItem, removeItem);
        ellipsisButton.setContextMenu(dropdown);
        node.getChildren().add(ellipsisButton);

        ellipsisButton.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER, SPACE -> ellipsisButton.getContextMenu().show(ellipsisButton, Side.BOTTOM, 0, 0);
            }
        });

        ellipsisButton.setOnAction(event -> {
            if (ellipsisButton.getContextMenu().isShowing()) {
                ellipsisButton.getContextMenu().hide();
            } else {
                ellipsisButton.getContextMenu().show(ellipsisButton, Side.BOTTOM, 0, 0);
            }
        });

        node.getStyleClass().add("github-account-cell");
    }

    @Override
    protected void updateItem(GithubAccount account, boolean empty) {
        super.updateItem(account, empty);
        if (empty || account == null) {
            setText(null);
            setGraphic(null);
            accountView.accountProperty().set(null);
        } else {
            accountView.accountProperty().set(account);
            setGraphic(node);
        }
    }

    public static class GithubAccountView extends RRHBox {
        private final ObjectProperty<GithubAccount> accountProperty = new SimpleObjectProperty<>();
        private final ImageView icon;
        private final Label aliasLabel;
        private final Label usernameLabel;

        public GithubAccountView() {
            getStyleClass().add("github-account-view");
            setSpacing(10);
            setPadding(new Insets(10));
            setAlignment(Pos.CENTER_LEFT);

            icon = new ImageView();
            icon.getStyleClass().add("github-account-avatar");
            icon.setFitWidth(32);
            icon.setFitHeight(32);
            icon.setPreserveRatio(true);
            accountProperty.addListener(($1, $2, account) -> {
                if(account != null) {
                    icon.setImage(GithubAvatar.getAvatar(account));
                } else {
                    icon.setImage(null);
                }
            });

            aliasLabel = new Label();
            aliasLabel.textProperty().bind(accountProperty.flatMap(GithubAccount::aliasProperty));
            aliasLabel.getStyleClass().add("github-alias-label");

            usernameLabel = new Label();
            usernameLabel.textProperty().bind(accountProperty.map(account -> {
                if (account != null) {
                    return account.getOrRequestUser().login();
                } else {
                    return "";
                }
            }));
            usernameLabel.getStyleClass().add("github-username-label");

            var labels = new RRVBox();
            labels.setSpacing(2);
            labels.getChildren().addAll(aliasLabel, usernameLabel);
            getChildren().setAll(icon, labels);
        }

        public ObjectProperty<GithubAccount> accountProperty() {
            return accountProperty;
        }
    }
}
