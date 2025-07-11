package io.github.railroad.github.ui;

import io.github.railroad.core.localization.LocalizationService;
import io.github.railroad.core.localization.LocalizationServiceLocator;
import io.github.railroad.core.ui.RRButton;
import io.github.railroad.core.ui.RRHBox;
import io.github.railroad.core.ui.RRStackPane;
import io.github.railroad.core.ui.RRVBox;
import io.github.railroad.github.GithubAccount;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

public class GithubAccountListCell extends ListCell<GithubAccount> {
    private final StackPane node = new RRStackPane();
    private final GithubAccountView accountView = new GithubAccountView();

    public GithubAccountListCell(Consumer<GithubAccount> onRemove) {
        getStyleClass().add("github-account-list-cell");
        node.getChildren().add(accountView);

        var ellipsisButton = new RRButton();
        ellipsisButton.setText("...");
        ellipsisButton.setBackground(null);
        StackPane.setAlignment(ellipsisButton, Pos.TOP_RIGHT);

        var dropdown = new ContextMenu();

        var removeItem = new MenuItem(LocalizationServiceLocator.getInstance().get("github.button.remove_account"));
        removeItem.setOnAction($ -> {
            GithubAccount account = getItem();
            if(account != null && onRemove != null) {
                onRemove.accept(account);
            }
        });

        dropdown.getItems().add(removeItem);

        ellipsisButton.setContextMenu(dropdown);
        node.getChildren().add(ellipsisButton);
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
            setGraphic(this.accountView);
        }
    }

    public static class GithubAccountView extends RRHBox {
        private final ObjectProperty<GithubAccount> accountProperty = new SimpleObjectProperty<>();

        public GithubAccountView() {
            getStyleClass().add("github-account-view");
            setSpacing(5);
            setPadding(new Insets(10));
            setAlignment(Pos.CENTER_LEFT);

            var aliasLabel = new Label();
            aliasLabel.textProperty().bind(accountProperty.flatMap(GithubAccount::aliasProperty));
            aliasLabel.getStyleClass().add("github-alias-label");

            var usernameLabel = new Label();
            usernameLabel.textProperty().bind(accountProperty.flatMap(GithubAccount::usernameProperty));
            usernameLabel.getStyleClass().add("github-username-label");

            var icon = new ImageView();
            icon.setFitWidth(32);
            icon.setFitHeight(32);
            icon.setPreserveRatio(true);

            var labels = new RRVBox();
            labels.getChildren().addAll(aliasLabel, usernameLabel);
            getChildren().addAll(icon, labels);
        }

        public GithubAccountView(GithubAccount account) {
            this();
            this.accountProperty.set(account);
        }

        public ObjectProperty<GithubAccount> accountProperty() {
            return accountProperty;
        }
    }
}
