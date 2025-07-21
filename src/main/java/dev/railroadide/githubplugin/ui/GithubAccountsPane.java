package dev.railroadide.githubplugin.ui;

import com.sun.javafx.application.HostServicesDelegate;
import dev.railroadide.core.localization.LocalizationServiceLocator;
import dev.railroadide.core.ui.RRButton;
import dev.railroadide.core.ui.RRHBox;
import dev.railroadide.core.ui.RRListView;
import dev.railroadide.core.ui.RRVBox;
import dev.railroadide.core.ui.localized.LocalizedLabel;
import dev.railroadide.core.ui.localized.LocalizedTooltip;
import dev.railroadide.githubplugin.GithubPlugin;
import dev.railroadide.githubplugin.data.GithubAccount;
import dev.railroadide.githubplugin.data.GithubUser;
import dev.railroadide.githubplugin.http.AccessTokenResponse;
import dev.railroadide.githubplugin.http.DeviceCodeResponse;
import dev.railroadide.githubplugin.http.GithubRequests;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GithubAccountsPane extends RRVBox {
    private final RRListView<GithubAccount> accountsListView = new RRListView<>();

    public GithubAccountsPane(List<GithubAccount> accounts) {
        getStylesheets().add(getClass().getResource("/assets/github/github_accounts.css").toExternalForm());
        setPadding(new Insets(16));
        setSpacing(10);
        setAccounts(accounts);
        accountsListView.setCellFactory(listView -> new GithubAccountListCell(account ->
                accountsListView.getItems().remove(account)));
        setFocusTraversable(false);

        var addButton = new RRButton("github.button.add_account");
        addButton.setTooltip(new LocalizedTooltip("github.accounts.add_account.tooltip"));
        addButton.setOnAction($ -> showAddAccountDialog());

        var scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(accountsListView);
        scrollPane.setPadding(new Insets(0, 0, 10, 0));

        var buttonBox = new RRHBox();
        buttonBox.getChildren().add(addButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        getChildren().setAll(scrollPane, buttonBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void showAddAccountDialog() {
        var dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(LocalizationServiceLocator.getInstance().get("github.dialog.add_account.title"));

        var dialogVbox = new RRVBox(20);
        dialogVbox.setPadding(new Insets(24));
        dialogVbox.setAlignment(Pos.CENTER);

        var instructions = new LocalizedLabel("github.settings.plugins.github.accounts.description");
        instructions.setWrapText(true);
        instructions.getStyleClass().add("github-accounts-instructions");
        dialogVbox.getChildren().add(instructions);

        var authorizeButton = new RRButton("github.button.authorize");
        authorizeButton.setTooltip(new LocalizedTooltip("github.accounts.authorize.tooltip"));
        dialogVbox.getChildren().add(authorizeButton);

        var loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        dialogVbox.getChildren().add(loadingIndicator);

        authorizeButton.setOnAction($ -> {
            authorizeButton.setDisable(true);
            loadingIndicator.setVisible(true);
            CompletableFuture.runAsync(() -> {
                try {
                    DeviceCodeResponse deviceCodeResponse = GithubRequests.requestDeviceCode("repo,read:user,user:email,notifications,gist,read:org");
                    String verificationUri = deviceCodeResponse.verificationUri();
                    String userCode = deviceCodeResponse.userCode();
                    String deviceCode = deviceCodeResponse.deviceCode();
                    int interval = deviceCodeResponse.interval();

                    Platform.runLater(() -> {
                        dialogVbox.getChildren().clear();
                        var mfaInstructions = new LocalizedLabel("github.accounts.mfa.instructions");
                        mfaInstructions.getStyleClass().add("github-accounts-instructions");
                        mfaInstructions.setWrapText(true);
                        mfaInstructions.setTextAlignment(TextAlignment.CENTER);
                        dialogVbox.getChildren().add(mfaInstructions);

                        var codeDisplay = new MFACodeDisplay(userCode);
                        if (codeDisplay.getChildren().size() > 1 && codeDisplay.getChildren().get(1) instanceof RRButton copyBtn) {
                            copyBtn.setTooltip(new LocalizedTooltip("github.accounts.mfa.copy.tooltip"));
                        }

                        codeDisplay.getOpenBrowserButton().setOnAction($$ -> {
                            tryOpenUrl(verificationUri);
                            var fallbackMsg = new LocalizedLabel("github.accounts.mfa.open_browser.fallback", verificationUri);
                            fallbackMsg.getStyleClass().add("github-mfa-fallback-msg");
                            fallbackMsg.setWrapText(true);
                            fallbackMsg.setTextAlignment(TextAlignment.CENTER);
                            if (dialogVbox.getChildren().size() < 3 || !(dialogVbox.getChildren().get(2) instanceof Label)) {
                                dialogVbox.getChildren().add(2, fallbackMsg);
                            } else {
                                dialogVbox.getChildren().set(2, fallbackMsg);
                            }

                            new Thread(() -> {
                                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                                Platform.runLater(() -> dialogVbox.getChildren().remove(fallbackMsg));
                            }).start();
                        });
                        codeDisplay.getOpenBrowserButton().setTooltip(new LocalizedTooltip("github.accounts.mfa.open_browser.tooltip"));
                        dialogVbox.getChildren().add(codeDisplay);

                        var cancelButton = new RRButton("railroad.generic.cancel");
                        cancelButton.setOnAction($$ -> dialog.close());
                        dialogVbox.getChildren().add(cancelButton);

                        pollForAccessToken(deviceCode, interval, dialog, loadingIndicator, authorizeButton);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showErrorDialog("github.accounts.error.device_authorization", ex.getMessage());
                        loadingIndicator.setVisible(false);
                        authorizeButton.setDisable(false);
                    });
                }
            });
        });

        var scene = new Scene(dialogVbox, 420, 300);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private void showErrorDialog(String messageKey, Object... messageArgs) {
        var alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(LocalizationServiceLocator.getInstance().get(messageKey, messageArgs));
        alert.showAndWait();
    }

    private static void tryOpenUrl(String url) {
        boolean desktopWorked = false;
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create(url));
                desktopWorked = true;
            } catch (Exception ignored) {}
        }

        if (desktopWorked) return;

        boolean wwwBrowserWorked = false;
        try {
            new ProcessBuilder("x-www-browser", url).start();
            wwwBrowserWorked = true;
        } catch (Exception ignored) {}

        if (wwwBrowserWorked) return;

        HostServicesDelegate hostServices = HostServicesDelegate.getInstance(null);
        if (hostServices != null) {
            try {
                hostServices.showDocument(url);
            } catch (Exception ignored) {}
        }
    }

    public List<GithubAccount> getAccounts() {
        return accountsListView.getItems();
    }

    public void setAccounts(List<GithubAccount> accounts) {
        accountsListView.getItems().setAll(accounts);
    }

    private void pollForAccessToken(String deviceCode, int interval, Stage parentDialog, ProgressIndicator loadingIndicator, RRButton authorizeButton) {
        CompletableFuture.runAsync(() -> {
            while (!Thread.interrupted() && parentDialog.isShowing()) {
                try {
                    AccessTokenResponse response = GithubRequests.requestAccessToken(deviceCode);
                    if (response instanceof AccessTokenResponse.ErrorResponse errorResponse) {
                        if (errorResponse.getErrorType() == AccessTokenResponse.ErrorType.AUTHORIZATION_PENDING) {
                            Thread.sleep(interval * 1000L);
                            GithubPlugin.LOGGER.debug("Polling for access token, waiting for {} seconds", interval);
                            continue;
                        } else if (errorResponse.getErrorType() == AccessTokenResponse.ErrorType.SLOW_DOWN) {
                            Thread.sleep((interval + 5) * 1000L);
                            GithubPlugin.LOGGER.debug("Polling for access token, slowing down, waiting for {} seconds", interval + 5);
                            continue;
                        } else {
                            Platform.runLater(() -> {
                                showErrorDialog("github.accounts.error.polling_access_token", errorResponse.getErrorType());
                                parentDialog.close();
                                loadingIndicator.setVisible(false);
                                authorizeButton.setDisable(false);
                            });
                            break;
                        }
                    }

                    if (response instanceof AccessTokenResponse.SuccessResponse successResponse) {
                        String accessToken = successResponse.getAccessToken();
                        if (accessToken.isEmpty()) {
                            GithubPlugin.LOGGER.error("Received empty access token");
                            Platform.runLater(() -> {
                                showErrorDialog("github.accounts.error.empty_access_token");
                                parentDialog.close();
                                loadingIndicator.setVisible(false);
                                authorizeButton.setDisable(false);
                            });
                            break;
                        }

                        GithubPlugin.LOGGER.debug("Successfully received access token");
                        GithubUser githubUser = GithubRequests.requestUser(accessToken);
                        if (githubUser == null) {
                            GithubPlugin.LOGGER.error("Failed to retrieve user information");
                            Platform.runLater(() -> {
                                showErrorDialog("github.accounts.error.user_info");
                                parentDialog.close();
                                loadingIndicator.setVisible(false);
                                authorizeButton.setDisable(false);
                            });
                            break;
                        }

                        GithubPlugin.LOGGER.debug("Successfully retrieved user information: {}", githubUser);
                        var account = new GithubAccount(githubUser);
                        account.aliasProperty().set(githubUser.login() == null ? githubUser.name() : githubUser.login());
                        account.setAccessToken(accessToken.toCharArray());
                        Platform.runLater(() -> {
                            accountsListView.getItems().add(account);
                            GithubPlugin.LOGGER.info("Added new GitHub account: {}", githubUser.login());
                            parentDialog.close();
                            loadingIndicator.setVisible(false);
                            authorizeButton.setDisable(false);
                        });
                        break;
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    GithubPlugin.LOGGER.debug("Polling for access token interrupted");
                    return;
                } catch (Exception exception) {
                    GithubPlugin.LOGGER.error("Error while polling for access token", exception);
                    Platform.runLater(() -> {
                        showErrorDialog("github.accounts.error.polling_access_token", exception.getMessage());
                        parentDialog.close();
                        loadingIndicator.setVisible(false);
                        authorizeButton.setDisable(false);
                    });
                    return;
                }
            }
        });
    }
}
